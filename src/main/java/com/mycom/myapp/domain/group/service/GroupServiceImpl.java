package com.mycom.myapp.domain.group.service;

import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.dto.*;
import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.group.entity.GroupMember;
import com.mycom.myapp.domain.group.exception.GroupNotFoundException;
import com.mycom.myapp.domain.group.exception.GroupPermissionDeniedException;
import com.mycom.myapp.domain.group.repository.GroupMemberRepository;
import com.mycom.myapp.domain.group.repository.GroupRepository;
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
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;


    // Tx : Group 생성 시, 그룹을 생성한 OWNER 는 Group Member 에 항상 포함되어야 함
    @Override
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found in Group Creation : " + ownerId));

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        Group saved = groupRepository.save(group);

        GroupMember groupMember = GroupMember.builder()
                .group(saved)
                .user(owner)
                .role(GroupMemberRole.OWNER)
                .build();

        groupMemberRepository.save(groupMember);

        return GroupResponse.from(saved);
    }

    @Override
    @Transactional
    public GroupResponse updateGroupByIdAndUserId(Long groupId, GroupUpdateRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found in Group Update : " + currentUserId));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group Not Found in Group Update : " + groupId));

        if (!group.getOwner().getId().equals(currentUserId)) {
            throw new GroupPermissionDeniedException("Current User " + currentUserId + " is not allowed to update this group : " + groupId);
        }

        group.update(request.getName(), request.getDescription());

        return GroupResponse.from(group);
    }

    @Override
    @Transactional
    public void deleteGroupByIdAndUserId(Long groupId, Long currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group Not Found in Group deletion : " + groupId));

        if (!group.getOwner().getId().equals(currentUserId)) {
            throw new GroupPermissionDeniedException("Current User " + currentUserId + " is not allowed to delete this group : " + groupId);
        }

        groupMemberRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDetailResponse findGroupDetailById(Long groupId, Long currentUserId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group Not Found : " + groupId));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        GroupMemberRole currentUserRole;

        // 1. OWNER 인지 먼저 체크
        if (group.getOwner().getId().equals(currentUserId)) {
            currentUserRole = GroupMemberRole.OWNER;
        } else {
            // 2. OWNER가 아니라면, 그룹 멤버인지 확인
            GroupMember membership = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                    .orElseThrow(() ->
                            new GroupPermissionDeniedException(
                                    "User " + currentUserId + " is not a member of group " + groupId
                            ));

            // 3. 멤버라면 그 멤버의 role 사용 (MEMBER, ADMIN 등 확장 가능)
            currentUserRole = membership.getRole();
        }
        return GroupDetailResponse.from(group, currentUserRole, memberCount);
    }

    @Override
    public Page<GroupListResponse> searchGroupsByUserId(GroupSearchCondition condition, Long userId, Pageable pageable) {
        String keyword = condition != null ? condition.getKeyword() : null;
        Page<GroupMember> memberships = groupMemberRepository.searchMembershipsByUserId(userId, keyword, pageable);

        return memberships.map(membership -> GroupListResponse.from(membership.getGroup(), membership.getRole()));
    }

    @Override
    public Page<GroupListResponse> searchGroups(GroupSearchCondition condition, Pageable pageable) {
        String keyword = condition != null ? condition.getKeyword() : null;
        Page<Group> groups = groupRepository.searchGroupsByCondition(keyword, pageable);
        return groups.map(GroupListResponse::from);
    }
}
