package com.mycom.myapp.domain.schedule_extras.entity;

import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "schedule_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ScheduleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 댓글 → 스케줄 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // 댓글 → 유저 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



}
