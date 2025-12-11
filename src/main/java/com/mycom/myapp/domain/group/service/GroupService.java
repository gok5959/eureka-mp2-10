package com.mycom.myapp.domain.group.service;

import com.mycom.myapp.domain.group.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface GroupService {

    GroupResponse createGroup(GroupCreateRequest request, Long ownerId);

    GroupResponse updateGroupByIdAndUserId(Long groupId,
                                           GroupUpdateRequest request,
                                           Long currentUserId);

    void deleteGroupByIdAndUserId(Long groupId, Long currentUserId);


    GroupDetailResponse findGroupDetailById(Long groupId, Long currentUserId);

    Page<GroupListResponse> searchGroupsByUserId(GroupSearchCondition condition,
                                                 Long userId,
                                                 Pageable pageable);

    Page<GroupListResponse> searchGroups(GroupSearchCondition condition,
                                         Pageable pageable);
}
