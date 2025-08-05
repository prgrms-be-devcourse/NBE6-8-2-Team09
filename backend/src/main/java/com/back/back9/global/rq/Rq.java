package com.back.back9.global.rq;

import com.back.back9.domain.user.entity.User;
import com.back.back9.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    /**
     * 현재 인증된 사용자 정보 가져오기
     */
    public User getActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(SecurityUser::getUser)
                .orElse(null);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {
        if (value == null) value = "";

        if (value.isBlank()) {
            req.removeAttribute(name);
        } else {
            resp.setHeader(name, value);
        }
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(cookies ->
                        Arrays.stream(cookies)
                                .filter(cookie -> cookie.getName().equals(name))
                                .map(Cookie::getValue)
                                .filter(value -> !value.isBlank())
                                .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 보안을 위해 true로 설정
        cookie.setSecure(false); // 개발환경
        cookie.setAttribute("SameSite", "Lax"); // Strict 대신 Lax

        if (value.isBlank()) {
            cookie.setMaxAge(0);
            // 추가적으로 과거 날짜로 설정하여 확실한 삭제
            cookie.setAttribute("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        } else {
            cookie.setMaxAge(60 * 60 * 24 * 365);
        }

        resp.addCookie(cookie);

        // 디버깅용 로그 추가
        System.out.println("쿠키 설정: " + name + "=" + value + ", MaxAge=" + cookie.getMaxAge());
    }


    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}
