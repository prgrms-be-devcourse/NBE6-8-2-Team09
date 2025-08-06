package com.back.back9.domain.orders.service;

import com.back.back9.domain.analytics.contoller.AnalyticsControllerTest;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.orders.dto.OrdersRequest;
import com.back.back9.domain.orders.dto.OrderResponse;
import com.back.back9.domain.orders.entity.OrdersMethod;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("order")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class OrdersServiceTest {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsControllerTest.class);

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CoinRepository coinRepository;

    private User user1;

    private Wallet wallet1;

    private Coin coin1;

    @BeforeEach
    void setUp(){
        coinRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        user1 = userRepository.save(User.builder()
                .userLoginId("user1")
                .username("유저1")
                .password("password")
                .role(User.UserRole.ADMIN)
                .build());
        wallet1 = walletRepository.save(Wallet.builder()
                .user(user1)
                .address("Korea")
                .balance(BigDecimal.valueOf(500_000_000L))
                .coinAmounts(new ArrayList<>())  // null 방지
                .build());
        coin1 = coinRepository.save(Coin.builder()
                .symbol("KRW-BTC4")
                .koreanName("비트코인4")
                .englishName("Bitcoin4")
                .build());

    }
    @DisplayName("OrderService - 매수 주문 성공")
    @Test
    void createOrder1() {
        OrdersRequest ordersRequest = new OrdersRequest(
                coin1.getSymbol(),
                coin1.getKoreanName(),
                TradeType.BUY,
                OrdersMethod.MARKET,
                BigDecimal.valueOf(0.1),
                BigDecimal.valueOf(10_000_000L)

        );
        // when
        OrderResponse orderResponse = ordersService.executeTrade(wallet1.getId(), ordersRequest);
        // then
        assertNotNull(orderResponse);
        assertEquals(coin1.getId(), orderResponse.coinId());
        assertEquals(BigDecimal.valueOf(0.1), orderResponse.quantity());
        assertEquals(BigDecimal.valueOf(10_000_000L), orderResponse.price());
        assertEquals("BUY", orderResponse.tradeType());
        log.info("매수 주문 성공: {}", orderResponse);
        //oinAmountRepository.findByWalletId 함수 구현이 안되어있어 주석 처리
//        CoinAmount coinAmount = coinAmountRepository.findByWalletId(wallet1.getId())
//                .orElseThrow(() -> new IllegalArgumentException("코인 금액 정보를 찾을 수 없습니다."));
//        log.info("코인 수 {}", coinAmount.getQuantity());
//        log.info("총 투자 금액 {}", coinAmount.getTotalAmount());


    }

    @DisplayName("OrderService - 매수 실패")
    @Test
    void createOrder2() {
        OrdersRequest ordersRequest = new OrdersRequest(
                coin1.getSymbol(),
                coin1.getKoreanName(),
                TradeType.BUY,
                OrdersMethod.MARKET,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(600_000_000L)

        );
        // when
        OrderResponse response = ordersService.executeTrade(wallet1.getId(), ordersRequest);

        // then
        assertNotNull(response);
        assertEquals("FAILED", response.orderStatus());
        assertEquals(coin1.getId(), response.coinId());
        assertEquals("KRW-BTC4", response.coinSymbol()); // 필요 시 수정
        assertEquals("비트코인4", response.coinName()); // 필요 시 수정

        System.out.println("실패 테스트 결과: " + response);

    }
}
