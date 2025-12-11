package com.mycom.myapp.domain.user.repository;

import com.mycom.myapp.domain.user.UserRole;
import com.mycom.myapp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") UserRole role,
                           Pageable pageable);
}