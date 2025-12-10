package com.mycom.myapp.schedule.entity;

import com.mycom.myapp.schedule.entity.enums.FileType;
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

public class ScheduleAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false)
    private String fileUrl;

    private String originalName;
    private Long fileSize;
    private String contentType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
