package com.back.back9.global.security;

import com.back.back9.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Lazy
    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String accessToken = userService.genAccessToken(securityUser.getUser());
        String role = securityUser.getUser().getRole().name();

        String redirectUrl = String.format(
                "http://localhost:3000/api/auth/google/callback?token=%s&role=%s",
                accessToken,
                role
        );
        response.sendRedirect(redirectUrl);
    }
}