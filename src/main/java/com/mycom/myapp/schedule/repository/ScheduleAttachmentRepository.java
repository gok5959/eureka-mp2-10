package com.mycom.myapp.schedule.repository;

import com.mycom.myapp.schedule.entity.ScheduleAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleAttachmentRepository extends JpaRepository<ScheduleAttachment, Long> {
    List<ScheduleAttachment> findByScheduleId(Long scheduleId);
}
