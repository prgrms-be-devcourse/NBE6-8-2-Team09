package com.back.back9.domain.user.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    private String userLoginId;
    private String username;
    private String password;
    private String confirmPassword;
}