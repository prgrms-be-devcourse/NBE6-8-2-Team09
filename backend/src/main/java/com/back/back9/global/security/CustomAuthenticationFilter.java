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
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        List<String> openApiUris = List.of(
                "/api/v1/users/login",
                "/api/v1/users/register",
                "/api/v1/users/logout",
                "/api/v1/users/register-admin"
        );
        if (openApiUris.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

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

        logger.debug("apiKey: " + apiKey);
        logger.debug("accessToken: " + accessToken);

        boolean hasApiKey = !apiKey.isBlank();
        boolean hasAccessToken = !accessToken.isBlank();

        if (!hasApiKey && !hasAccessToken) {
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