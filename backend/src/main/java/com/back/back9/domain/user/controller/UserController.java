package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.global.rq.Rq;
import com.back.back9.global.rsData.RsData;
import com.back.back9.global.security.HttpCookieOAuth2AuthorizationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "API 사용자 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final Rq rq;

    public record UserRegisterReqBody(
            @NotBlank @Size(min = 2, max = 30) String userLoginId,
            @NotBlank @Size(min = 2, max = 30) String username,
            @NotBlank @Size(min = 2, max = 30) String password,
            @NotBlank @Size(min = 2, max = 30) String confirmPassword
    ) {}

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public RsData<UserDto> register(@Valid @RequestBody UserRegisterReqBody reqBody) {
        log.info("회원가입 요청: {}", reqBody);
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
        return new RsData<>("201", registerResult.msg(), new UserDto(registerResult.data()));
    }

    @PostMapping("/register-admin")
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
        return new RsData<>("201", registerResult.msg(), new UserDto(registerResult.data()));
    }

    public record UserLoginReqBody(
            @NotBlank @Size(min = 2, max = 30) String userLoginId,
            @NotBlank @Size(min = 2, max = 30) String password
    ) {}

    public record UserLoginResBody(
            UserDto item,
            String apiKey,
            String accessToken
    ) {}

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<UserLoginResBody> login(@Valid @RequestBody UserLoginReqBody reqBody) {
        User actor = rq.getActor();
        if (actor != null || (rq.getCookieValue("apiKey", null) != null && userService.findByApiKey(rq.getCookieValue("apiKey", null)).isPresent())) {
            return new RsData<>("400", "이미 로그인된 상태입니다.");
        }

        RsData<User> loginResult = userService.login(reqBody.userLoginId(), reqBody.password());
        if (!loginResult.resultCode().startsWith("200")) {
            return new RsData<>(loginResult.resultCode(), loginResult.msg());
        }

        User user = loginResult.data();
        String accessToken = userService.genAccessToken(user);

        rq.setCookie("apiKey", user.getApiKey());
        rq.setCookie("accessToken", accessToken);
        rq.setCookie("role", user.getRole().name());

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(user.getUsername()),
                new UserLoginResBody(new UserDto(user), user.getApiKey(), accessToken)
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "통합 로그아웃 (OAuth + JWT/쿠키)")
    public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1) OAuth 세션 무효화
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            log.info("OAuth 세션 사용자 로그아웃 처리 완료");
        }

        // 2) JSESSIONID 만료
        ResponseCookie jsession = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .domain(".peuronteuendeu.onrender.com")
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jsession.toString());

        // 3) JWT 쿠키들 삭제
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");
        rq.deleteCookie("role");

        // 4) OAuth2 요청용 쿠키들 삭제
        rq.deleteCookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME);  // "oauth2_auth_request"
        rq.deleteCookie(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME); // e.g. "redirect_uri"

        // 5) SecurityContext 클리어
        SecurityContextHolder.clearContext();
        log.info("통합 로그아웃 처리 완료");

        return new RsData<>("200-1", "로그아웃 되었습니다.");
    }


    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public RsData<UserDto> me() {
        User actor = rq.getActor();
        if (actor == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        return new RsData<>("200", "현재 사용자 정보입니다.", new UserDto(actor));
    }
}
