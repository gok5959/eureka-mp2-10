package com.mycom.myapp.domain.participation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.BDDMockito.given; 
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ParticipationServiceImplTest {

    @Mock
    ScheduleParticipationRepository participationRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ParticipationServiceImpl participationService;

    @Test
    @DisplayName("새 유저가 투표하면 참여 엔티티가 생성되고 상태가 저장된다")
    void vote_createNewParticipation() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;

        ParticipationRequestDto request = ParticipationRequestDto.builder()
                .scheduleId(scheduleId)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .build();

        User user = User.builder()
                .id(userId)
                .name("홍길동")
                .build();

        // 레포 동작 정의
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(participationRepository.findByScheduleIdAndUserId(scheduleId, userId))
                .willReturn(Optional.empty());

        ScheduleParticipation saved = ScheduleParticipation.builder()
                .id(100L)
                .schedule(schedule)
                .user(user)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        given(participationRepository.save(org.mockito.ArgumentMatchers.any(ScheduleParticipation.class)))
                .willReturn(saved);

        // when
        ParticipationStatusResponseDto result = participationService.vote(userId, request);

        // then
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(ParticipationStatus.ACCEPTED);

        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(userId);
        verify(participationRepository).save(org.mockito.ArgumentMatchers.any(ScheduleParticipation.class));
    }

    @Test
    @DisplayName("이미 참여 정보가 있으면 vote는 상태만 업데이트한다")
    void vote_updateExistingParticipation() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;

        ParticipationRequestDto request = ParticipationRequestDto.builder()
                .scheduleId(scheduleId)
                .status(ParticipationStatus.DECLINED) // 불참으로 변경
                .build();

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .build();

        User user = User.builder()
                .id(userId)
                .name("홍길동")
                .build();

        ScheduleParticipation existing = ScheduleParticipation.builder()
                .id(100L)
                .schedule(schedule)
                .user(user)
                .status(ParticipationStatus.ACCEPTED) // 원래는 참여
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(participationRepository.findByScheduleIdAndUserId(scheduleId, userId))
                .willReturn(Optional.of(existing));

        // save 호출 시, JPA처럼 그대로 엔티티 리턴한다고 가정
        given(participationRepository.save(org.mockito.ArgumentMatchers.any(ScheduleParticipation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ParticipationStatusResponseDto result = participationService.vote(userId, request);

        // then
        assertThat(result.getStatus()).isEqualTo(ParticipationStatus.DECLINED);
    }

    @Test
    @DisplayName("내 참여 상태 조회: 존재하면 ParticipationStatusResponseDto로 반환")
    void getMyParticipation_found() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .build();

        User user = User.builder()
                .id(userId)
                .name("홍길동")
                .build();

        ScheduleParticipation participation = ScheduleParticipation.builder()
                .id(100L)
                .schedule(schedule)
                .user(user)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        given(participationRepository.findByScheduleIdAndUserId(scheduleId, userId))
                .willReturn(Optional.of(participation));

        // when
        ParticipationStatusResponseDto result = participationService.getMyParticipation(userId, scheduleId);

        // then
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(ParticipationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("내 참여 상태 조회: 참여 정보가 없으면 예외 발생")
    void getMyParticipation_notFound() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;

        given(participationRepository.findByScheduleIdAndUserId(scheduleId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.getMyParticipation(userId, scheduleId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참여 정보가 없습니다");
    }

    @Test
    @DisplayName("참여 현황 요약: ACCEPTED / DECLINED 인원 수를 반환")
    void getParticipationSummary() {
        // given
        Long scheduleId = 1L;

        given(participationRepository.countByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED))
                .willReturn(3L);
        given(participationRepository.countByScheduleIdAndStatus(scheduleId, ParticipationStatus.DECLINED))
                .willReturn(1L);

        // when
        ParticipationSummaryDto summary = participationService.getParticipationSummary(scheduleId);

        // then
        assertThat(summary.getScheduleId()).isEqualTo(scheduleId);
        assertThat(summary.getAcceptedCount()).isEqualTo(3L);
        assertThat(summary.getDeclinedCount()).isEqualTo(1L);
        // noneCount는 지금 0L로 두었으니 그 값도 확인 가능
        assertThat(summary.getNoneCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("실제 참여자 목록: ACCEPTED 상태인 참여자만 반환")
    void getParticipants() {
        // given
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .build();

        User user1 = User.builder().id(10L).name("유저1").build();
        User user2 = User.builder().id(20L).name("유저2").build();

        ScheduleParticipation p1 = ScheduleParticipation.builder()
                .id(100L)
                .schedule(schedule)
                .user(user1)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        ScheduleParticipation p2 = ScheduleParticipation.builder()
                .id(101L)
                .schedule(schedule)
                .user(user2)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        given(participationRepository.findByScheduleIdAndStatus(scheduleId, ParticipationStatus.ACCEPTED))
                .willReturn(List.of(p1, p2));

        // when
        List<ParticipationStatusResponseDto> result = participationService.getParticipants(scheduleId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ParticipationStatusResponseDto::getUserId)
                .containsExactlyInAnyOrder(10L, 20L);
    }
}