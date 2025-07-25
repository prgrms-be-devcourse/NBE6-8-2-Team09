package com.back.back9.domain.user.service;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.standard.util.Ut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenService authTokenService;

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .userLoginId("user1")
                .username("테스트유저")
                .password("123456789")
                .role(User.UserRole.MEMBER)
                .apiKey(UUID.randomUUID().toString())
                .build();

        java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1L);

        when(userRepository.findByUserLoginId("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByApiKey(any(String.class))).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(java.util.List.of(testUser));
        when(userRepository.count()).thenReturn(1L);
    }

    @Test
    @DisplayName("authTokenService 빈이 존재한다.")
    void t1() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("Ut.jwt.toString으로 JWT 생성, {name=Paul, age=23}")
    void t2() {
        Map<String, Object> payload = Map.of("name", "Paul", "age", 23);

        String jwt = Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                payload
        );

        assertThat(jwt).isNotBlank();
        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue();

        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, jwt);

        assertThat(parsedPayload).containsAllEntriesOf(payload);
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(user)")
    void t3() {
        User user = userService.findByUserLoginId("user1").orElseThrow();

        String accessToken = authTokenService.genAccessToken(user);

        assertThat(accessToken).isNotBlank();
        System.out.println("accessToken = " + accessToken);

        Map<String, Object> parsedPayload = authTokenService.payload(accessToken);

        assertThat(parsedPayload).containsAllEntriesOf(
                Map.of(
                        "id", user.getId(),
                        "userLoginId", user.getUserLoginId(),
                        "username", user.getUsername()
                )
        );
    }
}