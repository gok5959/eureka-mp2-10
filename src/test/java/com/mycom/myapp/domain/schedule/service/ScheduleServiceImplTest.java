package com.mycom.myapp.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.repository.ScheduleParticipationRepository;
import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;
import com.mycom.myapp.domain.schedule.entity.Schedule;
import com.mycom.myapp.domain.schedule.entity.ScheduleStatus;
import com.mycom.myapp.domain.schedule.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    ScheduleParticipationRepository participationRepository;

    @InjectMocks
    ScheduleServiceImpl scheduleService;

    @Test
    @DisplayName("투표를 사용하는 일정 생성 시 상태는 VOTING 이다")
    void createSchedule_withVoting() {
        // given
        LocalDateTime now = LocalDateTime.now();

        ScheduleRequestDto dto = ScheduleRequestDto.builder()
                .title("스터디")
                .description("알고리즘 스터디")
                .startAt(now.plusDays(1))
                .endAt(now.plusDays(1).plusHours(2))
                .placeName("카페")
                .userVoting(true) // 투표 사용
                .minParticipants(3)
                .voteDeadlineAt(now.plusDays(1).minusHours(3))
                .build();

        Schedule saved = Schedule.builder()
                .id(1L)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .placeName(dto.getPlaceName())
                .status(ScheduleStatus.VOTING)
                .voteDeadlineAt(dto.getVoteDeadlineAt())
                .minParticipants(dto.getMinParticipants())
                .build();

        given(scheduleRepository.save(org.mockito.ArgumentMatchers.any(Schedule.class)))
                .willReturn(saved);

        // when
        Long id = scheduleService.createSchedule(dto);

        // then
        assertThat(id).isEqualTo(1L);

        // 저장되는 엔티티가 우리가 기대한 값인지도 확인
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(captor.capture());
        Schedule captured = captor.getValue();

        assertThat(captured.getStatus()).isEqualTo(ScheduleStatus.VOTING);
        assertThat(captured.getMinParticipants()).isEqualTo(3);
        assertThat(captured.getVoteDeadlineAt()).isEqualTo(dto.getVoteDeadlineAt());
    }

    @Test
    @DisplayName("투표를 사용하지 않는 일정 생성 시 상태는 CONFIRMED 이고 minParticipants/voteDeadline은 null이다")
    void createSchedule_withoutVoting() {
        // given
        LocalDateTime now = LocalDateTime.now();

        ScheduleRequestDto dto = ScheduleRequestDto.builder()
                .title("개인 일정")
                .description("병원 예약")
                .startAt(now.plusDays(2))
                .endAt(now.plusDays(2).plusHours(1))
                .placeName("병원")
                .userVoting(false) // 투표 사용 안 함
                .build();

        Schedule saved = Schedule.builder()
                .id(2L)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .placeName(dto.getPlaceName())
                .status(ScheduleStatus.CONFIRMED)
                .build();

        given(scheduleRepository.save(org.mockito.ArgumentMatchers.any(Schedule.class)))
                .willReturn(saved);

        // when
        Long id = scheduleService.createSchedule(dto);

        // then
        assertThat(id).isEqualTo(2L);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(captor.capture());
        Schedule captured = captor.getValue();

        assertThat(captured.getStatus()).isEqualTo(ScheduleStatus.CONFIRMED);
        assertThat(captured.getMinParticipants()).isNull();
        assertThat(captured.getVoteDeadlineAt()).isNull();
    }

    @Test
    @DisplayName("일정 단건 조회가 존재하면 DTO로 반환된다")
    void getScheduleDetail_found() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .status(ScheduleStatus.CONFIRMED)
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

        // when
        ScheduleResponseDto result = scheduleService.getScheduleDetail(scheduleId);

        // then
        assertThat(result).isNotNull();
        // 필요하면 result.getTitle() 등도 검증 (네 DTO 구조에 맞게)
    }

    @Test
    @DisplayName("존재하지 않는 일정 조회 시 예외가 발생한다")
    void getScheduleDetail_notFound() {
        // given
        Long scheduleId = 999L;
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getScheduleDetail(scheduleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 일정이 없습니다");
    }

    @Test
    @DisplayName("투표 종료 시 참여 인원이 최소 인원 이상이면 CONFIRMED 상태로 변경된다")
    void closeVoting_confirmed() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("투표 일정")
                .status(ScheduleStatus.VOTING)
                .minParticipants(2)
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(participationRepository.countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED))
                .willReturn(3L); // 참여 인원 3명

        // when
        scheduleService.closeVoting(scheduleId);

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CONFIRMED);
    }

    @Test
    @DisplayName("투표 종료 시 참여 인원이 최소 인원 미만이면 CANCELED 상태로 변경된다")
    void closeVoting_canceled() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("투표 일정")
                .status(ScheduleStatus.VOTING)
                .minParticipants(5)
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(participationRepository.countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED))
                .willReturn(2L); // 참여 인원 2명

        // when
        scheduleService.closeVoting(scheduleId);

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELED);
    }

    @Test
    @DisplayName("closeVoting은 투표 상태가 아닌 일정이면 예외를 던진다")
    void closeVoting_notVoting() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("확정 일정")
                .status(ScheduleStatus.CONFIRMED)
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleService.closeVoting(scheduleId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("투표 중인 일정이 아닙니다");
    }

    @Test
    @DisplayName("일정 삭제는 repository.deleteById를 호출한다")
    void deleteSchedule() {
        // given
        Long scheduleId = 1L;

        // when
        scheduleService.deleteSchedule(scheduleId);

        // then
        verify(scheduleRepository).deleteById(scheduleId);
    }
}
