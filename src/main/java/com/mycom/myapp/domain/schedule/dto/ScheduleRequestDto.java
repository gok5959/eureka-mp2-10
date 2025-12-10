package com.mycom.myapp.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;

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
// 일정 요청할 때의 (클라이언트 -> 서버) Dto
public class ScheduleRequestDto {
    private String title;
    private String description;
    // Security 붙이면 현재 로그인 유저가 자동으로 붙는다 생각하지만
    // 혹시 몰라서 두겠습니다.
    // private Long ownerId;
    private Long groupId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String placeName;
    
    // 투표 사용 여부
    private boolean userVoting;
    private Integer minParticipants;
    // 투표 사용한다면 마감 시각
    private LocalDateTime voteDeadlineAt;
    // 투표 사용 안하고 참여자로
    private List<Long> participantUsersIds;
}
