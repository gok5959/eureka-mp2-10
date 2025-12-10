package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.group.GroupMemberRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupListResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private GroupMemberRole myRole;
}