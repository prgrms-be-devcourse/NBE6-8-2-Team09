package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserWithUsernameDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/adm/users")
@RequiredArgsConstructor
@Tag(name = "AdUserController", description = "관리자용 사용자 API")
@SecurityRequirement(name = "bearerAuth")
public class AdUserController {

    private final UserService userService;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "전체 사용자 조회")
    public RsData<List<UserWithUsernameDto>> getUsers() {
        List<User> users = userService.findAll();
        List<UserWithUsernameDto> dtos = users.stream()
                .map(UserWithUsernameDto::new)
                .toList();
        return new RsData<>(
                "200",
                "사용자 정보를 성공적으로 조회했습니다.",
                dtos
        );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "id로 사용자 조회")
    public RsData<UserWithUsernameDto> getUser(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ErrorException(ErrorCode.USER_NOT_FOUND, id));

        return new RsData<>(
                "200",
                "사용자 정보를 성공적으로 조회했습니다.",
                new UserWithUsernameDto(user)
        );
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "username으로 사용자 검색")
    public RsData<List<UserWithUsernameDto>> searchUsersByUsername(@RequestParam String keyword) {
        List<User> users = userService.searchByUsername(keyword);

        List<UserWithUsernameDto> dtos = users.stream()
                .map(UserWithUsernameDto::new)
                .toList();

        return new RsData<>(
                "200",
                "사용자 검색 결과입니다.",
                dtos
        );
    }
}