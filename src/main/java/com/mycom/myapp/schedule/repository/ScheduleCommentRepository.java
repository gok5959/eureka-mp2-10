package com.mycom.myapp.schedule.repository;

import com.mycom.myapp.schedule.entity.ScheduleComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCommentRepository extends JpaRepository<ScheduleComment, Long> {
    List<ScheduleComment> findByScheduledId(Long scheduleId);
}
