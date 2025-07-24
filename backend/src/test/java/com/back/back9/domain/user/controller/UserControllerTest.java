package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "userLoginId": "newuser",
                                            "username": "신규유저",
                                            "password": "password123",
                                            "confirmPassword": "password123"
                                        }
                                        """)
                )
                .andDo(print());

        User user = userService.findByUserLoginId("newuser").get();

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("register"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()));
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
                                            "userLoginId": "user1",
                                            "password": "12341234"
                                        }
                                        """)
                )
                .andDo(print());

        User user = userService.findByUserLoginId("user1").get();

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value(user.getUsername() + "님 환영합니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.item.id").value(user.getId()))
                .andExpect(jsonPath("$.data.apiKey").value(user.getApiKey()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEqualTo(user.getApiKey());
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                }
        );
    }

    @Test
    @DisplayName("내 정보 조회")
    void t3() throws Exception {
        User actor = userService.findByUserLoginId("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/users/me")
                                .cookie(new Cookie("apiKey", actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("현재 사용자 정보입니다."))
                .andExpect(jsonPath("$.data.id").value(actor.getId()))
                .andExpect(jsonPath("$.data.username").value(actor.getUsername()))
                .andExpect(jsonPath("$.data.userLoginId").value(actor.getUserLoginId()));
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
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
                .andExpect(result -> {
                    Cookie apiKey = result.getResponse().getCookie("apiKey");
                    Cookie accessToken = result.getResponse().getCookie("accessToken");

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
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."));
    }
}
