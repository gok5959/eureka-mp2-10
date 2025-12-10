package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.entity.GroupMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupMemberResponse {
    private Long userId;
    private String userName;
    private String email;
    private GroupMemberRole role;

    public static GroupMemberResponse from(GroupMember gm) {
        return GroupMemberResponse.builder()
                .userId(gm.getUser().getId())
                .userName(gm.getUser().getName())
                .email(gm.getUser().getEmail())
                .role(gm.getRole())
                .build();
    }

}
