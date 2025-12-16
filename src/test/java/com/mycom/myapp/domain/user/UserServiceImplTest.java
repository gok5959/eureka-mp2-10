package com.mycom.myapp.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.dto.UserSearchCondition;
import com.mycom.myapp.domain.user.dto.UserSignupRequest;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.DuplicatedEmailException;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.service.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    @DisplayName("insertUser - 정상 회원가입 시 UserResponse 반환")
    void insertUser_success() {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setName("홍길동");
        request.setRole(UserRole.USER);

        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("ENCODED_PW");

        User savedUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("홍길동")
                .role(UserRole.USER)
                .passwordHash("ENCODED_PW")
                .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = userService.insertUser(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);

        // 저장되는 User 객체 검증
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(captor.capture());
        User captured = captor.getValue();
        assertThat(captured.getPasswordHash()).isEqualTo("ENCODED_PW");
    }

    @Test
    @DisplayName("insertUser - 중복 이메일이면 DuplicatedEmailException 발생")
    void insertUser_duplicateEmail() {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("dup@test.com");
        request.setPassword("password123");
        request.setName("홍길동");
        request.setRole(UserRole.USER);

        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.insertUser(request))
                .isInstanceOf(DuplicatedEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다");
    }

    @Test
    @DisplayName("findUserById - 유저가 존재하면 UserResponse 반환")
    void findUserById_success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("홍길동")
                .role(UserRole.USER)
                .passwordHash("ENCODED")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.findUserById(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("findUserById - 유저가 없으면 UserNotFoundException")
    void findUserById_notFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUserById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("findUserByEmail - 유저가 존재하면 UserResponse 반환")
    void findUserByEmail_success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("홍길동")
                .role(UserRole.USER)
                .passwordHash("ENCODED")
                .build();

        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(user));

        // when
        UserResponse response = userService.findUserByEmail("test@test.com");

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("findUserByEmail - 유저가 없으면 UserNotFoundException")
    void findUserByEmail_notFound() {
        // given
        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUserByEmail("test@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("searchUsers - 조건/페이지 정보를 그대로 Repository에 위임")
    void searchUsers_delegatesToRepository() {
        // given
        UserSearchCondition condition = UserSearchCondition.builder()
                .keyword("kim")
                .role(UserRole.ADMIN)
                .build();


        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        User user = User.builder()
                .id(1L)
                .email("kim@test.com")
                .name("김철수")
                .role(UserRole.ADMIN)
                .passwordHash("ENC")
                .build();

        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.searchUsers("kim", UserRole.ADMIN, pageable))
                .willReturn(page);

        // when
        var result = userService.searchUsers(condition, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("kim@test.com");

        then(userRepository).should()
                .searchUsers("kim", UserRole.ADMIN, pageable);
    }
}
