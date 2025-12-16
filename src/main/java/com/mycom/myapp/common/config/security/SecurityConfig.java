package com.mycom.myapp.common.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.common.config.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

import static org.springframework.http.HttpMethod.POST;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 인증되지 않은 사용자의 브라우저 요청은 /login으로 리다이렉트
        AuthenticationEntryPoint loginRedirectEntryPoint =
                new LoginUrlAuthenticationEntryPoint("/login");

        // 인증되지 않은 사용자의 API 요청은 401 JSON
        AuthenticationEntryPoint apiEntryPoint = (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            var body = new LinkedHashMap<String, Object>();
            body.put("status", 401);
            body.put("code", "AUTH_401");
            body.put("message", "인증이 필요합니다.");
            body.put("path", request.getRequestURI());
            new ObjectMapper().writeValue(response.getWriter(), body);
        };

        // HTML 요청 판단(브라우저)
        RequestMatcher htmlRequest = new OrRequestMatcher(
                new AntPathRequestMatcher("/pages/**"),
                new AntPathRequestMatcher("/templates/**"),
                new AntPathRequestMatcher("/login"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML) // Accept: text/html
        );


        var entryPoints = new LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>();
        entryPoints.put(htmlRequest, loginRedirectEntryPoint);
        var delegatingEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        delegatingEntryPoint.setDefaultEntryPoint(apiEntryPoint);

        // (선택) 권한 부족(403)은 따로 처리하고 싶으면:
        AccessDeniedHandler accessDeniedHandler = (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            var body = new LinkedHashMap<String, Object>();
            body.put("status", 403);
            body.put("code", "AUTH_403");
            body.put("message", "권한이 없습니다.");
            body.put("path", request.getRequestURI());
            new ObjectMapper().writeValue(response.getWriter(), body);
        };

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(delegatingEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/js/**", "/pages/**", "/templates/**", "/.well-known/**").permitAll()
                        .requestMatchers("/auth/**", "/testmain", "/testmain/**", "/signup", "/login").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/users").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


}
