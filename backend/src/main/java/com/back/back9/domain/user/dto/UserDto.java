package com.back.back9.domain.user.dto;

import com.back.back9.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String userLoginId,
        String username,
        String role,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public UserDto(Long id, String userLoginId, String username, String role, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userLoginId = userLoginId;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public UserDto(User user) {
        this(
                user.getId(),
                user.getUserLoginId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
