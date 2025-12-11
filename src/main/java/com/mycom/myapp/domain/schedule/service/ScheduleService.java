package com.mycom.myapp.domain.schedule.service;

import java.util.List;

import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;

public interface ScheduleService {
    // 일정 생성
	// [필수] 투표 생성 여부 로직
    Long createSchedule(ScheduleRequestDto dto);
    
    // 일정 전체 조회
    List<ScheduleResponseDto> getScheduleList();
    
    // 일정 상세 조회
    ScheduleResponseDto getScheduleDetail(Long id);

    // 일정 수정
    Long updateSchedule(Long id, ScheduleRequestDto dto);

    // 일정 삭제
    void deleteSchedule(Long id);
    
    // 그룹 일정 목록
    List<ScheduleResponseDto> getGroupScheduleList(Long groupId);

    // 개인 일정 목록 (ownerId = 현재 로그인 유저)
    List<ScheduleResponseDto> getPersonalScheduleList(Long ownerId);
    
    // 투표 종료 후, 인원에 따라 상태 변경
    void closeVoting(Long scheduleId);
}
