package com.mycom.myapp.domain.group.service;

import com.mycom.myapp.domain.group.GroupMemberRole;
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
import com.mycom.myapp.domain.user.UserRole;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<GroupMemberResponse> searchGroupMembers(
            Long groupId,
            GroupMemberSearchCondition searchCondition,
            Pageable pageable
    ) {
        boolean exists = groupRepository.existsById(groupId);
        if(!exists) {
            throw new GroupNotFoundException("그룹을 찾을 수 없습니다: " + groupId);
        }

        String keyword = (searchCondition != null) ? searchCondition.getKeyword() : null;
        GroupMemberRole role = (searchCondition != null) ? searchCondition.getRole() : null;

        Page<GroupMember> members = groupMemberRepository.searchMembers(groupId, keyword, role, pageable);
        return members.map(GroupMemberResponse::from);
    }


    @Override
    @Transactional
    public GroupMemberResponse addGroupMember(Long groupId, Long userId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 중복 체크 로직
        boolean exists = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
        if (exists) {
            throw new GroupMemberAddDuplicateException("이미 그룹에 가입된 사용자입니다. groupId=" + groupId + ", userId=" + userId);
        }

        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .group(group)
                .role(GroupMemberRole.MEMBER)
                .build();

        GroupMember saved = groupMemberRepository.save(groupMember);

        return GroupMemberResponse.from(saved);
    }


    @Override
    @Transactional
    public void deleteGroupMember(Long groupId, Long targetUserId, Long currentUserId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다: " + groupId));

        // 삭제할 멤버 존재 확인
        boolean exists = groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId);
        if (!exists) {
            throw new GroupMemberNotFoundException(
                    "그룹 구성원을 찾을 수 없습니다. groupId=" + groupId + ", userId=" + targetUserId
            );
        }

        boolean isSelfLeave = currentUserId.equals(targetUserId);

        if (!isSelfLeave) {
            // 추방이면 권한 체크: OWNER만 가능
            if (!group.getOwner().getId().equals(currentUserId)) {
                throw new GroupPermissionDeniedException(
                        "사용자 " + currentUserId + "는 구성원 " + targetUserId + "을(를) 제거할 권한이 없습니다."
                );
            }
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Override
    @Transactional
    public GroupMemberResponse addGroupMemberByEmail(Long groupId, String email) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다: " + groupId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 중복 체크 로직
        boolean exists = groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId());
        if (exists) {
            throw new GroupMemberAddDuplicateException("이미 그룹에 가입된 사용자입니다. groupId=" + groupId + ", userId=" + user.getId());
        }

        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .group(group)
                .role(GroupMemberRole.MEMBER)
                .build();

        GroupMember saved = groupMemberRepository.save(groupMember);

        return GroupMemberResponse.from(saved);
    }


}
