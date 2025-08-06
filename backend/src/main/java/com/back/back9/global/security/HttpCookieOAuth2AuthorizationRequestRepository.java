package com.back.back9.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;

/**
 * 쿠키에 OAuth2AuthorizationRequest 를 저장/로드/삭제하는 구현체
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME       = "frontend-url"; // ← 변경

    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Value("${app.oauth2.cookie-domain:}")
    private String cookieDomain;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        System.out.println("=== OAuth2 쿠키 로드 시도 ===");
        System.out.println("요청 URI: " + request.getRequestURI());
        System.out.println("쿠키 존재 여부: " + (cookie != null));

        if (cookie != null) {
            System.out.println("쿠키 값: " + cookie.getValue());
            try {
                OAuth2AuthorizationRequest authReq = deserialize(cookie.getValue());
                System.out.println("역직렬화 성공");
                return authReq;
            } catch (Exception e) {
                System.out.println("역직렬화 실패: " + e.getMessage());
                return null;
            }
        } else {
            System.out.println("쿠키를 찾을 수 없음");
            // 모든 쿠키 출력
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                System.out.println("사용 가능한 쿠키들:");
                for (Cookie c : cookies) {
                    System.out.println("- " + c.getName() + ": " + c.getValue());
                }
            }
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        String serialized = serialize(authorizationRequest);

        // 1) OAuth2 인가 요청 쿠키 (SameSite=None; Secure 추가)
        String authCookieHeader = String.format(
                "%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=None; Secure%s",
                OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                serialized,
                COOKIE_EXPIRE_SECONDS,
                cookieDomain.isEmpty() ? "" : "; Domain=" + cookieDomain
        );
        response.addHeader("Set-Cookie", authCookieHeader);

        // 2) (선택) 리다이렉트 URI 쿠키도 같은 플래그로
        String redirectUri = request.getParameter("frontend-url");
        if (redirectUri != null && !redirectUri.isBlank()) {
            String redirectCookieHeader = String.format(
                    "%s=%s; Path=/; Max-Age=%d; SameSite=None; Secure%s",
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUri,
                    COOKIE_EXPIRE_SECONDS,
                    cookieDomain.isEmpty() ? "" : "; Domain=" + cookieDomain
            );
            response.addHeader("Set-Cookie", redirectCookieHeader);
        }
    }



    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        // removeAuthorizationRequest(HttpServletRequest, HttpServletResponse) 호출 시 이 메소드는 사용되지 않음
        return loadAuthorizationRequest(request);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authReq = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authReq;
    }

    private void removeAuthorizationRequestCookies(HttpServletRequest request,
                                                   HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);

        if (!cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);

        // 헤더로도 삭제
        String cookieHeader = String.format("%s=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax",
                OAUTH2_AUTH_REQUEST_COOKIE_NAME);

        if (!cookieDomain.isEmpty()) {
            cookieHeader += "; Domain=" + cookieDomain;
        }

        response.addHeader("Set-Cookie", cookieHeader);
    }

    // (De)serialize 유틸 메서드: 예시로 Base64 + Java 직렬화 사용
    private String serialize(OAuth2AuthorizationRequest obj) {
        try {
            byte[] bytes = SerializationUtils.serialize(obj);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize OAuth2AuthorizationRequest", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String serialized) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(serialized);
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize OAuth2AuthorizationRequest", e);
        }
    }
}
