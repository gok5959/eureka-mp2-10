package com.mycom.myapp.domain.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.group.dto.GroupCreateRequest;
import com.mycom.myapp.domain.group.dto.GroupDetailResponse;
import com.mycom.myapp.domain.group.dto.GroupListResponse;
import com.mycom.myapp.domain.group.dto.GroupResponse;
import com.mycom.myapp.domain.group.dto.GroupSearchCondition;
import com.mycom.myapp.domain.group.dto.GroupUpdateRequest;
import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.group.entity.GroupMember;
import com.mycom.myapp.domain.group.exception.GroupNotFoundException;
import com.mycom.myapp.domain.group.exception.GroupPermissionDeniedException;
import com.mycom.myapp.domain.group.repository.GroupMemberRepository;
import com.mycom.myapp.domain.group.repository.GroupRepository;
import com.mycom.myapp.domain.group.service.GroupServiceImpl;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GroupMemberRepository groupMemberRepository;

    @InjectMocks
    GroupServiceImpl groupService;

    @Test
    @DisplayName("createGroup - OWNER는 항상 GroupMember에 포함되어야 한다")
    void createGroup_success() {
        // given
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("owner")
                .email("owner@test.com")
                .build();

        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("스터디 그룹");
        request.setDescription("JPA 스터디");

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));

        Group savedGroup = Group.builder()
                .id(10L)
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);
        given(groupMemberRepository.save(any(GroupMember.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        GroupResponse response = groupService.createGroup(request, ownerId);

        // then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("스터디 그룹");
        assertThat(response.getOwnerId()).isEqualTo(ownerId);

        // GroupMember 저장 로직 검증
        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        then(groupMemberRepository).should().save(captor.capture());
        GroupMember groupMember = captor.getValue();
        System.out.println(groupMember.getGroup().getId());
        assertThat(groupMember.getGroup().getId()).isEqualTo(10L);
        assertThat(groupMember.getUser().getId()).isEqualTo(ownerId);
        assertThat(groupMember.getRole()).isEqualTo(GroupMemberRole.OWNER);
    }

    @Test
    @DisplayName("createGroup - ownerId에 해당하는 유저가 없으면 UserNotFoundException")
    void createGroup_userNotFound() {
        // given
        Long ownerId = 1L;
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("스터디");

        given(userRepository.findById(ownerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(request, ownerId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("그룹 생성 중 사용자");
    }

    @Test
    @DisplayName("updateGroupByIdAndUserId - OWNER가 이름/설명을 수정할 수 있다")
    void updateGroup_success() {
        // given
        Long ownerId = 1L;
        Long groupId = 10L;

        User owner = User.builder()
                .id(ownerId)
                .name("owner")
                .email("owner@test.com")
                .build();

        Group group = Group.builder()
                .id(groupId)
                .name("old name")
                .description("old desc")
                .owner(owner)
                .build();

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName("new name");
        request.setDescription("new desc");

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

        // when
        GroupResponse response = groupService.updateGroupByIdAndUserId(groupId, request, ownerId);

        // then
        assertThat(group.getName()).isEqualTo("new name");
        assertThat(group.getDescription()).isEqualTo("new desc");
        assertThat(response.getName()).isEqualTo("new name");
    }

    @Test
    @DisplayName("updateGroupByIdAndUserId - 그룹이 없으면 GroupNotFoundException")
    void updateGroup_groupNotFound() {
        // given
        Long userId = 1L;
        Long groupId = 10L;

        User user = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupRepository.findById(groupId)).willReturn(Optional.empty());

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName("new name");

        // when & then
        assertThatThrownBy(() -> groupService.updateGroupByIdAndUserId(groupId, request, userId))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    @DisplayName("updateGroupByIdAndUserId - OWNER가 아니면 GroupPermissionDeniedException")
    void updateGroup_permissionDenied() {
        // given
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long groupId = 10L;

        User currentUser = User.builder().id(otherUserId).build();
        User owner = User.builder().id(ownerId).build();

        Group group = Group.builder()
                .id(groupId)
                .name("old")
                .description("old")
                .owner(owner)
                .build();

        given(userRepository.findById(otherUserId)).willReturn(Optional.of(currentUser));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName("new name");

        // when & then
        assertThatThrownBy(() -> groupService.updateGroupByIdAndUserId(groupId, request, otherUserId))
                .isInstanceOf(GroupPermissionDeniedException.class);
    }

    @Test
    @DisplayName("deleteGroupByIdAndUserId - OWNER는 그룹을 삭제할 수 있다")
    void deleteGroup_success() {
        // given
        Long ownerId = 1L;
        Long groupId = 10L;

        User owner = User.builder().id(ownerId).build();
        Group group = Group.builder()
                .id(groupId)
                .name("group")
                .owner(owner)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

        // when
        groupService.deleteGroupByIdAndUserId(groupId, ownerId);

        // then
        then(groupMemberRepository).should().deleteByGroupId(groupId);
        then(groupRepository).should().delete(group);
    }

    @Test
    @DisplayName("deleteGroupByIdAndUserId - OWNER가 아니면 GroupPermissionDeniedException")
    void deleteGroup_permissionDenied() {
        // given
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long groupId = 10L;

        User owner = User.builder().id(ownerId).build();
        Group group = Group.builder()
                .id(groupId)
                .name("group")
                .owner(owner)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroupByIdAndUserId(groupId, otherUserId))
                .isInstanceOf(GroupPermissionDeniedException.class);

        then(groupMemberRepository).should(never()).deleteByGroupId(anyLong());
        then(groupRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("findGroupDetailById - OWNER로 조회하면 role은 OWNER")
    void findGroupDetail_owner() {
        // given
        Long ownerId = 1L;
        Long groupId = 10L;

        User owner = User.builder().id(ownerId).build();
        Group group = Group.builder()
                .id(groupId)
                .name("group")
                .owner(owner)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.countByGroupId(groupId)).willReturn(3L);

        // when
        GroupDetailResponse response = groupService.findGroupDetailById(groupId, ownerId);

        // then
        assertThat(response.getId()).isEqualTo(groupId);
        assertThat(response.getMemberCount()).isEqualTo(3L);
        assertThat(response.getMyRole()).isEqualTo(GroupMemberRole.OWNER);
    }

    @Test
    @DisplayName("findGroupDetailById - 멤버로 조회하면 membership role 사용")
    void findGroupDetail_member() {
        // given
        Long groupId = 10L;
        Long memberId = 2L;

        User owner = User.builder().id(1L).build();
        User memberUser = User.builder().id(memberId).build();

        Group group = Group.builder()
                .id(groupId)
                .name("group")
                .owner(owner)
                .build();

        GroupMember membership = GroupMember.builder()
                .id(100L)
                .group(group)
                .user(memberUser)
                .role(GroupMemberRole.MEMBER)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.countByGroupId(groupId)).willReturn(2L);
        given(groupMemberRepository.findByGroupIdAndUserId(groupId, memberId))
                .willReturn(Optional.of(membership));

        // when
        GroupDetailResponse response = groupService.findGroupDetailById(groupId, memberId);

        // then
        assertThat(response.getMyRole()).isEqualTo(GroupMemberRole.MEMBER);
    }

    @Test
    @DisplayName("findGroupDetailById - 비멤버는 GroupPermissionDeniedException")
    void findGroupDetail_notMember() {
        // given
        Long groupId = 10L;
        Long userId = 2L;

        User owner = User.builder().id(1L).build();

        Group group = Group.builder()
                .id(groupId)
                .name("group")
                .owner(owner)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.countByGroupId(groupId)).willReturn(1L);
        given(groupMemberRepository.findByGroupIdAndUserId(groupId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.findGroupDetailById(groupId, userId))
                .isInstanceOf(GroupPermissionDeniedException.class);
    }

    @Test
    @DisplayName("searchGroupsByUserId - membership 기준으로 그룹 리스트 조회")
    void searchGroupsByUserId_success() {
        // given
        Long userId = 1L;
        GroupSearchCondition condition = new GroupSearchCondition();
        condition.setKeyword("스터디");

        Pageable pageable = PageRequest.of(0, 10);
        User owner = User.builder().id(99L).build();
        Group group = Group.builder()
                .id(10L)
                .name("스터디 그룹")
                .owner(owner)
                .description("설명")
                .build();

        User user = User.builder().id(userId).build();

        GroupMember membership = GroupMember.builder()
                .id(100L)
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .build();

        Page<GroupMember> page = new PageImpl<>(List.of(membership), pageable, 1);

        given(groupMemberRepository.searchMembershipsByUserId(userId, "스터디", pageable))
                .willReturn(page);

        // when
        Page<GroupListResponse> result =
                groupService.searchGroupsByUserId(condition, userId, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        GroupListResponse first = result.getContent().get(0);
        assertThat(first.getId()).isEqualTo(10L);
        assertThat(first.getMyRole()).isEqualTo(GroupMemberRole.MEMBER);
    }

    @Test
    @DisplayName("searchGroups - 전체 그룹 검색")
    void searchGroups_success() {
        // given
        GroupSearchCondition condition = new GroupSearchCondition();
        condition.setKeyword("스터디");

        Pageable pageable = PageRequest.of(0, 10);

        User owner = User.builder().id(99L).build();
        Group group = Group.builder()
                .id(10L)
                .name("스터디 그룹")
                .description("설명")
                .owner(owner)
                .build();

        Page<Group> page = new PageImpl<>(List.of(group), pageable, 1);

        given(groupRepository.searchGroupsByCondition("스터디", pageable))
                .willReturn(page);

        // when
        Page<GroupListResponse> result = groupService.searchGroups(condition, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
    }
}
