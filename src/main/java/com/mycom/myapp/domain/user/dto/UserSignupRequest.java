package com.mycom.myapp.domain.user.dto;

import com.mycom.myapp.domain.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupRequest {

    @Email(message = "이메일 형식이 맞지 않습니다.")
    @NotBlank(message = "이메일은 ")
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank
    private String name;


    private UserRole role;
}
