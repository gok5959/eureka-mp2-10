package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.common.config.storage.GcpStorageService;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentRequestDto;
import com.mycom.myapp.domain.schedule_extras.dto.AttachmentResponseDto;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleAttachment;
import com.mycom.myapp.domain.schedule_extras.entity.enums.FileType;
import com.mycom.myapp.domain.schedule_extras.repository.ScheduleAttachmentRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
public class ScheduleAttachmentServiceTest {

    @InjectMocks
    ScheduleAttachmentServiceImpl attachmentService;

    @Mock
    ScheduleAttachmentRepository attachmentRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GcpStorageService gcpStorageService;

    Schedule schedule;
    User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        schedule = Schedule.builder().id(1L).build();
        user = User.builder().id(1L).build();
    }

    @DisplayName("첨부파일 업로드 성공")
    @Test
    void testUploadAttachment() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test data".getBytes()
        );

        AttachmentRequestDto dto = AttachmentRequestDto.builder()
                .file(file)
                .fileType(FileType.IMAGE)
                .build();

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gcpStorageService.uploadFile(anyString(), any(), anyString()))
                .thenReturn("gs://bucket/test.png");

        ScheduleAttachment saved = ScheduleAttachment.builder()
                .id(10L)
                .schedule(schedule)
                .user(user)
                .fileType(FileType.IMAGE)
                .fileUrl("gs://bucket/test.png")
                .originalName("test.png")
                .fileSize(8L)
                .contentType("image/png")
                .build();

        when(attachmentRepository.save(any())).thenReturn(saved);

        // when
        AttachmentResponseDto result =
                attachmentService.uploadAttachment(1L, 1L, dto);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFileUrl()).contains("gs://");
        verify(attachmentRepository, times(1)).save(any());
    }

    @DisplayName("첨부파일 목록 조회")
    @Test
    void testGetAttachments() {
        ScheduleAttachment a1 = ScheduleAttachment.builder()
                .id(1L).fileType(FileType.IMAGE).fileUrl("url1").schedule(schedule)
                .build();
        ScheduleAttachment a2 = ScheduleAttachment.builder()
                .id(2L).fileType(FileType.FILE).fileUrl("url2").schedule(schedule)
                .build();

        when(attachmentRepository.findBySchedule_Id(1L))
                .thenReturn(List.of(a1, a2));

        // when
        List<AttachmentResponseDto> result = attachmentService.getAttachments(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(attachmentRepository, times(1)).findBySchedule_Id(1L);
    }

    @DisplayName("첨부파일 삭제 성공")
    @Test
    void testDeleteAttachmentSuccess() {
        ScheduleAttachment attachment = ScheduleAttachment.builder()
                .id(10L)
                .user(user)
                .schedule(schedule)
                .fileUrl("gs://bucket/test.png")
                .build();

        when(attachmentRepository.findById(10L)).thenReturn(Optional.of(attachment));

        // when
        attachmentService.deleteAttachment(10L, 1L);

        // then
        verify(gcpStorageService, times(1)).deleteFile("gs://bucket/test.png");
        verify(attachmentRepository, times(1)).delete(attachment);
    }

    @DisplayName("첨부파일 삭제 실패 - 다른 사용자가 삭제 요청")
    @Test
    void testDeleteAttachmentFail_NotOwner() {
        User other = User.builder().id(99L).build();

        ScheduleAttachment attachment = ScheduleAttachment.builder()
                .id(10L)
                .user(user)  // 실제 파일 업로더 = 1번 유저
                .fileUrl("gs://bucket/test.png")
                .build();

        when(attachmentRepository.findById(10L)).thenReturn(Optional.of(attachment));

        // then
        assertThatThrownBy(() -> attachmentService.deleteAttachment(10L, other.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신이 업로드한 파일만 삭제할 수 있습니다");
    }

    @DisplayName("첨부파일 삭제 실패 - 파일 존재하지 않음")
    @Test
    void testDeleteAttachmentFail_NotFound() {
        when(attachmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.deleteAttachment(10L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("첨부파일을 찾을 수 없습니다");
    }
}

