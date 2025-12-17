package com.mycom.myapp.domain.schedule_extras.dto;

import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ScheduleCommentResponse {
    private Long id;
    private Long scheduleId;
    private Long userId;
    // 추가
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleCommentResponse fromEntity(ScheduleComment c) {
        return ScheduleCommentResponse.builder()
                .id(c.getId())
                .scheduleId(c.getSchedule().getId())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
