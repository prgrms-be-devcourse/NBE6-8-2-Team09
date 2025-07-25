package com.back.back9.global.security;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final @Lazy UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userService.findByUserLoginId(email)
                .orElseGet(() -> userService.register(
                        new com.back.back9.domain.user.dto.UserRegisterDto(
                                email, name, "oauth2", "oauth2"
                        )
                ).data());

        return new SecurityUser(user);
    }
}