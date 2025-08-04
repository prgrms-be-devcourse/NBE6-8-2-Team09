package com.back.back9.domain.wallet.controller;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.dto.BuyCoinRequest;
import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("wallet")
public class IntegrationWalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 추가

    @Autowired
    private CoinRepository coinRepository; // CoinRepository 추가

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 트랜잭션 내에서는 deleteAll 대신 @Sql 사용하거나
        // 각 테스트 메서드에서 독립적인 데이터 생성
        try {
            // 기존 데이터 정리 (트랜잭션이 롤백되므로 실제로는 영향 없음)
            walletRepository.deleteAll();
            coinRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // 무시하고 계속 진행
        }

        // 테스트용 사용자 생성
        User user = User.builder()
                .userLoginId("testuser")
                .username("testuser")
                .password("password")
                .role(User.UserRole.ADMIN)
                .build();
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

    @Nested
    @DisplayName("코인 구매 통합 테스트")
    class PurchaseItemIntegrationTest {

        @Test
        @DisplayName("성공: 유효한 구매 요청")
        void t9() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성 (충분한 잔액)
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(wallet);

            BuyCoinRequest request = new BuyCoinRequest(
                    coin.getId(),           // coinId
                    wallet.getId(),         // walletId
                    new BigDecimal("1000.00"),  // 구매 금액
                    new BigDecimal("0.5")       // 구매 수량
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.balance").value(9000.00)) // 10000 - 1000
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isNotEmpty()); // 구매 후 코인 보유
        }

        @Test
        @DisplayName("실패: 잔액 부족")
        void t10() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성 (부족한 잔액)
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("500.00"))
                    .build();
            walletRepository.save(wallet);

            BuyCoinRequest request = new BuyCoinRequest(
                    coin.getId(),           // coinId
                    wallet.getId(),         // walletId
                    new BigDecimal("1000.00"),  // 구매 금액 (잔액보다 큼)
                    new BigDecimal("0.5")
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 코인 ID")
        void t11() throws Exception {
            User user = userRepository.findAll().get(0);

            // 지갑 생성
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(wallet);

            BuyCoinRequest request = new BuyCoinRequest(
                    999L,                   // 존재하지 않는 coinId
                    wallet.getId(),
                    new BigDecimal("1000.00"),
                    new BigDecimal("0.5")
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 지갑 접근")
        void t12() throws Exception {
            User user = userRepository.findAll().get(0);

            // 다른 사용자 생성
            User otherUser = User.builder()
                    .userLoginId("otheruser")
                    .username("otheruser")
                    .password("password")
                    .role(User.UserRole.MEMBER)
                    .build();
            userRepository.save(otherUser);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 다른 사용자의 지갑 생성
            Wallet otherWallet = Wallet.builder()
                    .user(otherUser)
                    .address("0x987654321")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(otherWallet);

            BuyCoinRequest request = new BuyCoinRequest(
                    coin.getId(),
                    otherWallet.getId(),    // 다른 사용자의 지갑 ID
                    new BigDecimal("1000.00"),
                    new BigDecimal("0.5")
            );

            // 검증 - 권한이 없어야 함
            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized()); // UNAUTHORIZED_ACCESS 에러
        }
    }

    @Nested
    @DisplayName("코인 판매 통합 테스트")
    class SellItemIntegrationTest {

        @Test
        @DisplayName("성공: 유효한 판매 요청")
        void t13() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("5000.00"))
                    .build();
            walletRepository.save(wallet);

            // 먼저 코인을 구매해서 보유량을 만듦
            BuyCoinRequest buyRequest = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("2000.00"),
                    new BigDecimal("1.0")
            );

            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buyRequest)))
                    .andExpect(status().isOk());

            // 이제 판매 테스트
            BuyCoinRequest sellRequest = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("1000.00"),  // 판매 금액
                    new BigDecimal("0.5")       // 판매 수량
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/sell", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.balance").value(4000.00)) // 3000 + 1000 (구매 후 잔액 + 판매 수익)
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isNotEmpty()); // 아직 코인 보유
        }

        @Test
        @DisplayName("실패: 보유하지 않은 코인 판매")
        void t14() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성 (코인 보유 없음)
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(wallet);

            BuyCoinRequest request = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("1000.00"),
                    new BigDecimal("0.5")
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/sell", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패: 보유 수량보다 많은 판매")
        void t15() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(wallet);

            // 먼저 적은 양의 코인 구매
            BuyCoinRequest buyRequest = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("1000.00"),
                    new BigDecimal("0.5")  // 0.5개 구매
            );

            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buyRequest)))
                    .andExpect(status().isOk());

            // 보유량보다 많이 판매 시도
            BuyCoinRequest sellRequest = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("2000.00"),
                    new BigDecimal("1.0")  // 1.0개 판매 시도 (0.5개만 보유)
            );

            // 검증
            mockMvc.perform(post("/api/wallets/users/{userId}/sell", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("성공: 연속 거래 테스트 (구매→판매→구매)")
        void t16() throws Exception {
            User user = userRepository.findAll().get(0);

            // 테스트용 코인 생성
            Coin coin = Coin.builder()
                    .englishName("Bitcoin")
                    .symbol("BTC")
                    .build();
            coinRepository.save(coin);

            // 지갑 생성
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .address("0x123456789")
                    .balance(new BigDecimal("10000.00"))
                    .build();
            walletRepository.save(wallet);

            // 1. 첫 번째 구매
            BuyCoinRequest buyRequest1 = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("2000.00"),
                    new BigDecimal("1.0")
            );

            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buyRequest1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(8000.00)); // 10000 - 2000

            // 2. 판매
            BuyCoinRequest sellRequest = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("1500.00"),
                    new BigDecimal("0.7")
            );

            mockMvc.perform(post("/api/wallets/users/{userId}/sell", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(9500.00)); // 8000 + 1500

            // 3. 두 번째 구매
            BuyCoinRequest buyRequest2 = new BuyCoinRequest(
                    coin.getId(),
                    wallet.getId(),
                    new BigDecimal("1000.00"),
                    new BigDecimal("0.4")
            );

            mockMvc.perform(post("/api/wallets/users/{userId}/purchase", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buyRequest2)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(8500.00)) // 9500 - 1000
                    .andExpect(jsonPath("$.coinAmounts").isArray())
                    .andExpect(jsonPath("$.coinAmounts").isNotEmpty());
        }
    }
}
