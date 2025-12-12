package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.common.config.storage.GcpStorageService;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentRequestDto;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentResponseDto;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleAttachment;
import com.mycom.myapp.domain.schedule_extras.repository.ScheduleAttachmentRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleAttachmentServiceImpl implements ScheduleAttachmentService {

    private final ScheduleAttachmentRepository attachmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final GcpStorageService gcpStorageService;

    @Value("${gcp.bucket}")
    private String bucketName;

    @Override
    public AttachmentResponseDto uploadAttachment(Long scheduleId, Long userId, AttachmentRequestDto dto) {
        // 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        MultipartFile file = dto.getFile();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        try {
            // 파일 바이트 읽기
            byte[] bytes = file.getBytes();

            // GCP 업로드용 파일명 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            String gcsPath = gcpStorageService.uploadFile(
                    fileName,
                    bytes,
                    file.getContentType()
            );

//            String gcsPath = "attachments/" + fileName;
//
//            // gcp 업로드
//            gcpStorageService.uploadFile(
//                    gcsPath,
//                    bytes,
//                    file.getContentType()
//            );

            // 프론트 이미지 미리보기를 위한 url
            String url = "https://storage.googleapis.com/" + bucketName + "/" + fileName;


            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            ScheduleAttachment attachment = ScheduleAttachment.builder()
                    .schedule(schedule)
                    .user(user)
                    .fileType(dto.getFileType())
                    .fileUrl(url)
                    .gcsPath(gcsPath)
                    .originalName(originalName)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .build();

            ScheduleAttachment saved = attachmentRepository.save(attachment);

            return AttachmentResponseDto.builder()
                    .id(saved.getId())
                    .scheduleId(scheduleId)
                    .fileType(saved.getFileType())
                    .fileUrl(saved.getFileUrl())
                    .originalName(saved.getOriginalName())
                    .fileSize(saved.getFileSize())
                    .contentType(saved.getContentType())
                    .createdAt(saved.getCreatedAt())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAttachments(Long scheduleId) {

        return attachmentRepository.findBySchedule_Id(scheduleId)
                .stream()
                .map(a -> AttachmentResponseDto.builder()
                        .id(a.getId())
                        .scheduleId(scheduleId)
                        .fileType(a.getFileType())
                        .fileUrl(a.getFileUrl())
                        .originalName(a.getOriginalName())
                        .fileSize(a.getFileSize())
                        .contentType(a.getContentType())
                        .createdAt(a.getCreatedAt())
                        .build()
                    ).toList();
    }

    @Override
    public void deleteAttachment(Long id, Long userId) {
    ScheduleAttachment attachment =
            attachmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다"));

    if (!attachment.getUser().getId().equals(userId)) {
        throw new IllegalArgumentException("자신이 업로드한 파일만 삭제할 수 있습니다");
    }

    // gcp 에서 파일 삭제
        gcpStorageService.deleteFile(attachment.getGcsPath());

    // db 에서 파일 삭제
        attachmentRepository.delete(attachment);
    }
}
