package com.mycom.myapp.domain.participation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationSummaryDto {

    private Long scheduleId;
    private long noneCount;
    private long acceptedCount;
    private long declinedCount;

    // 필요하면 참석자 리스트도 따로 DTO 만들어서 추가 가능
}