package com.mycom.myapp.domain.group.dto;

import com.mycom.myapp.domain.user.entity.User;
import lombok.Data;

@Data
public class GroupCreateRequest {
    private String name;
    private String description;
    private User owner;
}
