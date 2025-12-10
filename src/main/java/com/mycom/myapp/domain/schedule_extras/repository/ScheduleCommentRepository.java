package com.mycom.myapp.domain.schedule_extras.repository;

import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCommentRepository extends JpaRepository<ScheduleComment, Long> {
    List<ScheduleComment> findByScheduledId(Long scheduleId);
}
