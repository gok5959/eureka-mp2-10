package com.mycom.myapp.domain.schedule_extras.repository;

import com.mycom.myapp.domain.schedule_extras.entity.ScheduleAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleAttachmentRepository extends JpaRepository<ScheduleAttachment, Long> {
    List<ScheduleAttachment> findBySchedule_Id(Long scheduleId);

}
