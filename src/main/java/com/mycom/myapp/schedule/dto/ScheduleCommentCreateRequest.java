package com.mycom.myapp.schedule.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCommentCreateRequest {
    private Long userId;
    private String content;
}
