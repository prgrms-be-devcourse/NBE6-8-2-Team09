package com.back.back9.global.security;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.domain.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Lazy
    @Autowired
    private UserService userService;

    @Lazy
    @Autowired
    private WalletService walletService;

    @Lazy
    @Autowired
    private UserRepository userRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${app.oauth2.frontend-url}")
    private String frontendUrl;

    @Value("${app.oauth2.cookie-domain:}")
    private String cookieDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        System.out.println("=== OAuth2SuccessHandler 시작 ===");
        System.out.println("활성 프로파일: " + activeProfile);

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();

        System.out.println("인증된 사용자: " + user.getUserLoginId());

        // User ID가 null인지 확인
        if (user.getId() == null) {
            System.out.println("사용자 ID가 null이므로 저장 필요");
            user = userRepository.save(user);
            System.out.println("사용자 저장 완료, ID: " + user.getId());
        }

        String accessToken = userService.genAccessToken(user);
        String role = user.getRole().name();

        System.out.println("액세스 토큰 생성 완료");
        System.out.println("사용자 역할: " + role);

        // 지갑이 존재하지 않으면 생성
        if (!walletService.existsByUserId(user.getId())) {
            System.out.println("지갑이 존재하지 않아 생성");
            walletService.createWallet(user.getId());
        } else {
            System.out.println("지갑이 이미 존재함");
        }

        String apiKey = user.getApiKey();

        System.out.println("=== 쿠키 설정 시작 ===");
        System.out.println("accessToken: " + accessToken);
        System.out.println("apiKey: " + apiKey);
        System.out.println("role: " + role);

        // 환경별 쿠키 설정
        boolean isLocal = "dev".equals(activeProfile);

        if (isLocal) {
            // 로컬 환경: 일반 쿠키 설정
            System.out.println("로컬 환경 - 일반 쿠키 설정");

            String accessTokenCookie = String.format(
                    "accessToken=%s; Path=/; HttpOnly=false; SameSite=Lax; Max-Age=3600",
                    accessToken
            );

            String apiKeyCookie = String.format(
                    "apiKey=%s; Path=/; HttpOnly=false; SameSite=Lax; Max-Age=3600",
                    apiKey
            );

            String roleCookie = String.format(
                    "role=%s; Path=/; HttpOnly=false; SameSite=Lax; Max-Age=3600",
                    role
            );

            response.addHeader("Set-Cookie", accessTokenCookie);
            response.addHeader("Set-Cookie", apiKeyCookie);
            response.addHeader("Set-Cookie", roleCookie);
        } else {
            // 프로덕션 환경: 크로스 도메인 쿠키 설정
            System.out.println("프로덕션 환경 - 크로스 도메인 쿠키 설정");

            String accessTokenCookie = String.format(
                    "accessToken=%s; Path=/; HttpOnly=false; Secure=true; SameSite=None; Domain=%s",
                    accessToken, cookieDomain
            );

            String apiKeyCookie = String.format(
                    "apiKey=%s; Path=/; HttpOnly=false; Secure=true; SameSite=None; Domain=%s",
                    apiKey, cookieDomain
            );

            String roleCookie = String.format(
                    "role=%s; Path=/; HttpOnly=false; Secure=true; SameSite=None; Domain=%s",
                    role, cookieDomain
            );

            response.addHeader("Set-Cookie", accessTokenCookie);
            response.addHeader("Set-Cookie", apiKeyCookie);
            response.addHeader("Set-Cookie", roleCookie);
        }

        System.out.println("Set-Cookie 헤더 추가 완료");

        // 환경별 리다이렉트 URL 설정
        String redirectUrl;

        if (isLocal) {
            // 로컬 환경: nginx를 통한 리다이렉트
            redirectUrl = String.format(
                    "%s/api/auth/google/callback?token=%s&apiKey=%s&role=%s",
                    frontendUrl, accessToken, apiKey, role
            );
            System.out.println("로컬 환경 - nginx 8888 포트 연결");
        } else {
            // 프로덕션 환경: API 라우트를 통한 쿠키 재설정
            redirectUrl = String.format(
                    "%s/api/auth/google/callback?token=%s&apiKey=%s&role=%s",
                    frontendUrl, accessToken, apiKey, role
            );
            System.out.println("프로덕션 환경 - API 라우트 활용");
        }

        System.out.println("리다이렉트 URL: " + redirectUrl);

        try {
            response.sendRedirect(redirectUrl);
            System.out.println("리다이렉트 완료");
        } catch (Exception e) {
            System.out.println("리다이렉트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
