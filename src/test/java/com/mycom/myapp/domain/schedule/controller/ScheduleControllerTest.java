package com.mycom.myapp.domain.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.schedule.dto.ScheduleRequestDto;
import com.mycom.myapp.domain.schedule.dto.ScheduleResponseDto;
import com.mycom.myapp.domain.schedule.service.ScheduleService;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)   // 🔥 보안 필터 끄기
@WebMvcTest(ScheduleController.class)   // 이 컨트롤러만 로딩
class ScheduleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ScheduleService scheduleService;    // 컨트롤러에서 주입받는 서비스는 Mock 처리

    @Test
    @DisplayName("그룹 일정 생성 API - 유효한 요청이면 200과 id를 응답한다")
    void createGroupSchedule_success() throws Exception {
        // given
        Long groupId = 1L;

        ScheduleRequestDto requestDto = ScheduleRequestDto.builder()
                .title("스터디")
                .description("알고리즘 스터디")
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .placeName("카페")
                .userVoting(true)
                .minParticipants(3)
                .build();

        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willReturn(100L);

        // when & then
        mockMvc.perform(post("/groups/{groupId}/schedules", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
        		.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        // groupId가 dto에 세팅되어 서비스로 넘어가는지까지 체크하고 싶으면:
        verify(scheduleService).createSchedule(Mockito.argThat(dto ->
                dto.getGroupId().equals(groupId)
                        && dto.getTitle().equals("스터디")
        ));
    }

    @Test
    @DisplayName("개인 일정 생성 API - 유효한 요청이면 200과 id를 응답한다")
    void createPersonalSchedule_success() throws Exception {
        ScheduleRequestDto requestDto = ScheduleRequestDto.builder()
                .title("병원 예약")
                .description("정기 검진")
                .startAt(LocalDateTime.now().plusDays(2))
                .endAt(LocalDateTime.now().plusDays(2).plusHours(1))
                .placeName("병원")
                .userVoting(false)
                .build();

        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willReturn(200L);

        mockMvc.perform(post("/personal-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
            	.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("200"));
    }

    @Test
    @DisplayName("그룹 일정 상세 조회 API - 존재하는 일정이면 200과 DTO를 응답한다")
    void getGroupScheduleDetail_success() throws Exception {
        Long scheduleId = 1L;

        ScheduleResponseDto responseDto = ScheduleResponseDto.builder()
                .id(scheduleId)
                .title("테스트 일정")
                .description("설명")
                .build();

        given(scheduleService.getScheduleDetail(scheduleId))
                .willReturn(responseDto);

        mockMvc.perform(get("/group-schedules/{scheduleId}", scheduleId))
        		.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduleId))
                .andExpect(jsonPath("$.title").value("테스트 일정"));
    }

    @Test
    @DisplayName("개인 일정 목록 조회 API - 유효한 요청이면 200과 리스트를 응답한다")
    void getPersonalScheduleList_success() throws Exception {
        List<ScheduleResponseDto> list = List.of(
                ScheduleResponseDto.builder().id(1L).title("일정1").build(),
                ScheduleResponseDto.builder().id(2L).title("일정2").build()
        );
        given(scheduleService.getScheduleList()).willReturn(list);

        mockMvc.perform(get("/personal-schedules"))
				.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("그룹 일정 수정 API - 유효한 요청이면 200과 id를 응답한다")
    void updateGroupSchedule_success() throws Exception {
        Long scheduleId = 1L;

        ScheduleRequestDto requestDto = ScheduleRequestDto.builder()
                .title("수정된 제목")
                .userVoting(false)
                .build();

        given(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequestDto.class)))
                .willReturn(scheduleId);

        mockMvc.perform(put("/group-schedules/{scheduleId}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
				.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("개인 일정 삭제 API - 유효한 요청이면 204를 응답한다")
    void deletePersonalSchedule_success() throws Exception {
        Long scheduleId = 1L;

        mockMvc.perform(delete("/personal-schedules/{scheduleId}", scheduleId))
				.andDo(print())
                .andExpect(status().isNoContent());

        verify(scheduleService).deleteSchedule(scheduleId);
    }
}
