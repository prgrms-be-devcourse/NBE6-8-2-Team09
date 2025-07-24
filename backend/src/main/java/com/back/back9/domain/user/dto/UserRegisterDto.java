package com.back.back9.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDto(
        @NotBlank
        String userLoginId,
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotBlank
        String confirmPassword
) {
    public UserRegisterDto {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("패스워드가 일치하지 않습니다");
        }
    }
}
