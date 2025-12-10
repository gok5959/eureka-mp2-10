package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.domain.schedule_extras.dto.AttachmentRequestDto;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentResponseDto;

import java.util.List;

public interface ScheduleAttachmentService {

    AttachmentResponseDto uploadAttachment(AttachmentRequestDto dto);

    List<AttachmentResponseDto> getAttachments(Long scheduleId);

    void deleteAttachment(Long id);
}
