package com.mycom.myapp.domain.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private GroupMemberRole myRole;
    private long memberCount;

    public static GroupDetailResponse from(Group group) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .ownerName(group.getOwner().getName())
                .build();
    }

    public static GroupDetailResponse from(Group group, GroupMemberRole myRole, long memberCount) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .ownerName(group.getOwner().getName())
                .myRole(myRole)
                .memberCount(memberCount)
                .build();
    }
}
