package com.mycom.myapp.domain.auth.repository;

import com.mycom.myapp.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByJti(String jti);
    void deleteByUser_Id(Long userId);
}
