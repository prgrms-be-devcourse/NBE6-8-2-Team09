package com.back.back9.domain.wallet.controller;

import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
    }

    @Nested
    @DisplayName("지갑 잔액 조회 테스트")
    class GetWalletBalanceTest {

        @Test
        @DisplayName("성공: 유효한 사용자 ID와 코인 ID로 잔액 조회")
        void t1() throws Exception {
            // 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.50"))
                    .build();
            walletRepository.save(wallet);

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.address").value("0x123456789"))
                    .andExpect(jsonPath("$.balance").value(1000.50));
        }

        @Test
        @DisplayName("성공: 잔액이 0인 경우")
        void t2() throws Exception {
            // 데이터 저장(0원)
            int userId = 2;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x987654321")
                    .balance(BigDecimal.ZERO)
                    .build();
            walletRepository.save(wallet);

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0.0));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID")
        void t3() throws Exception {
            // userId와 coinId가 존재하지 않는 경우
            int userId = 999;
            int coinId = 1;

            // 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패: 잘못된 경로 파라미터 형식")
        void t4() throws Exception {
            // 잘못된 사용자 ID와 코인 ID
            mockMvc.perform(get("/api/wallets/users/invalid/coins/invalid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("포인트 충전 테스트")
    class ChargePointsTest {

        @Test
        @DisplayName("성공: 유효한 충전 요청")
        void t5() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            BigDecimal chargeAmount = new BigDecimal("500.00");
            ChargePointsRequest request = new ChargePointsRequest(chargeAmount);

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.address").value("0x123456789"));
        }

        @Test
        @DisplayName("실패: 음수 충전 금액")
        void t6() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            ChargePointsRequest request = new ChargePointsRequest(new BigDecimal("-100.00"));

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: null 충전 금액")
        void t7() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            String requestBody = "{\"amount\": null}";

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 빈 요청 본문")
        void t8() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 잘못된 JSON 형식")
        void t9() throws Exception {
            // User ID와 Coin ID 설정
            int userId = 1;
            int coinId = 1;

            // 잘못된 요청 본문
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": \"invalid\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("성공: 소수점 충전 금액")
        void t10() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            BigDecimal chargeAmount = new BigDecimal("123.456");
            ChargePointsRequest request = new ChargePointsRequest(chargeAmount);

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1123.456));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 지갑에 충전 시도")
        void t11() throws Exception {
            // DB에 지갑 데이터가 없는 경우
            int userId = 999;
            int coinId = 1;
            ChargePointsRequest request = new ChargePointsRequest(new BigDecimal("100.00"));

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("시나리오: 지갑 생성 후 잔액 조회 및 포인트 충전")
        void t12() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("1000.00"))
                    .build();
            walletRepository.save(wallet);

            // 초기 잔액 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1000.00));

            // 포인트 충전
            ChargePointsRequest request = new ChargePointsRequest(new BigDecimal("500.00"));
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId));

            // 충전 후 잔액 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1500.00));
        }

        @Test
        @DisplayName("시나리오: 여러 번 충전 테스트")
        void t13() throws Exception {
            // DB에 지갑 데이터 저장
            int userId = 1;
            int coinId = 1;
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .address("0x123456789")
                    .balance(new BigDecimal("100.00"))
                    .build();
            walletRepository.save(wallet);

            // 첫 번째 충전
            ChargePointsRequest firstCharge = new ChargePointsRequest(new BigDecimal("200.00"));
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstCharge)))
                    .andExpect(status().isOk());

            // 두 번째 충전
            ChargePointsRequest secondCharge = new ChargePointsRequest(new BigDecimal("150.00"));
            mockMvc.perform(post("/api/wallets/users/{userId}/coins/{coinId}/charge", userId, coinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondCharge)))
                    .andExpect(status().isOk());

            // 최종 잔액 확인
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId));
            // 잔액 검증
            mockMvc.perform(get("/api/wallets/users/{userId}/coins/{coinId}", userId, coinId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(450.00)); // 100 + 200 + 150
        }
    }
}
