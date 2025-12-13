package com.mycom.myapp.domain.group.service;

import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.dto.GroupMemberResponse;
import com.mycom.myapp.domain.group.dto.GroupMemberSearchCondition;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupMemberService {
    // 그룹별 멤버 조회
    Page<GroupMemberResponse> searchGroupMembers(Long groupId, GroupMemberSearchCondition searchCondition, Pageable pageable);
    // 멤버 추가
    GroupMemberResponse addGroupMember(Long groupId, Long userId);
    // 멤버 제거(탈퇴)
    void deleteGroupMember(Long groupId, Long targetUserId, Long currentUserId);
}
