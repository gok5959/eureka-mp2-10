package com.mycom.myapp.schedule.dto;

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
