package com.mycom.myapp.domain.auth.entity;

import com.mycom.myapp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(length = 64)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash; // 원문 저장 X (SHA-256 등)

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by", length = 64)
    private String replacedBy; // rotation 시 새 jti

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revokeNow(String replacedBy) {
        this.revokedAt = Instant.now();
        this.replacedBy = replacedBy;
    }

    public static RefreshToken of(String jti, User user, String tokenHash, Instant expiresAt) {
        return RefreshToken.builder()
                .jti(jti)
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();
    }
}
