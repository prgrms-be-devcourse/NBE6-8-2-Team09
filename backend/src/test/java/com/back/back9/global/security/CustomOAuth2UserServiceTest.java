package com.back.back9.global.security;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomOAuth2UserServiceTest {

    static class TestCustomOAuth2UserService extends CustomOAuth2UserService {
        private final UserService userService;
        private final Map<String, Object> attributes;

        public TestCustomOAuth2UserService(UserService userService, Map<String, Object> attributes) {
            super(userService);
            this.userService = userService;
            this.attributes = attributes;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");

            Optional<User> userOpt = userService.findByUserLoginId(email);
            User user;
            if (userOpt.isEmpty()) {
                user = User.builder()
                        .userLoginId(email)
                        .username(name)
                        .password("oauth2")
                        .role(User.UserRole.MEMBER)
                        .apiKey("dummy-api-key")
                        .build();
                userService.register(Mockito.any());
            } else {
                user = userOpt.get();
            }
            return new SecurityUser(user);
        }
    }

    @Test
    @DisplayName("OAuth2 로그인 시 신규 사용자는 DB에 저장된다")
    void testOAuth2UserRegistration() {
        // given
        UserService userService = Mockito.mock(UserService.class);

        Map<String, Object> attributes = Map.of(
                "email", "testuser@example.com",
                "name", "테스트유저"
        );

        Mockito.when(userService.findByUserLoginId("testuser@example.com"))
                .thenReturn(Optional.empty());

        User newUser = User.builder()
                .userLoginId("testuser@example.com")
                .username("테스트유저")
                .password("oauth2")
                .role(User.UserRole.MEMBER)
                .apiKey("dummy-api-key")
                .build();

        Mockito.when(userService.register(Mockito.any()))
                .thenReturn(new com.back.back9.global.rsData.RsData<>("200", "ok", newUser));

        TestCustomOAuth2UserService customOAuth2UserService = new TestCustomOAuth2UserService(userService, attributes);

        OAuth2UserRequest userRequest = Mockito.mock(OAuth2UserRequest.class);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(SecurityUser.class);
        SecurityUser securityUser = (SecurityUser) result;
        assertThat(securityUser.getUser().getUserLoginId()).isEqualTo("testuser@example.com");
        assertThat(securityUser.getUser().getUsername()).isEqualTo("테스트유저");
    }
}