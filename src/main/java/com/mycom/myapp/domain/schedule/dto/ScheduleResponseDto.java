package com.mycom.myapp.domain.schedule.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.schedule.entity.ScheduleStatus;
import com.mycom.myapp.domain.schedule.entity.Schedule;

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
    private Long groupId;
    
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String placeName;
    private ScheduleStatus status;
    private LocalDateTime voteDeadlineAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    // 수정 필요
//    private List<CommentDto~> comments;

    // builder로 schedule 만들고
    // 바로 SchedulerResponseDto.fromEntity(schedule)로 선언 가능
    // 리스트/달력용 (댓글 없이)
    public static ScheduleResponseDto fromEntity(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .placeName(schedule.getPlaceName())
                .status(schedule.getStatus())
                .voteDeadlineAt(schedule.getVoteDeadlineAt())
                .createdAt(schedule.getCreatedAt())   // BaseEntity에서 상속
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

//    // 상세용 (댓글까지)
//    public static ScheduleResponseDto fromEntityWithComments(
//            Schedule schedule,
//            //List<CommentResponseDto> comments
//    ) {
//        return ScheduleResponseDto.builder()
//                .id(schedule.getId())
//                .title(schedule.getTitle())
//                .description(schedule.getDescription())
//                .startAt(schedule.getStartAt())
//                .endAt(schedule.getEndAt())
//                .placeName(schedule.getPlaceName())
//                .status(schedule.getStatus())
//                .voteDeadlineAt(schedule.getVoteDeadlineAt())
//                .createdAt(schedule.getCreatedAt())
//                .updatedAt(schedule.getUpdatedAt())
//                .comments(comments)
//                .build();
//    }
}
