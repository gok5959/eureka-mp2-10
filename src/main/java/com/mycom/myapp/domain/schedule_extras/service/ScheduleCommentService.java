package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentCreateRequest;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;

import java.util.List;

public interface ScheduleCommentService {
//    ScheduleCommentResponse createComment(Long scheduleId, ScheduleCommentCreateRequest request);
//
//    List<ScheduleCommentResponse> getCommentBySchedule(Long scheduleId);
//
//    ScheduleCommentResponse updateComment(Long commentId, String newContent);
//
//    void deleteComment(Long commentId);

    ScheduleCommentResponse createComment(Long scheduleId, Long userId, String content);

    List<ScheduleCommentResponse> getComments(Long scheduleId);

    ScheduleCommentResponse updateComment(Long scheduleId, Long commentId, Long userId, String content);

    void deleteComment(Long scheduleId, Long commentId, Long userId);
}
