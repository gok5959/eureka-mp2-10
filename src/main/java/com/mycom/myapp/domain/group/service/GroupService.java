package com.mycom.myapp.domain.group.service;

import com.mycom.myapp.common.dto.PageResponse;
import com.mycom.myapp.domain.group.dto.*;

import java.awt.print.Pageable;

public interface GroupService {

    GroupResponse createGroup(GroupCreateRequest request, Long ownerId);

    GroupResponse updateGroupByIdAndUserId(Long groupId,
                                           GroupUpdateRequest request,
                                           Long currentUserId);

    void deleteGroupByIdAndUserId(Long groupId, Long currentUserId);

    PageResponse<GroupListResponse> findGroupsByUserId(GroupSearchCondition condition,
                                                           Long userId,
                                                           Pageable pageable);

    GroupDetailResponse findGroupDetailByIdAndUserId(Long groupId, Long userId);

    PageResponse<GroupMemberResponse> findGroupMembersByGroupId(Long groupId,
                                                                Pageable pageable);
}
