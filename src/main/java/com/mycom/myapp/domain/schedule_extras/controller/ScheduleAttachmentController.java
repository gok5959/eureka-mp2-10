package com.mycom.myapp.domain.schedule_extras.controller;

import com.mycom.myapp.domain.schedule_extras.dto.AttachmentRequestDto;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentResponseDto;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleAttachment;
import com.mycom.myapp.domain.schedule_extras.service.ScheduleAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mycom.myapp.domain.schedule_extras.entity.enums.FileType;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules/{scheduleId}/attachments")
public class ScheduleAttachmentController {

    private final ScheduleAttachmentService attachmentService;

    /**
     * 첨부파일 업로드
     * POST /schedules/{scheduleId}/attachments?userId=1
     */
    @PostMapping
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long scheduleId,
            @RequestParam Long userId,
            @RequestParam("file")MultipartFile file,
            @RequestParam("fileType") String fileType
    ) {

        // fileType 검증 추가
        FileType type;
        try {
            type = FileType.valueOf(fileType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 파일 타입입니다.");
        }

        AttachmentRequestDto dto = AttachmentRequestDto.builder()
                .file(file)
                .fileType(type)
                .build();

        AttachmentResponseDto response =
                attachmentService.uploadAttachment(scheduleId, userId, dto);

        return ResponseEntity.ok(response);

    }

    /**
     * 첨부파일 조회
     * GET /schedules/{scheduleId}/attachments
     */
    @GetMapping
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(attachmentService.getAttachments(scheduleId));
    }

    /**
     * 첨부파일 삭제
     * DELETE /schedules/{scheduleId}/attachments/{attachmentId}?userId=1
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long scheduleId,
            @PathVariable Long attachmentId,
            @RequestParam Long userId
    ) {

//        Long currentUserId = authUser.getId();
//        attachmentService.deleteAttachment(attachmentId, currentUserId);

        attachmentService.deleteAttachment(attachmentId, userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
