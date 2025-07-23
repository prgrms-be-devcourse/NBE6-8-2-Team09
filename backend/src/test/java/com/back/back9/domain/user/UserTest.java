package com.back.back9.domain.user;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUserLoginId("testuser").isEmpty()) {
            User user = User.builder()
                    .userLoginId("loginid1")
                    .username("testuser")
                    .password(passwordEncoder.encode("1234"))
                    .role("MEMBER")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/user/register")
                        .param("userLoginId", "loginid2")
                        .param("username", "newuser")
                        .param("password", "abcd")
                        .param("confirmPassword", "abcd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void registerFail_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/user/register")
                        .param("userLoginId", "loginid3")
                        .param("username", "newuser2")
                        .param("password", "abcd")
                        .param("confirmPassword", "efgh"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        mockMvc.perform(formLogin("/user/login")
                        .user("userLoginId", "loginid1")
                        .password("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFail_WrongPassword() throws Exception {
        mockMvc.perform(formLogin("/user/login")
                        .user("userLoginId", "loginid1")
                        .password("password", "wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login?error"));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void loginFail_NoUser() throws Exception {
        mockMvc.perform(formLogin("/user/login")
                        .user("userLoginId", "nouser")
                        .password("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login?error"));
    }
}