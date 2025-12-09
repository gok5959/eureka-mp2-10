package com.mycom.myapp.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbConnectionTest {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void testConnection() {
        try {
            String result = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
            log.info("✅ DB 연결 성공! 현재 시간: {}", result);
        } catch (Exception e) {
            log.error("❌ DB 연결 실패: {}", e.getMessage());
        }
    }
}
