package com.back.back9.domain.log.tradeLog.service;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.wallet.service.WalletService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class TradeLogServiceTest {
    @Autowired
    private TradeLogService tradeLogService;

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CoinRepository coinRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mock;

    private Wallet wallet;
    private Coin coin;
    private User user;
    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .userLoginId("user1")
                .username("테스트유저")
                .password("test1234")
                .role(User.UserRole.MEMBER)
                .build());

        wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .address("TestAddress")
                .balance(BigDecimal.valueOf(1_000_000L))
                .coinAmounts(new ArrayList<>())
                .build());

        coin = coinRepository.save(Coin.builder()
                .symbol("KRW-BTC")
                .koreanName("비트코인")
                .englishName("Bitcoin")
                .build());
    }
    @Test
    @DisplayName("거래 내역 생성")
    public void createTradeLog() {
        TradeLog tradeLog = new TradeLog();
        tradeLog.setWallet(wallet);
        tradeLog.setCoin(coin);
        tradeLog.setType(TradeType.BUY);
        tradeLog.setQuantity(new BigDecimal("0.5"));
        tradeLog.setPrice(new BigDecimal("43000"));
        // when
        TradeLog saved = tradeLogService.save(tradeLog);

        // then
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(wallet.getId(), saved.getWallet().getId());
    }

}
