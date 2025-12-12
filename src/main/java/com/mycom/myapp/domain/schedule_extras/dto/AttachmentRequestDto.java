package com.mycom.myapp.domain.schedule_extras.dto;

import com.mycom.myapp.domain.schedule_extras.entity.enums.FileType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AttachmentRequestDto {

//    private Long scheduleId;
    private FileType fileType;
    private MultipartFile file; // 실제 업로드 파일

}
