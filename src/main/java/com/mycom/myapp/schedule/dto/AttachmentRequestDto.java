package com.mycom.myapp.schedule.dto;

import com.mycom.myapp.schedule.entity.enums.FileType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AttachmentRequestDto {

    private Long scheduleId;
    private FileType fileType;
    private MultipartFile file; // 실제 업로드 파일

}
