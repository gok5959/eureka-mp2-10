package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;

    public static GroupResponse from(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .build();
    }
}
