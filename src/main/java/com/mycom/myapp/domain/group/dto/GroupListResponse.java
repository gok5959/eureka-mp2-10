package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GroupListResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private GroupMemberRole myRole;

    public static GroupListResponse from(Group group) {
        return GroupListResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .ownerName(group.getOwner().getName())
                .build();
    }

    public static GroupListResponse from(Group group, GroupMemberRole myRole) {
        return GroupListResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .myRole(myRole)
                .build();
    }
}