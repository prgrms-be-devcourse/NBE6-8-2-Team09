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
        System.out.println("=== OAuth2 쿠키 저장 시도 ===");
        System.out.println("요청 URI: " + request.getRequestURI());
        System.out.println("인증 요청 존재 여부: " + (authorizationRequest != null));

        if (authorizationRequest == null) {
            System.out.println("인증 요청이 null이므로 쿠키 삭제");
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        try {
            String serialized = serialize(authorizationRequest);
            System.out.println("직렬화 성공, 길이: " + serialized.length());

            Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);

            // 크로스 도메인 환경을 위한 설정
            if (!cookieDomain.isEmpty()) {
                cookie.setDomain(cookieDomain);
                System.out.println("쿠키 도메인 설정: " + cookieDomain);
            }

            // SameSite 속성 설정을 위해 헤더로 직접 설정
            String cookieHeader = String.format("%s=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                    OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);

            if (!cookieDomain.isEmpty()) {
                cookieHeader += "; Domain=" + cookieDomain;
            }

            System.out.println("쿠키 헤더: " + cookieHeader);
            response.addHeader("Set-Cookie", cookieHeader);
            System.out.println("쿠키 저장 완료");
        } catch (Exception e) {
            System.out.println("쿠키 저장 실패: " + e.getMessage());
            e.printStackTrace();
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
