package com.back.back9.domain.user.dto;

import com.back.back9.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserWithUsernameDto(
        Long id,
        String username,
        String userLoginId,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public UserWithUsernameDto(Long id, String username, String userLoginId, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.username = username;
        this.userLoginId = userLoginId;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
    public UserWithUsernameDto(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getUserLoginId(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
