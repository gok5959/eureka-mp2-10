package com.mycom.myapp.schedule.dto;

import com.mycom.myapp.schedule.entity.enums.FileType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AttachmentResponseDto {
    private Long id;
    private Long scheduleId;
    private FileType fileType;
    private String fileUrl;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
}
