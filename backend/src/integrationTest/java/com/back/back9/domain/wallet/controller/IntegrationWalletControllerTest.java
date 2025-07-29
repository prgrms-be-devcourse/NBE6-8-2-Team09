package com.back.back9.domain.wallet.controller;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("WalletController 통합 테스트")
@Transactional
@Tag("wallet")
public class IntegrationWalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 추가
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .userLoginId("testuser")
                .username("testuser")
                .password("password")
                .role(User.UserRole.ADMIN)
                .build();
        // 사용자 데이터 저장
        userRepository.save(user);

    }

    @Nested
    @DisplayName("사용자 지갑 조회 통합 테스트")
    class GetUserWalletTest {

        @Test
        @DisplayName("성공: 유효한 사용자 ID로 지갑 조회")
        void t1() throws Exception {
            User user = userRepository.findAll().get(0);
            // 데이터 저장
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.50"))
                    .build();
            walletRepository.save(wallet);

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}", user.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.address").value("0x123456789"))
                    .andExpect(jsonPath("$.balance").value(1000.50))
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isEmpty());
        }

        @Test
        @DisplayName("성공: 잔액이 0인 경우")
        void t2() throws Exception {
            User user = userRepository.findAll().get(0);
            // 데이터 저장(0원)
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x987654321")
                    .balance(BigDecimal.ZERO)
                    .build();
            walletRepository.save(wallet);

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}", user.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0.0))
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isEmpty());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID")
        void t3() throws Exception {
            // userId가 존재하지 않는 경우
            Long userId = 999L;

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}", userId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패: 잘못된 경로 파라미터 형식")
        void t4() throws Exception {
            // 잘못된 사용자 ID
            mockMvc.perform(get("/api/wallets/users/invalid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("지갑 잔액 충전 통합 테스트")
    class ChargeWalletTest {

        @Test
        @DisplayName("성공: 유효한 충전 요청")
        void t5() throws Exception {
            User user = userRepository.findAll().get(0);
            // DB에 지갑 데이터 저장
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            BigDecimal chargeAmount = new BigDecimal("500.00");
            ChargePointsRequest request = new ChargePointsRequest(chargeAmount);

            // 검증 - 충전은 정상 동작하고, coinAmounts는 빈 배열이어야 함 (유효한 CoinAmount가 없으므로)
            mockMvc.perform(post("/api/wallets/users/{userId}/charge", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.address").value("0x123456789"))
                    .andExpect(jsonPath("$.balance").value(1500.00)) // 1000 + 500
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isEmpty()); // 유효한 CoinAmount가 없으므로 빈 배열
        }

        @Test
        @DisplayName("실패: 음수 충전 금액")
        void t6() throws Exception {
            User user = userRepository.findAll().get(0);
            // DB에 지갑 데이터 저장
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            ChargePointsRequest request = new ChargePointsRequest(new BigDecimal("-100.00"));

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/charge", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("성공: 여러 번 충전하여 누적 확인")
        void t7() throws Exception {
            User user = userRepository.findAll().get(0);
            // DB에 지갑 데이터 저장
            BigDecimal initialBalance = new BigDecimal("1000.00");
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(initialBalance)
                    .build();
            walletRepository.save(wallet);

            // 첫 번째 충전
            ChargePointsRequest request1 = new ChargePointsRequest(new BigDecimal("500.00"));
            mockMvc.perform(post("/api/wallets/users/{userId}/charge", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1500.00)); // 1000 + 500

            // 두 번째 충전
            ChargePointsRequest request2 = new ChargePointsRequest(new BigDecimal("300.00"));
            mockMvc.perform(post("/api/wallets/users/{userId}/charge", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1800.00)) // 1500 + 300
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isEmpty()); // 유효한 CoinAmount가 없으므로 빈 배열
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID")
        void t8() throws Exception {
            Long userId = 999L;
            ChargePointsRequest request = new ChargePointsRequest(new BigDecimal("100.00"));

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/charge", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
