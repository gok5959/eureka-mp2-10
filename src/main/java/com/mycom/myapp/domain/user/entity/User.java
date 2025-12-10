package com.mycom.myapp.domain.user.entity;

import com.mycom.myapp.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // business logic
    public void changeName(String name) {
        this.name = name;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }
}
