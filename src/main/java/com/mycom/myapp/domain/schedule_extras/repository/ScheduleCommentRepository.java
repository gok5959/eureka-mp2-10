package com.mycom.myapp.domain.schedule_extras.repository;

import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCommentRepository extends JpaRepository<ScheduleComment, Long> {

    // 일정(schedule_id) 기준으로 댓글 가져오기
    List<ScheduleComment> findBySchedule_IdOrderByCreatedAtAsc(Long scheduleId);
}
