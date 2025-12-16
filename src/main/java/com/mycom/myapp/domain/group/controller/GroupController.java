package com.mycom.myapp.domain.group.controller;

import com.mycom.myapp.domain.group.dto.*;
import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    // 그룹 생성
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestParam("currentUserId")Long currentUserId,
            @RequestBody @Valid GroupCreateRequest request
    ) {
        GroupResponse response = groupService.createGroup(request, currentUserId);
        return ResponseEntity.ok(response);
    }
    // 그룹 수정
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody @Valid GroupUpdateRequest request
    ) {
        GroupResponse response =
                groupService.updateGroupByIdAndUserId(groupId, request, currentUserId);
        return ResponseEntity.ok(response);
    }
    // 그룹 삭제
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam Long currentUserId
    ) {
        groupService.deleteGroupByIdAndUserId(groupId, currentUserId);
        return ResponseEntity.noContent().build();
    }
    // 그룹 목록 조회
    @GetMapping
    public ResponseEntity<Page<GroupListResponse>> searchGroups(
            @ModelAttribute GroupSearchCondition condition,
            Pageable pageable
    ) {
        Page<GroupListResponse> result = groupService.searchGroups(condition, pageable);
        return ResponseEntity.ok(result);
    }
    // 그룹 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
    		@PathVariable("groupId") Long groupId,
            @RequestParam("currentUserId") Long currentUserId
    ) {
        GroupDetailResponse result = groupService.findGroupDetailById(groupId, currentUserId);
        return ResponseEntity.ok(result);
    }
    
    // 사용자별 그룹 리스트 조회
    @GetMapping("/users")
    public ResponseEntity<Page<GroupListResponse>> getUserGroups(
            @RequestParam("currentUserId") Long currentUserId,
            @ModelAttribute GroupSearchCondition condition,
            Pageable pageable
    ) {
        Page<GroupListResponse> result = groupService.searchGroupsByUserId(condition, currentUserId, pageable);
        return ResponseEntity.ok(result);
    }

}
