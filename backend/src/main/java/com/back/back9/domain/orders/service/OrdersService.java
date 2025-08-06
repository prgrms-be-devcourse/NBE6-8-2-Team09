package com.back.back9.domain.orders.service;


import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.orders.dto.OrdersRequest;
import com.back.back9.domain.orders.dto.OrderResponse;
import com.back.back9.domain.orders.entity.Orders;
import com.back.back9.domain.orders.entity.OrdersStatus;
import com.back.back9.domain.orders.repository.OrdersRepository;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.wallet.dto.BuyCoinRequest;
import com.back.back9.domain.wallet.dto.TransactionType;
import com.back.back9.domain.wallet.dto.WalletResponse;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.wallet.service.WalletService;
import com.back.back9.global.error.ErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final CoinRepository coinRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    @Transactional
    public List<OrderResponse> getOrdersByWalletId(Long walletId) {
        List<Orders> orders = ordersRepository.findByWalletId(walletId);

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse executeTrade(Long walletId, OrdersRequest ordersRequest) {
        log.info(String.valueOf(ordersRequest));
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));
        User user = wallet.getUser();

        Coin coin = coinRepository.findBySymbol(ordersRequest.coinSymbol())
                .orElseThrow(() -> new IllegalArgumentException("코인을 찾을 수 없습니다."));

        //1. BuyCoinRequest생성
        BuyCoinRequest buyCoinRequest = new BuyCoinRequest(
                coin.getId(),
                wallet.getId(),
                ordersRequest.price().multiply(ordersRequest.quantity()),
                ordersRequest.quantity()
        );
        //2. walletService.processTransaction 메서드 호출 및 결과 값 확인
        TransactionType transactionType = ordersRequest.tradeType() == TradeType.BUY
                ? TransactionType.BUY
                : TransactionType.SELL;
        //3. 성공 여부에 따라 Order 엔티티 저장

        try {
            ResponseEntity<WalletResponse> walletResponse = walletService.processTransaction(user.getId(), buyCoinRequest, transactionType);

            if (walletResponse.getStatusCode().is2xxSuccessful()) {
                log.info("거래 성공: {}", walletResponse.getBody());

                Orders savedOrder = Orders.builder()
                        .user(user)
                        .wallet(wallet)
                        .coin(coin)
                        .tradeType(ordersRequest.tradeType())
                        .ordersMethod(ordersRequest.ordersMethod())
                        .quantity(ordersRequest.quantity())
                        .price(ordersRequest.price())
                        .createdAt(LocalDateTime.now())
                        .ordersStatus(OrdersStatus.FILLED)
                        .build();

                ordersRepository.save(savedOrder);
                return OrderResponse.from(savedOrder);
            }

            // 성공 아니지만 예외도 안 난 경우
            log.error("거래 실패: {}", walletResponse.getStatusCode());

        } catch (ErrorException e) {
            // 실패한 이유를 로그에 남기고, FAILED 상태로 주문 저장
            log.error("거래 예외 발생 - 실패 사유: {}", e.getMessage());

            Orders failedOrder = Orders.builder()
                    .user(user)
                    .wallet(wallet)
                    .coin(coin)
                    .tradeType(ordersRequest.tradeType())
                    .ordersMethod(ordersRequest.ordersMethod())
                    .quantity(ordersRequest.quantity())
                    .price(ordersRequest.price())
                    .createdAt(LocalDateTime.now())
                    .ordersStatus(OrdersStatus.FAILED)
                    .build();

            ordersRepository.save(failedOrder);
            return OrderResponse.from(failedOrder);
        }

        throw new IllegalArgumentException("예상치 못한 거래 실패");

    }
}