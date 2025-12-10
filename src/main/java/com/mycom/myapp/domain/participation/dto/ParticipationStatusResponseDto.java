package com.mycom.myapp.domain.participation.dto;

import com.mycom.myapp.domain.participation.entity.ParticipationStatus;
import com.mycom.myapp.domain.participation.entity.ScheduleParticipation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationStatusResponseDto {

    private Long scheduleId;
    private Long userId;
    private ParticipationStatus status;

    public static ParticipationStatusResponseDto fromEntity(ScheduleParticipation participation) {
        return ParticipationStatusResponseDto.builder()
                .scheduleId(participation.getSchedule().getId())
                //.userId(participation.getUser().getId())
                .status(participation.getStatus())
                .build();
    }
}
