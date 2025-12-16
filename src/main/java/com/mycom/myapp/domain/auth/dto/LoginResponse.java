package com.mycom.myapp.domain.auth.dto;

import com.mycom.myapp.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    // TODO 일정 가능하면 refreshToken 구현
    private String refreshToken;
    private UserResponse user;
}
