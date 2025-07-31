package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.global.exception.ServiceException;
import com.back.back9.global.rq.Rq;
import com.back.back9.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "API 사용자 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final Rq rq;

    public record UserRegisterReqBody(
            @NotBlank @Size(min = 2, max = 30) String userLoginId,
            @NotBlank @Size(min = 2, max = 30) String username,
            @NotBlank @Size(min = 2, max = 30) String password,
            @NotBlank @Size(min = 2, max = 30) String confirmPassword
    ) {}

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "회원가입")
    public RsData<UserDto> register(@Valid @RequestBody UserRegisterReqBody reqBody) {
        if (!reqBody.password().equals(reqBody.confirmPassword())) {
            return new RsData<>("400", "비밀번호 확인이 일치하지 않습니다.");
        }

        RsData<User> registerResult = userService.register(
                new com.back.back9.domain.user.dto.UserRegisterDto(
                        reqBody.userLoginId(),
                        reqBody.username(),
                        reqBody.password(),
                        reqBody.confirmPassword()
                )
        );

        if (!registerResult.resultCode().startsWith("200")) {
            return new RsData<>(registerResult.resultCode(), registerResult.msg());
        }

        User user = registerResult.data();

        return new RsData<>(
                "201",
                "회원가입이 완료되었습니다.",
                new UserDto(user)
        );
    }

    @PostMapping("/register-admin")
    @Transactional
    @Operation(summary = "관리자 회원가입")
    public RsData<UserDto> registerAdmin(@Valid @RequestBody UserRegisterReqBody reqBody) {
        RsData<User> registerResult = userService.registerAdmin(
                new com.back.back9.domain.user.dto.UserRegisterDto(
                        reqBody.userLoginId(),
                        reqBody.username(),
                        reqBody.password(),
                        reqBody.confirmPassword()
                )
        );

        if (!registerResult.resultCode().startsWith("200")) {
            return new RsData<>(registerResult.resultCode(), registerResult.msg());
        }

        User user = registerResult.data();
        return new RsData<>(
                "201",
                "관리자 회원가입이 완료되었습니다.",
                new UserDto(user)
        );
    }


    public record UserLoginReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String userLoginId,
            @NotBlank
            @Size(min = 2, max = 30)
            String password
    ) {}

    public record UserLoginResBody(
            UserDto item,
            String apiKey,
            String accessToken
    ) {}

    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "로그인")
    public RsData<UserLoginResBody> login(@Valid @RequestBody UserLoginReqBody reqBody) {
        User actor = rq.getActor();
        if (actor == null) {
            // 쿠키에서 apiKey 확인
            String apiKey = rq.getCookieValue("apiKey", null);
            if (apiKey != null && !apiKey.isBlank()) {
                // UserService로 사용자 조회
                if (userService.findByApiKey(apiKey).isPresent()) {
                    return new RsData<>("400", "이미 로그인된 상태입니다.");
                }
            }
        } else {
            return new RsData<>("400", "이미 로그인된 상태입니다.");
        }

        User user = userService.findByUserLoginId(reqBody.userLoginId())
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 아이디입니다."));

        userService.checkPassword(user, reqBody.password());

        String accessToken = userService.genAccessToken(user);

        rq.setCookie("apiKey", user.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(user.getUsername()),
                new UserLoginResBody(new UserDto(user), user.getApiKey(), accessToken)
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    public RsData<Void> logout() {
        rq.deleteCookie("apiKey");
        rq.deleteCookie("access_token");
        return new RsData<>("200-1", "로그아웃 되었습니다.");
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 정보 조회")
    public RsData<UserDto> me() {
        User actor = rq.getActor();
        if (actor == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        return new RsData<>("200", "현재 사용자 정보입니다.", new UserDto(actor));
    }
}
