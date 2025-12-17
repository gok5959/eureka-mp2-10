package com.mycom.myapp.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mycom.myapp.domain.participation.dto.ParticipationStatusResponseDto;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.entity.ScheduleStatus;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentResponseDto;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 일정 요청 받을 때(서버 -> 클라이언트)의 Dto
public class ScheduleResponseDto {
    private Long id;
    private String title;
    private String description;

    private Long ownerId;
    private String ownerName; // 추가
    private Long groupId;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String placeName;
    private ScheduleStatus status;
    private LocalDateTime voteDeadlineAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ScheduleCommentResponse> comments;
    private List<AttachmentResponseDto> attachments;
    
    private List<ParticipationStatusResponseDto> participations;

    // 목록/달력용 (댓글, 첨부 X)
    public static ScheduleResponseDto fromEntity(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .ownerId(schedule.getOwner() != null ? schedule.getOwner().getId() : null)
                .groupId(schedule.getGroup() != null ? schedule.getGroup().getId() : null)
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .placeName(schedule.getPlaceName())
                .status(schedule.getStatus())
                .voteDeadlineAt(schedule.getVoteDeadlineAt())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    // 상세용 (댓글 + 첨부파일)
    public static ScheduleResponseDto fromEntityWithDetails(
            Schedule schedule,
            List<ScheduleComment> comments
    ) {
        List<ScheduleCommentResponse> commentDtos = comments.stream()
                .map(ScheduleCommentResponse::fromEntity)
                .collect(Collectors.toList());

        List<AttachmentResponseDto> attachmentDtos = schedule.getAttachments().stream()
                .map(a -> AttachmentResponseDto.builder()
                        .id(a.getId())
                        .fileType(a.getFileType())
                        .fileUrl(a.getFileUrl())
                        .originalName(a.getOriginalName())
                        .fileSize(a.getFileSize())
                        .contentType(a.getContentType())
                        .createdAt(a.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        // ✅ 참여자 리스트 매핑
        List<ParticipationStatusResponseDto> participationDtos = schedule.getParticipations().stream()
                .map(ParticipationStatusResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .ownerId(schedule.getOwner() != null ? schedule.getOwner().getId() : null)
                .ownerName(schedule.getOwner() != null ? schedule.getOwner().getName() : null)
                .groupId(schedule.getGroup() != null ? schedule.getGroup().getId() : null)
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .placeName(schedule.getPlaceName())
                .status(schedule.getStatus())
                .voteDeadlineAt(schedule.getVoteDeadlineAt())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .comments(commentDtos)
                .attachments(attachmentDtos)
                .participations(participationDtos)   // ✅ 여기!
                .build();
    }

}
