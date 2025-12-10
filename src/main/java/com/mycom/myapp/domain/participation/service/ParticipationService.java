package com.mycom.myapp.domain.participation.service;

import java.util.List;

import com.mycom.myapp.domain.participation.dto.ParticipationRequestDto;
import com.mycom.myapp.domain.participation.dto.ParticipationStatusResponseDto;
import com.mycom.myapp.domain.participation.dto.ParticipationSummaryDto;

public interface ParticipationService {

    // 투표만 활성화된 일정 → 참여 / 불참 투표
    ParticipationStatusResponseDto vote(Long userId, ParticipationRequestDto request);

    // 내 참여 상태 조회 (이 일정에 난 참여/불참?)
    ParticipationStatusResponseDto getMyParticipation(Long userId, Long scheduleId);

    // 이 일정의 참여 현황 요약 (참여 몇 명, 불참 몇 명)
    ParticipationSummaryDto getParticipationSummary(Long scheduleId);

    // 이 일정에 실제 참여하는 사람 리스트
    List<ParticipationStatusResponseDto> getParticipants(Long scheduleId);
}
