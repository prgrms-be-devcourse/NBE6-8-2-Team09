package com.back.back9.global.rq;

import com.back.back9.domain.user.entity.User;
import com.back.back9.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    // after (하이픈 표기와 일치)
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.max-age-seconds:31536000}")
    private long cookieMaxAgeSeconds;

    /** 현재 인증된 사용자 반환(null 허용) */
    public User getActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof SecurityUser su) {
            return su.getUser();
        }
        return null;
    }

    /** 요청 헤더 조회(빈 문자열은 default 반환) */
    public String getHeader(String name, String defaultValue) {
        String v = req.getHeader(name);
        return (StringUtils.hasText(v)) ? v : defaultValue;
    }

    /** 응답 헤더 세팅(비우면 빈 값으로 세팅) */
    public void setHeader(String name, String value) {
        resp.setHeader(name, (value == null) ? "" : value);
    }

    /** 요청 쿠키 값 조회 (jakarta로 통일) */
    public String getCookieValue(String name, String defaultValue) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return defaultValue;
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)   // ★ jakarta.servlet.http.Cookie
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(defaultValue);
    }

    /** HttpOnly 쿠키 설정(기본 maxAge=설정값) */
    public void setCookie(String name, String value) {
        setCookie(name, value, Duration.ofSeconds(cookieMaxAgeSeconds), true, "/");
    }

    /** 쿠키 삭제(설정 때와 동일 속성으로) */
    public void deleteCookie(String name) {
        setCookie(name, null, Duration.ZERO, true, "/");
    }

    /** 필요 시 HttpOnly/Path/MaxAge 커스텀 */
    public void setCookie(String name, String value, Duration maxAge, boolean httpOnly, String path) {
        boolean delete = !StringUtils.hasText(value);

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie
                .from(name, delete ? "" : value)
                .httpOnly(httpOnly)
                .secure(cookieSecure && isHttps())  // HTTPS일 때만 secure=true
                .path((path == null || path.isBlank()) ? "/" : path)
                .sameSite(cookieSameSite);

        // Host-Only: domain 생략 / 공유 필요 시만 지정
        if (StringUtils.hasText(cookieDomain)) {
            b.domain(cookieDomain);
        }

        b.maxAge(delete ? Duration.ZERO : maxAge);

        resp.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    /** 현재 요청이 HTTPS 인지 단순 조회 (프록시 헤더 포함) */
    public boolean isHttps() {
        String proto = req.getHeader("X-Forwarded-Proto");
        if (StringUtils.hasText(proto)) {
            return "https".equalsIgnoreCase(proto);
        }
        return "https".equalsIgnoreCase(req.getScheme());
    }

    /** 디버그용: 쿠키 정책 문자열 */
    public String cookiePolicyDebug() {
        return "domain=" + (StringUtils.hasText(cookieDomain) ? cookieDomain : "(host-only)")
                + ", secure=" + cookieSecure
                + ", sameSite=" + cookieSameSite
                + ", maxAgeSec=" + cookieMaxAgeSeconds;
    }
}
