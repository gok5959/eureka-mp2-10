package com.mycom.myapp.domain.user.service;

import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import com.mycom.myapp.domain.user.dto.UserSignupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse insertUser(UserSignupRequest userSignupRequest);
    UserResponse findUserById(Long userId);
    UserResponse findUserByEmail(String email);
    Page<UserResponse> searchUsers(UserSearchCondition condition, Pageable pageable);

}
