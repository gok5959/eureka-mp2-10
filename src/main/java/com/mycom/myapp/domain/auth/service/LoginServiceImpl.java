package com.mycom.myapp.domain.auth.service;

import com.mycom.myapp.common.config.security.jwt.JwtUtil;
import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.exception.InvalidCredentialsException;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.exception.UserNotFoundException;
import com.mycom.myapp.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String ACCESS_COOKIE_NAME = "access_token";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("로그인 대상 사용자를 찾을 수 없습니다: " + loginRequest.getEmail()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("이메일 혹은 비밀번호가 올바르지 않습니다.");
        }

        String access = jwtUtil.createAccessToken(user.getId(), user.getRole().name());

        String jti = UUID.randomUUID().toString();
        String refresh = jwtUtil.createRefreshToken(user.getId(), jti);

        refreshTokenRepository.save(RefreshToken.of(jti, user, sha256(refresh), Instant.now().plusSeconds(jwtUtil.getRefreshTtlSeconds())));

        setRefreshCookie(response, refresh);
        setAccessCookie(response, access);

        return LoginResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(UserResponse.from(user))
                .build();
    }

    @Override
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = readCookie(request, REFRESH_COOKIE_NAME);
        if (refresh == null) throw new RuntimeException("리프레시 쿠키를 찾을 수 없습니다: " + Arrays.toString(request.getCookies()));

        Claims parsed = jwtUtil.parse(refresh);
        if(!jwtUtil.isRefresh(parsed)) throw new RuntimeException("리프레시 토큰이 아닙니다: " + Arrays.toString(request.getCookies()));

        String oldJti = parsed.getId();
        Long userId = Long.valueOf(parsed.getSubject());

        RefreshToken old = refreshTokenRepository.findByJti(oldJti)
                .orElseThrow(() -> new RuntimeException("리프레시 토큰 정보를 찾을 수 없습니다: " + oldJti));

        if(old.isRevoked() || old.isExpired()) throw new RuntimeException("유효하지 않은 리프레시 토큰입니다: " + oldJti);
        if(!old.getTokenHash().equals(sha256(refresh))) throw new RuntimeException("유효하지 않은 리프레시 토큰입니다: " + oldJti);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        String newJti = UUID.randomUUID().toString();
        String newRefresh = jwtUtil.createRefreshToken(user.getId(), newJti);

        refreshTokenRepository.save(
                RefreshToken.of(newJti, user, sha256(newRefresh),
                        Instant.now().plusSeconds(jwtUtil.getRefreshTtlSeconds()))
        );

        String newAccess = jwtUtil.createAccessToken(user.getId(), user.getRole().name());
        setRefreshCookie(response, newRefresh);
        setAccessCookie(response, newAccess);

        return newAccess;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refresh = readCookie(request, REFRESH_COOKIE_NAME);
        if (refresh != null) {
            try {
                Claims claims = jwtUtil.parse(refresh);
                String jti = claims.getId();
                refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                    rt.revokeNow(null);
                    refreshTokenRepository.save(rt);
                });
            } catch (Exception ignored) {}
        }
        clearRefreshCookie(response);
        clearAccessCookie(response);
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] byteData = token.getBytes();
            byte[] hashBytes = digest.digest(byteData);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException(e);
        }
    }

    private void setRefreshCookie(HttpServletResponse res, String refresh) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth");
        cookie.setMaxAge((int) jwtUtil.getRefreshTtlSeconds());
        res.addCookie(cookie);
    }

    private void setAccessCookie(HttpServletResponse res, String accessToken) {
        Cookie cookie = new Cookie(ACCESS_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtUtil.getAccessTtlSeconds());
        res.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    private void clearAccessCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie(ACCESS_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    private String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }
}
