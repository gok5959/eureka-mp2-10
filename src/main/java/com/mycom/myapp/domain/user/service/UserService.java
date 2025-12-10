package com.mycom.myapp.domain.user.service;

import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import com.mycom.myapp.domain.user.dto.UserSignupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserSignupRequest userSignupRequest);
    UserResponse findUserById(Long userId);
    Page<UserResponse> findUsersByCondition(UserSearchCondition condition, Pageable pageable);
}
