package com.mycom.myapp.domain.participation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.participation.dto.ParticipationRequestDto;
import com.mycom.myapp.domain.participation.dto.ParticipationStatusResponseDto;
import com.mycom.myapp.domain.participation.dto.ParticipationSummaryDto;
import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.entity.ScheduleParticipation;
import com.mycom.myapp.domain.participation.repository.ScheduleParticipationRepository;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationServiceImpl implements ParticipationService {

    private final ScheduleParticipationRepository participationRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    /**
     * 투표 → 참여 / 불참
     * - userId : 현재 로그인한 유저 id
     * - request.scheduleId : 어떤 일정에 대한 투표인지
     * - request.status : ACCEPTED / DECLINED
     */
    @Override
    public ParticipationStatusResponseDto vote(Long userId, ParticipationRequestDto request) {
        Long scheduleId = request.getScheduleId();

        // 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정이 존재하지 않습니다. id=" + scheduleId));

        // 🔹 1) 투표 기능이 있는 일정인지 확인
        boolean votingEnabled = (schedule.getVoteDeadlineAt() != null || schedule.getMinParticipants() != null);
        if (!votingEnabled) {
            throw new IllegalStateException("투표 기능이 없는 일정입니다. scheduleId=" + scheduleId);
        }

        // 🔹 2) 현재 상태가 VOTING 인지 확인
        if (!schedule.isVoting()) {  // => status != VOTING
            throw new IllegalStateException("이미 투표가 종료된 일정입니다. scheduleId=" + scheduleId);
        }

        // 🔹 3) (옵션) 마감 시간 체크
        if (schedule.getVoteDeadlineAt() != null &&
            schedule.getVoteDeadlineAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("투표 마감 시간이 지났습니다. scheduleId=" + scheduleId);
        }
        
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. id=" + userId));

        // 기존 참여 정보가 있으면 가져오고, 없으면 새로 생성
        ScheduleParticipation participation = participationRepository
                .findByScheduleIdAndUserId(scheduleId, userId)
                .orElseGet(() -> ScheduleParticipation.builder()
                        .schedule(schedule)
                        .user(user)
                        .status(ParticipationStatus.INVITED) // 기본값, 곧 변경됨
                        .build()
                );

        // 요청 상태로 변경 (ACCEPTED / DECLINED)
        participation.changeStatus(request.getStatus());

        ScheduleParticipation saved = participationRepository.save(participation);
        return ParticipationStatusResponseDto.fromEntity(saved);
    }

    /**
     * 내 참여 상태 조회 (이 일정에 나는 참여/불참?)
     */
    @Override
    @Transactional(readOnly = true)
    public ParticipationStatusResponseDto getMyParticipation(Long userId, Long scheduleId) {

        return participationRepository
                .findByScheduleIdAndUserId(scheduleId, userId)
                .map(ParticipationStatusResponseDto::fromEntity)
                .orElse(null); // ✅ 참여 안 했으면 null 반환
    }

    /**
     * 참여 현황 요약
     * - ACCEPTED 몇 명
     * - DECLINED 몇 명
     * - noneCount는 필요하면 그룹 인원 수 기반으로 계산 가능
     */
    @Override
    @Transactional(readOnly = true)
    public ParticipationSummaryDto getParticipationSummary(Long scheduleId) {

        long accepted = participationRepository
                .countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED);

        long declined = participationRepository
                .countByScheduleIdAndStatus(scheduleId, ParticipationStatus.DECLINED);

        // TODO: 그룹 인원 수에서 accepted + declined 뺀 값으로 계산하고 싶으면 여기서 처리
        long none = 0L;

        return ParticipationSummaryDto.builder()
                .scheduleId(scheduleId)
                .acceptedCount(accepted)
                .declinedCount(declined)
                .noneCount(none)
                .build();
    }

    /**
     * 실제 참여하는 사람 리스트 (ACCEPTED 상태만)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationStatusResponseDto> getParticipants(Long scheduleId) {
        List<ScheduleParticipation> list = participationRepository
                .findByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED);

        return list.stream()
                .map(ParticipationStatusResponseDto::fromEntity)
                .toList();
    }
}