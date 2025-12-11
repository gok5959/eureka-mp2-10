package com.mycom.myapp.domain.participation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.participation.dto.ParticipationRequestDto;
import com.mycom.myapp.domain.participation.dto.ParticipationStatusResponseDto;
import com.mycom.myapp.domain.participation.dto.ParticipationSummaryDto;
import com.mycom.myapp.domain.participation.service.ParticipationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    /**
     * 참여 상태 등록/변경 (참여 / 불참 투표)
     * POST /group-schedules/{scheduleId}/participations?userId=10
     *
     * body 예:
     * { "status": "ACCEPTED" }
     */
    @PostMapping("/group-schedules/{scheduleId}/participations")
    public ResponseEntity<ParticipationStatusResponseDto> vote(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userId") Long userId,
            @RequestBody ParticipationRequestDto request
    ) {
        // path 에서 받은 scheduleId 를 DTO에 세팅해줌
        request.setScheduleId(scheduleId);

        ParticipationStatusResponseDto response =
                participationService.vote(userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 참여 상태 조회
     * GET /group-schedules/{scheduleId}/participations/me?userId=10
     */
    @GetMapping("/group-schedules/{scheduleId}/participations/me")
    public ResponseEntity<ParticipationStatusResponseDto> getMyParticipation(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userId") Long userId
    ) {
        ParticipationStatusResponseDto response =
                participationService.getMyParticipation(userId, scheduleId);

        return ResponseEntity.ok(response);
    }

    /**
     * 해당 일정 참여 현황 조회 (요약 정보)
     * GET /group-schedules/{scheduleId}/participations
     */
    @GetMapping("/group-schedules/{scheduleId}/participations")
    public ResponseEntity<ParticipationSummaryDto> getParticipationSummary(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        ParticipationSummaryDto summary =
                participationService.getParticipationSummary(scheduleId);

        return ResponseEntity.ok(summary);
    }

    /**
     * (옵션) 실제 참여자 리스트까지 보고 싶으면 이런 것도 만들 수 있음
     * GET /group-schedules/{scheduleId}/participations/list
     */
    @GetMapping("/group-schedules/{scheduleId}/participations/list")
    public ResponseEntity<List<ParticipationStatusResponseDto>> getParticipants(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        List<ParticipationStatusResponseDto> participants =
                participationService.getParticipants(scheduleId);

        return ResponseEntity.ok(participants);
    }
}
