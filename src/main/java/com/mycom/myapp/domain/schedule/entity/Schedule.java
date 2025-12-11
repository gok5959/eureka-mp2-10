package com.mycom.myapp.domain.schedule.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.schedule_extras.entity.ScheduleAttachment;
import com.mycom.myapp.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="schedule")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// 일정 제목
	@Column(name = "title", nullable = false)
	private String title;
	
	// 일정 내용
	@Column(name = "description")
	private String description;
	
	// User ID
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	// 그룹 ID
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;
	
	// 일정 시작 시각
	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;
	
	// 일정 마무리 시각
	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;
	
	// 장소
	@Column(name = "place_name")
	private String placeName;
	
	// 일정 상태 [VOTING / CONFIRMED / CANCELED]
	// 투표 기능 활성화 시 VOTING
	// 투표 종료 후 설정한 참가자 수보다 많거나 같을 시에 CONFIRMED
	// 투표 종료 후 설정한 참가자 수보다 적으면 CANCELED
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScheduleStatus status;
	
	// 투표 데드라인
	@Column(name = "vote_deadline_at")
	private LocalDateTime voteDeadlineAt;
	
	// 최소 참가자 수 설정
	@Column(name = "min_participants")
	private Integer minParticipants;
	
	// 첨부 파일
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleAttachment> attachments = new ArrayList<>();

    public void addAttachment(ScheduleAttachment attachment) {
        attachments.add(attachment);
        attachment.setSchedule(this);
    }

    public void removeAttachment(ScheduleAttachment attachment) {
        attachments.remove(attachment);
        attachment.setSchedule(null);
    }
	
	public boolean isVoting() {
		return status == ScheduleStatus.VOTING;
	}
	
	// BaseEntity로 createdAt, updateAt 선언
	
	public boolean isGroupSchedule() {
		return this.group != null;
	}
}
