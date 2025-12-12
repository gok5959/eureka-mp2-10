package com.mycom.myapp.domain.schedule_extras.service;

import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.schedule_extras.dto.ScheduleCommentResponse;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleComment;
import com.mycom.myapp.domain.schedule_extras.repository.ScheduleCommentRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommentServiceImpl implements ScheduleCommentService {

    private final ScheduleCommentRepository commentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Override
    public ScheduleCommentResponse createComment(Long scheduleId, Long userId, String content) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ScheduleComment comment = ScheduleComment.builder()
                .schedule(schedule)
                .user(user)
                .content(content)
//                .createdAt(LocalDateTime.now())
                .build();

        ScheduleComment saved = commentRepository.save(comment);

        return ScheduleCommentResponse.builder()
                .id(saved.getId())
                .scheduleId(scheduleId)
                .userId(userId)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleCommentResponse> getComments(Long scheduleId) {
        return commentRepository.findBySchedule_IdOrderByCreatedAtAsc(scheduleId)
                .stream()
                .map( c -> ScheduleCommentResponse.builder()
                        .id(c.getId())
                        .scheduleId(scheduleId)
                        .userId(c.getUser().getId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public ScheduleCommentResponse updateComment(Long scheduleId, Long commentId, Long userId, String content) {
        ScheduleComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 댓글만 수정할 수 있습니다");
        }

        comment.setContent(content);
//        comment.setUpdatedAt(LocalDateTime.now());

        ScheduleComment updated = commentRepository.save(comment);

        return ScheduleCommentResponse.builder()
                .id(updated.getId())
                .scheduleId(updated.getSchedule().getId())
                .userId(updated.getUser().getId())
                .content(updated.getContent())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteComment(Long scheduleId, Long commentId, Long userId) {
        ScheduleComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 댓글만 삭제할 수 있습니다");
        }

        commentRepository.delete(comment);
    }
}
