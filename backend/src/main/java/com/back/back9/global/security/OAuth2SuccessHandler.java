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


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();

        // User ID가 null인지 확인
        if (user.getId() == null) {
            // User가 아직 저장되지 않은 경우, 먼저 저장
            user = userRepository.save(user);
        }

        String accessToken = userService.genAccessToken(user);
        String role = user.getRole().name();

        // 지갑이 존재하지 않으면 생성
        if (!walletService.existsByUserId(user.getId())) {
            walletService.createWallet(user.getId());
        }

        String apiKey = user.getApiKey();
        String redirectUrl = String.format(
                "http://localhost:3000/api/auth/google/callback?token=%s&apiKey=%s&role=%s",
                accessToken,
                apiKey,
                role
        );
        response.sendRedirect(redirectUrl);
    }
}