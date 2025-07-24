package com.back.back9.domain.user.entity;

import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
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

    @Column(unique = true)
    private String apiKey;

    public User(String userLoginId, String username, String password) {
        this.userLoginId = userLoginId;
        this.username = username;
        this.password = password;
        this.role = UserRole.MEMBER;
        this.apiKey = UUID.randomUUID().toString();
    }

    public enum UserRole {
        MEMBER, ADMIN
    }

    public void modifyApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = new ArrayList<>();
        if (isAdmin()) {
            roles.add("ROLE_ADMIN");
        } else {
            roles.add("ROLE_MEMBER");
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
