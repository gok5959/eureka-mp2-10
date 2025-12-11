package com.mycom.myapp.domain.user.service;

import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import com.mycom.myapp.domain.user.dto.UserSignupRequest;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.DuplicatedEmailException;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse insertUser(UserSignupRequest userSignupRequest) {
        boolean userExists = userRepository.existsByEmail(userSignupRequest.getEmail());
        if(userExists) {
            throw new DuplicatedEmailException("Duplicate email address : " + userSignupRequest.getEmail());
        }

        return UserResponse.from(userRepository.save(User.builder()
                .name(userSignupRequest.getName())
                .email(userSignupRequest.getEmail())
                .role(userSignupRequest.getRole())
                .passwordHash(passwordEncoder.encode(userSignupRequest.getPassword()))
                .build()));
    }

    @Override
    public UserResponse findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found : " + userId));
        return UserResponse.from(user);
    }

    @Override
    public UserResponse findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User Not Found : " + email));
        return UserResponse.from(user);
    }

    @Override
    public Page<UserResponse> searchUsers(UserSearchCondition condition, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(
                condition.getKeyword(),
                condition.getRole(),
                pageable
        );
        return users.map(UserResponse::from);
    }
}
