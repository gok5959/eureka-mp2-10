package com.mycom.myapp.domain.participation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.entity.ScheduleParticipation;

@Repository
public interface ScheduleParticipationRepository extends JpaRepository<ScheduleParticipation, Long> {

    // 특정 일정 + 특정 유저의 참여 정보 (있을 수도, 없을 수도)
    Optional<ScheduleParticipation> findByScheduleIdAndUserId(Long scheduleId, Long userId);

    // 해당 일정의 특정 상태(ACCEPTED 등) 인원 수
    long countByScheduleIdAndStatus(Long scheduleId, ParticipationStatus status);

    // 해당 일정의 전체 참여 정보
    List<ScheduleParticipation> findByScheduleId(Long scheduleId);

    // 상태별 참여자 목록
    List<ScheduleParticipation> findByScheduleIdAndStatus(Long scheduleId, ParticipationStatus status);
}

