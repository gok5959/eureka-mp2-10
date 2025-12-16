package com.mycom.myapp.domain.participation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.participation.dto.ParticipationRequestDto;
import com.mycom.myapp.domain.participation.dto.ParticipationStatusResponseDto;
import com.mycom.myapp.domain.participation.dto.ParticipationSummaryDto;
import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.service.ParticipationService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ParticipationController.class)
@AutoConfigureMockMvc(addFilters = false)   // Security 필터 끔

class ParticipationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ParticipationService participationService;
    
    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("참여 상태 등록/변경 API - 유효한 요청이면 200과 상태 DTO를 응답한다")
    void vote_success() throws Exception {
        // given
        Long scheduleId = 1L;
        Long userId = 10L;

        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .status(ParticipationStatus.ACCEPTED)
                .build();

        ParticipationStatusResponseDto responseDto = ParticipationStatusResponseDto.builder()
                .scheduleId(scheduleId)
                .userId(userId)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        given(participationService.vote(eq(userId), any(ParticipationRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/group-schedules/{scheduleId}/participations", scheduleId)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // scheduleId가 DTO에 제대로 세팅되었는지까지 확인
        verify(participationService).vote(eq(userId), argThat(req ->
                req.getScheduleId().equals(scheduleId)
                        && req.getStatus() == ParticipationStatus.ACCEPTED
        ));
    }

    @Test
    @DisplayName("내 참여 상태 조회 API - 유효한 요청이면 200과 상태 DTO를 응답한다")
    void getMyParticipation_success() throws Exception {
        // given
        Long scheduleId = 1L;
        Long userId = 10L;

        ParticipationStatusResponseDto responseDto = ParticipationStatusResponseDto.builder()
                .scheduleId(scheduleId)
                .userId(userId)
                .status(ParticipationStatus.DECLINED)
                .build();

        given(participationService.getMyParticipation(userId, scheduleId))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/group-schedules/{scheduleId}/participations/me", scheduleId)
                        .param("userId", String.valueOf(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    @DisplayName("참여 현황 요약 조회 API - 유효한 요청이면 200과 summary DTO를 응답한다")
    void getParticipationSummary_success() throws Exception {
        // given
        Long scheduleId = 1L;

        ParticipationSummaryDto summaryDto = ParticipationSummaryDto.builder()
                .scheduleId(scheduleId)
                .acceptedCount(3)
                .declinedCount(1)
                .noneCount(2)
                .build();

        given(participationService.getParticipationSummary(scheduleId))
                .willReturn(summaryDto);

        // when & then
        mockMvc.perform(get("/group-schedules/{scheduleId}/participations", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId))
                .andExpect(jsonPath("$.acceptedCount").value(3))
                .andExpect(jsonPath("$.declinedCount").value(1))
                .andExpect(jsonPath("$.noneCount").value(2));
    }

    @Test
    @DisplayName("참여자 리스트 조회 API - 유효한 요청이면 200과 리스트를 응답한다")
    void getParticipants_success() throws Exception {
        // given
        Long scheduleId = 1L;

        List<ParticipationStatusResponseDto> participants = List.of(
                ParticipationStatusResponseDto.builder()
                        .scheduleId(scheduleId)
                        .userId(10L)
                        .status(ParticipationStatus.ACCEPTED)
                        .build(),
                ParticipationStatusResponseDto.builder()
                        .scheduleId(scheduleId)
                        .userId(11L)
                        .status(ParticipationStatus.ACCEPTED)
                        .build()
        );

        given(participationService.getParticipants(scheduleId))
                .willReturn(participants);

        // when & then
        mockMvc.perform(get("/group-schedules/{scheduleId}/participations/list", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(10L))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }
}
