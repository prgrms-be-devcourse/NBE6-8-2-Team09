package com.back.back9.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_login_id", nullable = false, unique = true)
    private String userLoginId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "user_role", nullable = false)
    private String role = "MEMBER";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String password;
}
