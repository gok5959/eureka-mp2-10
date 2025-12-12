package com.mycom.myapp.domain.schedule.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.group.repository.GroupRepository;
import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.repository.ScheduleParticipationRepository;
import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.entity.ScheduleStatus;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    //private final ScheduleCommentRepository scheduleCommentRepository; // 🔹 댓글 레포지토리 추가

    /**
     * 일정 생성
     * - DTO에서 받은 정보(title, description, time, place 등)를 사용해서 Schedule 엔티티 생성
     * - userVoting 여부에 따라 상태(ScheduleStatus)를 VOTING / CONFIRMED 로 결정
     *   - userVoting == true  → VOTING + voteDeadlineAt + minParticipants 세팅
     *   - userVoting == false → CONFIRMED + 투표 관련 값 null
     * - 생성된 일정의 id를 반환
     */
    @Override
    public Long createSchedule(ScheduleRequestDto dto) {
    	
        // 1. owner 설정 (무조건 필요)
        if (dto.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId는 필수입니다.");
        }

        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. id=" + dto.getOwnerId()));

        // 2. group 설정 (개인 일정이면 null, 그룹 일정이면 path에서 온 groupId)
        Group group = null;
        if (dto.getGroupId() != null) {
            group = groupRepository.findById(dto.getGroupId())
            		.orElseThrow(() -> new IllegalArgumentException("해당 그룹이 없습니다. id=" + dto.getGroupId()));
        }

        // 투표 기능 사용 여부에 따라 초기 상태 결정
        ScheduleStatus status = dto.isUserVoting()
                ? ScheduleStatus.VOTING
                : ScheduleStatus.CONFIRMED;

        Schedule schedule = Schedule.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                // TODO: owner, group 세팅은 나중에 Security/Group 연관관계 붙이면서 처리
                .owner(owner)
                .group(group)
                //
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
     * 전체 일정 목록 조회
     * - 관리용/디버깅용으로 사용 (실제 화면에서는 그룹/개인 필터링된 메서드 쓰는게 좋음)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getScheduleList() {
        return scheduleRepository.findAll().stream()
                .map(ScheduleResponseDto::fromEntity) // 댓글/첨부 없는 가벼운 버전
                .toList();
    }

    /**
     * 그룹 일정 목록 조회
     * - 특정 groupId에 속한 일정들만 조회
     * - group_id = :groupId 인 일정만 반환
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getGroupScheduleList(Long groupId) {
        return scheduleRepository.findByGroup_Id(groupId).stream()
                .map(ScheduleResponseDto::fromEntity) // 목록/달력 화면용: 댓글/첨부 X
                .toList();
    }

    /**
     * 개인 일정 목록 조회
     * - ownerId = 나 이고, group 이 null 인 일정만 조회 (순수 개인 일정)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getPersonalScheduleList(Long ownerId) {
        return scheduleRepository.findByOwner_IdAndGroupIsNull(ownerId).stream()
                .map(ScheduleResponseDto::fromEntity) // 목록/달력 화면용: 댓글/첨부 X
                .toList();
    }

    /**
     * 일정 상세 조회
     * - 단건 Schedule 엔티티를 찾고
     * - 해당 일정에 달린 댓글 목록을 댓글 레포지토리에서 조회
     * - 첨부파일은 schedule.getAttachments() 로 가져옴
     * - DTO의 fromEntityWithDetails(...) 를 사용해 댓글+첨부 포함한 상세 DTO로 변환
     */
    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleDetail(Long id) {
//        // 1) 일정 엔티티 조회
//        Schedule schedule = scheduleRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다. id=" + id));
//
//        // 2) 이 일정에 달린 댓글 목록 조회
//        List<ScheduleComment> comments =
//                scheduleCommentRepository.findBySchedule_IdOrderByCreatedAtAsc(id);
//
//        // 3) 댓글 + 첨부까지 포함한 상세 DTO로 변환
//        return ScheduleResponseDto.fromEntityWithDetails(schedule, comments);
    	return null;
    }

    /**
     * 일정 수정
     * - 기본 정보(title, description, time, place)를 DTO로부터 받아서 변경
     * - userVoting 값에 따라 다시 상태/투표 관련 필드 재설정
     * - JPA 변경 감지에 의해 트랜잭션 종료 시 자동 UPDATE
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

        // 투표 여부에 따른 상태/투표 설정 재조정
        if (dto.isUserVoting()) {
            schedule.setStatus(ScheduleStatus.VOTING);
            schedule.setVoteDeadlineAt(dto.getVoteDeadlineAt());
            schedule.setMinParticipants(dto.getMinParticipants());
        } else {
            schedule.setStatus(ScheduleStatus.CONFIRMED);
            schedule.setVoteDeadlineAt(null);
            schedule.setMinParticipants(null);
        }

        // 변경 감지로 자동 update 되므로 save() 안 해도 됨
        return schedule.getId();
    }

    /**
     * 일정 삭제
     * - 지정된 id의 일정 삭제
     * - 존재하지 않는 id여도 deleteById()는 예외 없이 지나갈 수 있으니,
     *   필요하다면 삭제 전 findById로 존재 여부를 확인하고 예외를 던져도 된다.
     */
    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    /**
     * 투표 종료 처리
     * - 현재 일정이 VOTING 상태인지 확인 (아니면 예외)
     * - 참여 테이블에서 ACCEPTED(참여) 인원 수 조회
     * - minParticipants 설정값과 비교
     *   - acceptedCount < minParticipants → CANCELED
     *   - 그 외 → CONFIRMED
     */
    @Override
    public void closeVoting(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다. id=" + scheduleId));

        // 투표 중인 일정인지 검증
        if (!schedule.isVoting()) {
            throw new IllegalStateException("투표 중인 일정이 아닙니다. id=" + scheduleId);
        }

        // ACCEPTED 상태인 참여 인원 수
        long acceptedCount = participationRepository
                .countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED);

        Integer min = schedule.getMinParticipants();

        if (min != null && acceptedCount < min) {
            // 최소 인원 미달 → 일정 취소
            schedule.setStatus(ScheduleStatus.CANCELED);
        } else {
            // 최소 인원 충족 or 최소 인원 미설정 → 확정
            schedule.setStatus(ScheduleStatus.CONFIRMED);
        }
    }
}