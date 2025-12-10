package com.mycom.myapp.domain.schedule_extras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ScheduleCommentResponse {
    private Long id;
    private Long scheduleId;
    private Long userId;
    private String content;
    private String createdAt;
    private String updatedAt;
}
