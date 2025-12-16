package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import com.mycom.myapp.domain.schedule_extras.repository.ScheduleCommentRepository;
import com.mycom.myapp.domain.user.entity.User;

import com.mycom.myapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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

    @DisplayName("정상적인 요청으로 댓글 생성하면 ID가 포함된 응답을 반환한다.")
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


    @DisplayName("특정 일정에 등록된 모든 댓글을 조회할 수 있다.")
    @Test
    void testGetComments() {
        // given
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder().id(scheduleId).build();
        User user = User.builder().id(2L).build();

        ScheduleComment comment1 = ScheduleComment.builder()
                .id(1L).schedule(schedule).user(user).content("댓글1")
                .createdAt(LocalDateTime.now()).build();

        ScheduleComment comment2 = ScheduleComment.builder()
                .id(2L).schedule(schedule).user(user).content("댓글2")
                .createdAt(LocalDateTime.now()).build();

        when(commentRepository.findBySchedule_IdOrderByCreatedAtAsc(scheduleId))
                .thenReturn(java.util.List.of(comment1, comment2));

        // when
        var result = commentService.getComments(scheduleId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("댓글1");
        assertThat(result.get(1).getContent()).isEqualTo("댓글2");

        verify(commentRepository, times(1)).findBySchedule_IdOrderByCreatedAtAsc(scheduleId);
    }

    @DisplayName("댓글 작성자가 요청하면 댓글 내용을 성공적으로 수정할 수 있다.")
    @Test
    void testUpdateCommentSuccess() {
        // given
        Long commentId = 10L;
        Long userId = 2L;

        User user = User.builder().id(userId).build();
        Schedule schedule = Schedule.builder().id(1L).build();

        ScheduleComment comment = ScheduleComment.builder()
                .id(commentId)
                .schedule(schedule)
                .user(user)
                .content("old content")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenReturn(comment);

        // when
        ScheduleCommentResponse response = commentService.updateComment(1L, commentId, userId, "new content");

        // then
        assertThat(response.getContent()).isEqualTo("new content");
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any());
    }


    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 예외가 발생한다.")
    @Test
    void testUpdateComment_NotFound() {
        Long commentId = 999L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.updateComment(1L, commentId, 1L, "update")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 수정하면 예외가 발생한다.")
    @Test
    void testUpdateComment_NotOwner() {
        Long commentId = 10L;

        User owner = User.builder().id(1L).build(); // 댓글 실제 작성자
        User other = User.builder().id(2L).build(); // 수정 시도자

        Schedule schedule = Schedule.builder().id(1L).build();

        ScheduleComment comment = ScheduleComment.builder()
                .id(commentId)
                .schedule(schedule)
                .user(owner)
                .content("content")
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() ->
                commentService.updateComment(1L, commentId, other.getId(), "changed")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신의 댓글만 수정할 수 있습니다");
    }

    @DisplayName("댓글 작성자가 요청하면 댓글을 성공적으로 삭제할 수 있다.")
    @Test
    void testDeleteCommentSuccess() {
        Long commentId = 5L;
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        Schedule schedule = Schedule.builder().id(1L).build();

        ScheduleComment comment = ScheduleComment.builder()
                .id(commentId)
                .user(user)
                .schedule(schedule)
                .content("삭제할 댓글")
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(1L, commentId, userId);

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @DisplayName("존재하지 않는 댓글을 삭제하려 하면 예외가 발생한다.")
    @Test
    void testDeleteComment_NotFound() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.deleteComment(1L, 123L, 1L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 삭제하려 하면 예외가 발생한다.")
    @Test
    void testDeleteComment_NotOwner() {
        User owner = User.builder().id(1L).build();
        User other = User.builder().id(2L).build();

        Schedule schedule = Schedule.builder().id(1L).build();

        ScheduleComment comment = ScheduleComment.builder()
                .id(10L)
                .user(owner)
                .schedule(schedule)
                .content("댓글")
                .build();

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() ->
                commentService.deleteComment(1L, 10L, other.getId())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신의 댓글만 삭제할 수 있습니다");
    }



}
