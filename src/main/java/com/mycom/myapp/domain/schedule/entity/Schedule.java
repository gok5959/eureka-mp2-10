package com.mycom.myapp.domain.schedule.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	
	@Column(nullable = false)
	private String title;
	private String description;
	
	// Merge 후 구현 필요
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "owner_id", nullable = false)
//	private User owner;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "group_id")
//	private Group group;
	
	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;
	
	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;
	
	@Column(name = "place_name")
	private String placeName;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScheduleStatus status;
	
	@Column(name = "vote_deadline_at")
	private LocalDateTime voteDeadlineAt;
	
	@Column(name = "min_participants")
	private Integer minParticipants;
	
	public boolean isVoting() {
		return status == ScheduleStatus.VOTING;
	}
	
	// BaseEntity로 createdAt, updateAt 선언
	
//	public boolean isGroupSchedule() {
//		return this.group != null;
//	}
}
