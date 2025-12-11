package com.mycom.myapp.domain.schedule.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.repository.ScheduleParticipationRepository;
import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.entity.ScheduleStatus;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository participationRepository;

    /**
     * 일정 생성
     * - dto.useVoting 이 true면: VOTING 상태 + 마감/최소인원 세팅
     * - false면: 바로 CONFIRMED 상태 (투표 없이 확정)
     */
    @Override
    public Long createSchedule(ScheduleRequestDto dto) {

        // 투표 사용 여부에 따라 상태/마감/최소 인원 설정
        ScheduleStatus status;
        if (dto.isUserVoting()) {
            status = ScheduleStatus.VOTING;

            // 필요하면 여기서 유효성 체크 (마감/최소인원 null 체크 등)
            // if (dto.getVoteDeadlineAt() == null) throw ...;
        } else {
            status = ScheduleStatus.CONFIRMED;
        }

        Schedule schedule = Schedule.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .placeName(dto.getPlaceName())
                .status(status)
                .voteDeadlineAt(dto.isUserVoting() ? dto.getVoteDeadlineAt() : null)
                .minParticipants(dto.isUserVoting() ? dto.getMinParticipants() : null)
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        return saved.getId();
    }

    /**
     * 일정 전체 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getScheduleList() {
        return scheduleRepository.findAll().stream()
                .map(ScheduleResponseDto::fromEntity)
                .toList();
    }

    /**
     * 일정 단건 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleDetail(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다. id=" + id));

        return ScheduleResponseDto.fromEntity(schedule);
    }

    /**
     * 일정 수정
     * - 기본 정보(title, time, place 등)
     * - 투표 설정(useVoting)이 바뀌면 상태/마감/최소인원도 다시 세팅
     */
    @Override
    public Long updateSchedule(Long id, ScheduleRequestDto dto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다. id=" + id));

        // 기본 정보 수정
        schedule.setTitle(dto.getTitle());
        schedule.setDescription(dto.getDescription());
        schedule.setStartAt(dto.getStartAt());
        schedule.setEndAt(dto.getEndAt());
        schedule.setPlaceName(dto.getPlaceName());

        // 투표 여부에 따른 상태/마감/최소인원 재설정
        if (dto.isUserVoting()) {
            schedule.setStatus(ScheduleStatus.VOTING);
            schedule.setVoteDeadlineAt(dto.getVoteDeadlineAt());
            schedule.setMinParticipants(dto.getMinParticipants());
        } else {
            schedule.setStatus(ScheduleStatus.CONFIRMED);
            schedule.setVoteDeadlineAt(null);
            schedule.setMinParticipants(null);
        }

        // 변경감지로 자동 update
        return schedule.getId();
    }

    /**
     * 일정 삭제
     */
    @Override
    public void deleteSchedule(Long id) {
        // 필요하면 존재 여부 먼저 체크 후 삭제해도 됨
        scheduleRepository.deleteById(id);
    }

    /**
     * 투표 종료 후 인원에 따라 상태 변경
     * - ACCEPTED 인원 수 >= minParticipants → CONFIRMED
     * - 아니면 CANCELED
     */
    @Override
    public void closeVoting(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다. id=" + scheduleId));

        if (!schedule.isVoting()) {
            throw new IllegalStateException("투표 중인 일정이 아닙니다. id=" + scheduleId);
        }

        // 현재 참여(YES) 인원 수 조회
        long acceptedCount = participationRepository
                .countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED);

        Integer min = schedule.getMinParticipants();

        if (min != null && acceptedCount < min) {
            // 최소 인원 미달 → 일정 취소
            schedule.setStatus(ScheduleStatus.CANCELED);
        } else {
            // 최소 인원 충족 or minParticipants가 없는 경우 → 확정
            schedule.setStatus(ScheduleStatus.CONFIRMED);
        }
    }
}