package com.mycom.myapp.domain.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.CurrentUser;
import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;
import com.mycom.myapp.domain.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // =========================
    //  그룹 일정 API
    // =========================

    /**
     * 그룹 일정 생성
     * POST /groups/{groupId}/schedules
     */
    @PostMapping("/groups/{groupId}/schedules")
    public ResponseEntity<Long> createGroupSchedule(
            @PathVariable("groupId") Long groupId,
            @RequestBody ScheduleRequestDto dto
    ) {
        // 그룹 일정이므로 DTO에 groupId 세팅
        dto.setGroupId(groupId);

        Long id = scheduleService.createSchedule(dto);
        return ResponseEntity.ok(id);
    }

    /**
     * 그룹 일정 목록 조회
     * GET /groups/{groupId}/schedules
     */
    @GetMapping("/groups/{groupId}/schedules")
    public ResponseEntity<List<ScheduleResponseDto>> getGroupScheduleList(
            @PathVariable("groupId") Long groupId
    ) {
        // TODO: groupId 기준으로 필터링하는 메서드로 변경 (예: scheduleService.getGroupSchedules(groupId))
        List<ScheduleResponseDto> list = scheduleService.getScheduleList();
        return ResponseEntity.ok(list);
    }

    /**
     * 그룹 일정 상세 조회
     * GET /group-schedules/{scheduleId}
     */
    @GetMapping("/group-schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> getGroupScheduleDetail(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        ScheduleResponseDto dto = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 그룹 일정 수정
     * PUT /group-schedules/{scheduleId}
     */
    @PutMapping("/group-schedules/{scheduleId}")
    public ResponseEntity<Long> updateGroupSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleRequestDto dto
    ) {
        Long updatedId = scheduleService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok(updatedId);
    }

    /**
     * 그룹 일정 취소(삭제)
     * DELETE /group-schedules/{scheduleId}
     */
    @DeleteMapping("/group-schedules/{scheduleId}")
    public ResponseEntity<Void> cancelGroupSchedule(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // =========================
    //  개인 일정 API
    // =========================

    /**
     * 개인 일정 생성
     * POST /personal-schedules
     */
    @PostMapping("/personal-schedules")
    public ResponseEntity<Long> createPersonalSchedule(
            @RequestBody ScheduleRequestDto dto
    ) {
        // 개인 일정이므로 groupId 는 null로 둔다 (또는 무시)
        dto.setGroupId(null);

        Long id = scheduleService.createSchedule(dto);
        
        return ResponseEntity.ok(id);
    }

    /**
     * 개인 일정 목록 조회
     * GET /personal-schedules
     */
//    @GetMapping("/personal-schedules")
//    public ResponseEntity<List<ScheduleResponseDto>> getPersonalScheduleList() {
//        // TODO: 현재 로그인 유저 기준으로 필터링 (ownerId)
//        List<ScheduleResponseDto> list = scheduleService.getScheduleList();
//        return ResponseEntity.ok(list);
//    }
    

	@GetMapping("/personal-schedules")
	public ResponseEntity<List<ScheduleResponseDto>> getPersonalScheduleList(Authentication auth) {
	    Long userId = CurrentUser.idOrDev(auth, 1L); // ✅ 무조건 1번으로 테스트 가능
	    System.out.println("userId = " + userId);
	
	    return ResponseEntity.ok(scheduleService.getScheduleList());
	}

    /**
     * 개인 일정 상세 조회
     * GET /personal-schedules/{scheduleId}
     */
    @GetMapping("/personal-schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> getPersonalScheduleDetail(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        ScheduleResponseDto dto = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 개인 일정 수정
     * PUT /personal-schedules/{scheduleId}
     */
    @PutMapping("/personal-schedules/{scheduleId}")
    public ResponseEntity<Long> updatePersonalSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleRequestDto dto
    ) {
        Long updatedId = scheduleService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok(updatedId);
    }

    /**
     * 개인 일정 삭제
     * DELETE /personal-schedules/{scheduleId}
     */
    @DeleteMapping("/personal-schedules/{scheduleId}")
    public ResponseEntity<Void> deletePersonalSchedule(
            @PathVariable("scheduleId") Long scheduleId
    ) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}