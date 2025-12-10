package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.group.GroupMemberRole;
import lombok.Data;

@Data
public class GroupSearchCondition {
    private GroupMemberRole role;
}
