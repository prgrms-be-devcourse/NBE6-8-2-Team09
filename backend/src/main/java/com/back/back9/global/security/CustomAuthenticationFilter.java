package com.back.back9.global.security;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.global.exception.ServiceException;
import com.back.back9.global.rq.Rq;
import com.back.back9.global.rsData.RsData;
import com.back.back9.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    @Lazy
    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext applicationContext;

    private Rq getRq(HttpServletRequest request, HttpServletResponse response) {
        return applicationContext.getBean(Rq.class, request, response);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Processing request for " + request.getRequestURI());

        Rq rq = getRq(request, response);

        try {
            doFilterLogic(request, response, filterChain, rq);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(
                    Ut.json.toString(rsData)
            );
        } catch (Exception e) {
            throw e;
        }
    }

    private void doFilterLogic(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Rq rq) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        System.out.println("=== CustomAuthenticationFilter 요청 처리 ===");
        System.out.println("요청 URI: " + requestURI);
        System.out.println("요청 메소드: " + request.getMethod());

        // OAuth2 관련 경로는 필터를 건너뛰도록 수정
        if (!requestURI.startsWith("/api/") ||
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/login/oauth2/") ||
                requestURI.startsWith("/api/auth/")) { // 모든 auth 경로 허용
            System.out.println("인증 필터 건너뛰기: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        List<String> openApiUris = List.of(
                "/api/v1/users/login",
                "/api/v1/users/register",
                "/api/v1/users/logout",
                "/api/v1/users/register-admin"
        );
        if (openApiUris.contains(requestURI)) {
            System.out.println("공개 API 경로: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("인증 필터 처리 시작: " + requestURI);

        String apiKey;
        String accessToken;

        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");

            String[] parts = headerAuthorization.split(" ", 3);
            apiKey = parts[1];
            accessToken = parts.length == 3 ? parts[2] : "";
        } else {
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
        }

        System.out.println("=== 쿠키/헤더 확인 ===");
        System.out.println("Authorization 헤더: " + (headerAuthorization.isBlank() ? "없음" : "존재"));
        System.out.println("apiKey 쿠키: " + (apiKey.isBlank() ? "없음" : "존재 - " + apiKey));
        System.out.println("accessToken 쿠키: " + (accessToken.isBlank() ? "없음" : "존재 - 길이: " + accessToken.length()));

        boolean hasApiKey = !apiKey.isBlank();
        boolean hasAccessToken = !accessToken.isBlank();

        System.out.println("hasApiKey: " + hasApiKey + ", hasAccessToken: " + hasAccessToken);

        if (!hasApiKey && !hasAccessToken) {
            System.out.println("토큰이 없어서 필터 통과 - 401 오류 발생 예상");
            filterChain.doFilter(request, response);
            return;
        }

        User user = null;
        boolean isAccessTokenValid = false;

        if (hasAccessToken) {
            Map<String, Object> payload = userService.getPayloadFromToken(accessToken);

            if (payload != null) {
                Long id = ((Number) payload.get("id")).longValue();
                String userLoginId = (String) payload.get("userLoginId");
                String username = (String) payload.get("username");

                user = userService.findById(id)
                        .orElse(null);

                if (user != null) {
                    isAccessTokenValid = true;
                }
            }
        }

        if (user == null && hasApiKey) {
            user = userService.findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));
        }

        if (hasAccessToken && !isAccessTokenValid) {
            String newAccessToken = userService.genAccessToken(user);
            rq.setCookie("accessToken", newAccessToken);
            rq.setHeader("Authorization", "Bearer " + newAccessToken);
        }

        UserDetails userDetails = new SecurityUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}