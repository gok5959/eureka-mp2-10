package com.mycom.myapp.schedule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//@Entity
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor

@Entity
@Table(name = "schedule_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ScheduleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId; // schedules FK

    @Column(nullable = false)
    private Long userId; // users FK

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updated_at;



}
