package com.mycom.myapp.domain.schedule_extras.controller;

import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentCreateRequest;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentUpdateRequest;
import com.mycom.myapp.domain.schedule_extras.service.ScheduleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules/{scheduleId}/comments")
@RequiredArgsConstructor
public class ScheduleCommentController {

    private final ScheduleCommentService commentService;

    // 댓글 생성
    @PostMapping
    public ResponseEntity<ScheduleCommentResponse> createComment(
            @PathVariable Long scheduleId,
            @RequestParam Long userId,
            @RequestBody ScheduleCommentCreateRequest request
    ) {
        ScheduleCommentResponse response =
                commentService.createComment(scheduleId, userId, request.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<List<ScheduleCommentResponse>> getComments(
            @PathVariable Long scheduleId
    ) {
        List<ScheduleCommentResponse> response =
                commentService.getComments(scheduleId);

        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ScheduleCommentResponse> updateComment(
            @PathVariable Long scheduleId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody ScheduleCommentUpdateRequest request
    ) {
        ScheduleCommentResponse response =
                commentService.updateComment(scheduleId, commentId, userId,request.getContent());

        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ScheduleCommentResponse> deleteComment(
            @PathVariable Long scheduleId,
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentService.deleteComment(scheduleId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
