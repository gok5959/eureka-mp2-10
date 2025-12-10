package com.mycom.myapp.domain.participation.dto;

import com.mycom.myapp.domain.participation.entity.ParticipationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestDto {

    // 어떤 일정에 대한 참여인지
    private Long scheduleId;

    // 참여 상태 (ACCEPTED / DECLINED 등)
    private ParticipationStatus status;
}
