package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.AuthTokenService;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthTokenService authTokenService;

    @Operation(summary = "회원가입", description = "신규 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<RsData<User>> register(@RequestBody @Valid UserDto userDto) {
        RsData<User> result = userService.register(userDto);
        return ResponseEntity.status(result.statusCode()).body(result);
    }

    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<RsData<UserDto>> login(@RequestBody @Valid UserDto userDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUserLoginId(), userDto.getPassword())
            );

            User user = userService.findByUserLoginId(userDto.getUserLoginId()).get();
            String token = authTokenService.createToken(user.getUserLoginId(), user.getUsername());

            UserDto response = new UserDto();
            response.setUserLoginId(user.getUserLoginId());
            response.setUsername(user.getUsername());
            response.setToken(token);

            RsData<UserDto> rs = new RsData<>("200-2", "로그인 성공", response);
            return ResponseEntity.status(rs.statusCode()).body(rs);

        } catch (AuthenticationException e) {
            RsData<UserDto> rs = new RsData<>("400-2", "로그인에 실패했습니다.");
            return ResponseEntity.status(rs.statusCode()).body(rs);
        }
    }
}
