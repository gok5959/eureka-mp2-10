package com.mycom.myapp.domain.schedule_extras.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import com.mycom.myapp.domain.schedule_extras.repository.ScheduleCommentRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

class ScheduleCommentServiceTest {

    @InjectMocks
    ScheduleCommentServiceImpl commentService;

    @Mock
    ScheduleCommentRepository commentRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    UserRepository userRepository;

    Schedule schedule;
    User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 가짜 user, schedule 준비
        user = User.builder().id(1L).build();
        schedule = Schedule.builder().id(1L).build();
    }

    @Test
    void testCreateComment() {
        // given
        String content = "테스트 댓글";

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ScheduleComment fake = ScheduleComment.builder()
                .id(10L)
                .content(content)
                .user(user)
                .schedule(schedule)
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.save(any(ScheduleComment.class))).thenReturn(fake);

        // when
        ScheduleCommentResponse result =
                commentService.createComment(1L, 1L, content);

        // then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("테스트 댓글", result.getContent());
        verify(commentRepository, times(1)).save(any());
    }
}
