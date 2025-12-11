package com.mycom.myapp.domain.schedule_extras.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCommentCreateRequest {

//    private Long scheduleId;
//    private Long userId;
    private String content;
}
