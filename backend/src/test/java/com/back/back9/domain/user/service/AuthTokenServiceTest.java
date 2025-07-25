package com.back.back9.domain.user.service;

import com.back.back9.domain.user.dto.UserRegisterDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.standard.util.Ut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenService authTokenService;

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    @BeforeEach
    void setUp() {
        // 이미 있으면 삭제 또는 생략 (테스트 DB라면 덮어써도 됨)
        if (userService.findByUserLoginId("user1").isEmpty()) {
            // UserRegisterDto 사용해서 회원가입 처리
            userService.register(new UserRegisterDto(
                    "user1",
                    "테스트유저",
                    "123456789",
                    "123456789"
            ));
        }
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
