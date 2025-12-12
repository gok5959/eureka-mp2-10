package com.mycom.myapp.domain.schedule_extras.entity;

import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule_extras.entity.enums.FileType;
import com.mycom.myapp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    // 프론트용 (https)
    @Column(nullable = false)
    private String fileUrl;

    // GCP 삭제용 (ex: attachments/uuid_file.png)
    @Column(nullable = false)
    private String gcsPath;

    private String originalName;
    private Long fileSize;
    private String contentType;

    // 첨부파일 → 스케줄 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // 첨부파일 → 유저 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
