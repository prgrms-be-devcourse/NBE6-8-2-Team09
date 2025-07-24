package com.back.back9.domain.user.controller;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdUserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("전체 사용자 조회 - ADMIN 권한")
    @WithUserDetails("admin") // admin 계정이 실제 DB에 있어야 함
    void getAllUsers_withAdmin() throws Exception {
        List<User> users = userService.findAll();

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(users.size()));

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(user.getId()))
                    .andExpect(jsonPath("$[%d].username".formatted(i)).value(user.getUsername()));
        }
    }

    @Test
    @DisplayName("단일 사용자 조회 - ADMIN 권한")
    @WithUserDetails("admin")
    void getUserById_withAdmin() throws Exception {
        Long id = 1L;
        User user = userService.findById(id).orElseThrow();

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users/{id}", id))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    @DisplayName("username으로 검색 - ADMIN 권한")
    @WithUserDetails("admin")
    void searchUserByUsername_withAdmin() throws Exception {
        String keyword = "user"; // 테스트에 맞게 유효한 키워드로 수정

        List<User> users = userService.searchByUsername(keyword);

        ResultActions resultActions = mvc.perform(get("/api/v1/adm/users/search")
                        .param("keyword", keyword))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(users.size()));

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(user.getId()))
                    .andExpect(jsonPath("$[%d].username".formatted(i)).value(user.getUsername()));
        }
    }
}
