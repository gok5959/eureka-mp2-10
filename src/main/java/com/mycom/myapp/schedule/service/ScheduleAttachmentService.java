package com.mycom.myapp.schedule.service;

import com.mycom.myapp.schedule.dto.AttachmentRequestDto;
import com.mycom.myapp.schedule.dto.AttachmentResponseDto;

import java.util.List;

public interface ScheduleAttachmentService {

    AttachmentResponseDto uploadAttachment(AttachmentRequestDto dto);

    List<AttachmentResponseDto> getAttachments(Long scheduleId);

    void deleteAttachment(Long id);
}
