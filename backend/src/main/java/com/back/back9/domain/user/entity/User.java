package com.back.back9.domain.user.entity;

import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "user_login_id", nullable = false, unique = true)
    private String userLoginId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String password;

    public enum UserRole {
        MEMBER, ADMIN
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
