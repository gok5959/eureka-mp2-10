package com.mycom.myapp.domain.group.controller;

import com.mycom.myapp.domain.group.dto.GroupMemberResponse;
import com.mycom.myapp.domain.group.dto.GroupMemberSearchCondition;
import com.mycom.myapp.domain.group.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @GetMapping
    public ResponseEntity<Page<GroupMemberResponse>> searchGroupMember(
            @PathVariable("groupId") Long groupId,
            @ModelAttribute GroupMemberSearchCondition condition,
            Pageable pageable
    ) {
        return ResponseEntity.ok(groupMemberService.searchGroupMembers(groupId, condition, pageable));
    }

    @PostMapping
    public ResponseEntity<GroupMemberResponse> addGroupMember(
            @PathVariable("groupId") Long groupId,
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(groupMemberService.addGroupMember(groupId, userId));
    }

    @PostMapping("/email")
    public ResponseEntity<GroupMemberResponse> addGroupMemberByEmail(
            @PathVariable("groupId") Long groupId,
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(groupMemberService.addGroupMemberByEmail(groupId, email));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> removeGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            @RequestParam Long currentUserId
    ) {
        groupMemberService.deleteGroupMember(groupId, targetUserId, currentUserId);
        return ResponseEntity.noContent().build();
    }

}
