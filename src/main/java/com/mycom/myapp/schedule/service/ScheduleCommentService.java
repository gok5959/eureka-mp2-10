package com.mycom.myapp.schedule.service;

import com.mycom.myapp.schedule.dto.ScheduleCommentCreateRequest;
import com.mycom.myapp.schedule.dto.ScheduleCommentResponse;

import java.util.List;

public interface ScheduleCommentService {
    ScheduleCommentResponse createComment(Long scheduleId, ScheduleCommentCreateRequest request);

    List<ScheduleCommentResponse> getCommentBySchedule(Long scheduleId);

    ScheduleCommentResponse updateComment(Long commentId, String newContent);

    void deleteComment(Long commentId);
}
