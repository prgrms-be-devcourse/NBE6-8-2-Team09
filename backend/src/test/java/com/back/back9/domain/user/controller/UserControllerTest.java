package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.dto.UserRegisterDto;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService.register(new UserRegisterDto(
                "testuser",
                "테스트 사용자",
                "12345678",
                "12345678"
        ));
    }


    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                           "userLoginId": "testuser1",
                                           "username": "테스트 사용자1",
                                           "password": "12345678",
                                           "confirmPassword": "12345678"
                                         }
                                        """.stripIndent())
                )
                .andDo(print());

        User user = userService.findByUserLoginId("testuser1").get();

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("register"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.result.id").value(user.getId()))
                .andExpect(jsonPath("$.result.userLoginId").value(user.getUserLoginId()));
    }

    @Test
    @DisplayName("로그인")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "userLoginId": "testuser",
                                            "password": "12345678"
                                        }
                                        """)
                )
                .andDo(print());

        User user = userService.findByUserLoginId("testuser").get();

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(user.getUsername() + "님 환영합니다."))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.item.id").value(user.getId()))
                .andExpect(jsonPath("$.result.apiKey").value(user.getApiKey()))
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty());

        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(apiKeyCookie).isNotNull(); // Null 체크
                    assertThat(accessTokenCookie).isNotNull();

                    assertThat(apiKeyCookie.getValue()).isEqualTo(user.getApiKey());
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.isHttpOnly()).isTrue();

                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );
    }

    @Test
    @DisplayName("내 정보 조회")
    void t3() throws Exception {
        ResultActions loginResult = mvc
                .perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "userLoginId": "testuser",
                                        "password": "12345678"
                                    }
                                    """)
                )
                .andDo(print());

        Cookie apiKeyCookie = loginResult.andReturn().getResponse().getCookie("apiKey");
        Cookie accessTokenCookie = loginResult.andReturn().getResponse().getCookie("accessToken");

        assertThat(apiKeyCookie).isNotNull(); // Null 체크
        assertThat(accessTokenCookie).isNotNull();

        User actor = userService.findByUserLoginId("testuser").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/users/me")
                                .cookie(apiKeyCookie, accessTokenCookie)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("현재 사용자 정보입니다."))
                .andExpect(jsonPath("$.result.id").value(actor.getId()))
                .andExpect(jsonPath("$.result.userLoginId").value(actor.getUserLoginId()));
    }

    @Test
    @DisplayName("로그아웃")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/users/logout")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andExpect(result -> {
                    Cookie apiKey = result.getResponse().getCookie("apiKey");
                    Cookie accessToken = result.getResponse().getCookie("accessToken");
                    assertThat(apiKey).isNotNull(); // Null 체크
                    assertThat(accessToken).isNotNull();

                    assertThat(apiKey.getValue()).isEmpty();
                    assertThat(apiKey.getMaxAge()).isEqualTo(0);
                    assertThat(apiKey.getPath()).isEqualTo("/");
                    assertThat(apiKey.isHttpOnly()).isTrue();

                    assertThat(accessToken.getValue()).isEmpty();
                    assertThat(accessToken.getMaxAge()).isEqualTo(0);
                    assertThat(accessToken.getPath()).isEqualTo("/");
                    assertThat(accessToken.isHttpOnly()).isTrue();
                });
    }

    @Test
    @DisplayName("Authorization 헤더가 잘못된 형식일 때 오류 발생")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/users/me")
                                .header("Authorization", "invalid-format")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Authorization 헤더가 Bearer 형식이 아닙니다."));
    }
    @Test
    @DisplayName("구글 OAuth2 로그인 엔드포인트 접근 시 구글 로그인 창으로 리다이렉트된다")
    void t6() throws Exception {
        mvc.perform(get("/oauth2/authorization/google"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("https://accounts.google.com/")));
    }
}