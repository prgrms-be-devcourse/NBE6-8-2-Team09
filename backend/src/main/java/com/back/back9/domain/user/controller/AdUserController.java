package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserWithUsernameDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
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
    @Operation(summary = "사용자 단건 조회")
    public UserWithUsernameDto getUser(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new UserWithUsernameDto(user);
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "username으로 사용자 검색")
    public List<UserWithUsernameDto> searchUsersByUsername(@RequestParam String keyword) {
        List<User> users = userService.searchByUsername(keyword);
        return users.stream()
                .map(UserWithUsernameDto::new)
                .toList();
    }
}
