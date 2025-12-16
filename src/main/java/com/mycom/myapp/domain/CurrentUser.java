package com.mycom.myapp.domain;

import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class CurrentUser {
    private CurrentUser() {}

    // 개발용: 인증 없으면 devUserId(예: 1L)로 대체
    public static Long idOrDev(Authentication auth, Long devUserId) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return devUserId;
        try {
            return id(auth);
        } catch (Exception e) {
            return devUserId;
        }
    }

    public static Long id(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("인증 정보 없음");
        }

        Object principal = auth.getPrincipal();

        // 혹시 나중에 커스텀 필터가 Map으로 principal 넣는 경우 대비
        if (principal instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) principal;
            Object v = map.get("userId"); // 나중에 키 맞추기
            if (v instanceof Number) return ((Number) v).longValue();
            if (v instanceof String) return Long.parseLong((String) v);
        }

        throw new IllegalStateException("userId 추출 실패: principal=" + principal);
    }
}
