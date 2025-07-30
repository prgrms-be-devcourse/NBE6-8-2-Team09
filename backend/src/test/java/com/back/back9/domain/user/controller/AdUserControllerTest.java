package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.domain.user.dto.UserRegisterDto;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdUserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    private Cookie apiKeyCookie;
    private Cookie accessTokenCookie;

    @BeforeAll
    void setUpAdmin() throws Exception {
        if (userService.findByUserLoginId("admin").isEmpty()) {
            userService.register(new UserRegisterDto(
                    "admin",
                    "관리자",
                    "admin1234",
                    "admin1234"
            ));
            User admin = userService.findByUserLoginId("admin").get();
            admin.setRole(User.UserRole.ADMIN);
            userService.save(admin);
        }

        // 로그인하여 인증 쿠키 획득
        ResultActions loginResult = mvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "userLoginId": "admin",
                    "password": "admin1234"
                }
            """))
                .andDo(print());

        apiKeyCookie = loginResult.andReturn().getResponse().getCookie("apiKey");
        accessTokenCookie = loginResult.andReturn().getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("전체 사용자 조회 - ADMIN 권한")
    void getAllUsers_withAdmin() throws Exception {
        List<User> users = userService.findAll();

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users")
                        .cookie(apiKeyCookie, accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(users.size()));

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            resultActions
                    .andExpect(jsonPath("$.result[%d].id".formatted(i)).value(user.getId()))
                    .andExpect(jsonPath("$.result[%d].username".formatted(i)).value(user.getUsername()));
        }
    }

    @Test
    @DisplayName("단일 사용자 조회 - ADMIN 권한")
    void getUserById_withAdmin() throws Exception {
        Long id = 1L;
        User user = userService.findById(id).orElseThrow();

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users/{id}", id)
                        .cookie(apiKeyCookie, accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(user.getId()))
                .andExpect(jsonPath("$.result.username").value(user.getUsername()));
    }

    @Test
    @DisplayName("username으로 검색 - ADMIN 권한")
    void searchUserByUsername_withAdmin() throws Exception {
        String keyword = "user";
        List<User> users = userService.searchByUsername(keyword);

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users/search")
                        .param("keyword", keyword)
                        .cookie(apiKeyCookie, accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(users.size()));

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            resultActions
                    .andExpect(jsonPath("$.result[%d].id".formatted(i)).value(user.getId()))
                    .andExpect(jsonPath("$.result[%d].username".formatted(i)).value(user.getUsername()));
        }
    }

    @Test
    @DisplayName("userLoginId로 사용자 삭제 - ADMIN 권한")
    void deleteUserByLoginId_withAdmin() throws Exception {
        String loginId = "deleteTestUser";
        userService.register(new UserRegisterDto(
                loginId,
                "삭제테스트",
                "test1234",
                "test1234"
        ));

        ResultActions resultActions = mvc.perform(delete("/api/v1/adm/users/loginId/{userLoginId}", loginId)
                        .cookie(apiKeyCookie, accessTokenCookie))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("사용자가 성공적으로 삭제되었습니다."));

        Assertions.assertTrue(userService.findByUserLoginId(loginId).isEmpty());
    }
}