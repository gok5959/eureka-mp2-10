package com.mycom.myapp.domain.user.dto;

import com.mycom.myapp.domain.user.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchCondition {
    private String keyword;
    private UserRole role;
}
