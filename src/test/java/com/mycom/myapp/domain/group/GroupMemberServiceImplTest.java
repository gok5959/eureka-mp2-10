package com.mycom.myapp.domain.group;

import com.mycom.myapp.domain.group.dto.GroupMemberResponse;
import com.mycom.myapp.domain.group.dto.GroupMemberSearchCondition;
import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.group.entity.GroupMember;
import com.mycom.myapp.domain.group.exception.GroupMemberAddDuplicateException;
import com.mycom.myapp.domain.group.exception.GroupMemberNotFoundException;
import com.mycom.myapp.domain.group.exception.GroupNotFoundException;
import com.mycom.myapp.domain.group.exception.GroupPermissionDeniedException;
import com.mycom.myapp.domain.group.repository.GroupMemberRepository;
import com.mycom.myapp.domain.group.repository.GroupRepository;
import com.mycom.myapp.domain.group.service.GroupMemberServiceImpl;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceImplTest {

    @Mock GroupMemberRepository groupMemberRepository;
    @Mock GroupRepository groupRepository;
    @Mock UserRepository userRepository;

    @InjectMocks
    GroupMemberServiceImpl groupMemberService;

    @Test
    @DisplayName("searchGroupMembers - 조건/페이징으로 조회하고 Response로 매핑한다")
    void searchGroupMembers_success() {
        Long groupId = 10L;
        GroupMemberSearchCondition cond = GroupMemberSearchCondition.builder()
                .keyword("kim")
                .role(GroupMemberRole.MEMBER)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        // from()에서 user 접근하므로 user 세팅 필수
        User user = User.builder().id(1L).name("kim").email("kim@test.com").build();
        Group group = Group.builder().id(groupId).build();
        GroupMember gm = GroupMember.builder()
                .id(100L)
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .build();

        Page<GroupMember> page = new PageImpl<>(List.of(gm), pageable, 1);

        given(groupMemberRepository.searchMembers(groupId, "kim", GroupMemberRole.MEMBER, pageable))
                .willReturn(page);

        Page<GroupMemberResponse> result = groupMemberService.searchGroupMembers(groupId, cond, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        GroupMemberResponse r = result.getContent().get(0);
        assertThat(r.getUserId()).isEqualTo(1L);
        assertThat(r.getUserName()).isEqualTo("kim");
        assertThat(r.getEmail()).isEqualTo("kim@test.com");
        assertThat(r.getRole()).isEqualTo(GroupMemberRole.MEMBER);

        then(groupMemberRepository).should(times(1))
                .searchMembers(groupId, "kim", GroupMemberRole.MEMBER, pageable);
    }

    @Test
    @DisplayName("addGroupMember - 정상 추가")
    void addGroupMember_success() {
        Long groupId = 10L;
        Long userId = 1L;

        Group group = Group.builder().id(groupId).build();
        User user = User.builder().id(userId).name("kim").email("kim@test.com").build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)).willReturn(false);

        GroupMember saved = GroupMember.builder()
                .id(100L)
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .build();

        given(groupMemberRepository.save(any(GroupMember.class))).willReturn(saved);

        GroupMemberResponse response = groupMemberService.addGroupMember(groupId, userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUserName()).isEqualTo("kim");
        assertThat(response.getEmail()).isEqualTo("kim@test.com");
        assertThat(response.getRole()).isEqualTo(GroupMemberRole.MEMBER);

        then(groupMemberRepository).should(times(1)).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("addGroupMember - 그룹이 없으면 예외")
    void addGroupMember_groupNotFound_throw() {
        given(groupRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> groupMemberService.addGroupMember(10L, 1L))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    @DisplayName("addGroupMember - 유저가 없으면 예외")
    void addGroupMember_userNotFound_throw() {
        Long groupId = 10L;
        Long userId = 1L;

        given(groupRepository.findById(groupId)).willReturn(Optional.of(Group.builder().id(groupId).build()));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> groupMemberService.addGroupMember(groupId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("addGroupMember - 이미 멤버면 중복 예외")
    void addGroupMember_duplicate_throw() {
        Long groupId = 10L;
        Long userId = 1L;

        given(groupRepository.findById(groupId)).willReturn(Optional.of(Group.builder().id(groupId).build()));
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().id(userId).build()));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)).willReturn(true);

        assertThatThrownBy(() -> groupMemberService.addGroupMember(groupId, userId))
                .isInstanceOf(GroupMemberAddDuplicateException.class);

        then(groupMemberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("deleteGroupMember - 멤버가 없으면 예외")
    void deleteGroupMember_memberNotFound_throw() {
        Long groupId = 10L;
        Long targetUserId = 2L;
        Long currentUserId = 2L;

        Group group = Group.builder()
                .id(groupId)
                .owner(User.builder().id(999L).build())
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)).willReturn(false);

        assertThatThrownBy(() -> groupMemberService.deleteGroupMember(groupId, targetUserId, currentUserId))
                .isInstanceOf(GroupMemberNotFoundException.class);

        then(groupMemberRepository).should(never()).deleteByGroupIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("deleteGroupMember - 본인 탈퇴면 삭제된다")
    void deleteGroupMember_selfLeave_success() {
        Long groupId = 10L;
        Long targetUserId = 2L;
        Long currentUserId = 2L;

        Group group = Group.builder()
                .id(groupId)
                .owner(User.builder().id(999L).build())
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)).willReturn(true);

        willDoNothing().given(groupMemberRepository).deleteByGroupIdAndUserId(groupId, targetUserId);

        groupMemberService.deleteGroupMember(groupId, targetUserId, currentUserId);

        then(groupMemberRepository).should(times(1)).deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Test
    @DisplayName("deleteGroupMember - 추방은 OWNER만 가능")
    void deleteGroupMember_kick_requiresOwner() {
        Long groupId = 10L;
        Long targetUserId = 2L;
        Long currentUserId = 3L;

        Group group = Group.builder()
                .id(groupId)
                .owner(User.builder().id(999L).build())
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)).willReturn(true);

        assertThatThrownBy(() -> groupMemberService.deleteGroupMember(groupId, targetUserId, currentUserId))
                .isInstanceOf(GroupPermissionDeniedException.class);

        then(groupMemberRepository).should(never()).deleteByGroupIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("deleteGroupMember - OWNER가 추방하면 삭제된다")
    void deleteGroupMember_kick_byOwner_success() {
        Long groupId = 10L;
        Long targetUserId = 2L;
        Long currentUserId = 999L;

        Group group = Group.builder()
                .id(groupId)
                .owner(User.builder().id(currentUserId).build())
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)).willReturn(true);

        willDoNothing().given(groupMemberRepository).deleteByGroupIdAndUserId(groupId, targetUserId);

        groupMemberService.deleteGroupMember(groupId, targetUserId, currentUserId);

        then(groupMemberRepository).should(times(1)).deleteByGroupIdAndUserId(groupId, targetUserId);
    }
}
