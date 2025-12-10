package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentCreateRequest;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;

import java.util.List;

public interface ScheduleCommentService {
    ScheduleCommentResponse createComment(Long scheduleId, ScheduleCommentCreateRequest request);

    List<ScheduleCommentResponse> getCommentBySchedule(Long scheduleId);

    ScheduleCommentResponse updateComment(Long commentId, String newContent);

    void deleteComment(Long commentId);
}
