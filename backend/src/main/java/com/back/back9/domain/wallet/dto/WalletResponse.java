package com.back.back9.domain.wallet.dto;

import com.back.back9.domain.wallet.entity.CoinAmount;
import com.back.back9.domain.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

// 새로운 지갑 응답 DTO - 사용자의 지갑과 모든 코인 수량 정보를 포함
public record WalletResponse(
        Long walletId,    // Long 타입으로 변경
        Long userId,      // Long 타입으로 변경
        String address,
        BigDecimal balance,
        List<CoinAmountResponse> coinAmounts
) {
    public static WalletResponse from(Wallet wallet) {
        List<CoinAmountResponse> coinAmountResponses = wallet.getCoinAmounts().stream()
                .map(CoinAmountResponse::from)
                .toList();

        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),  // user.getId()로 변경
                wallet.getAddress(),
                wallet.getBalance(),
                coinAmountResponses
        );
    }

    // 검증된 CoinAmount 리스트를 받아서 처리하는 새로운 팩토리 메서드
    public static WalletResponse fromWithValidCoinAmounts(Wallet wallet, List<CoinAmount> validCoinAmounts) {
        List<CoinAmountResponse> coinAmountResponses = validCoinAmounts.stream()
                .map(CoinAmountResponse::from)
                .toList();

        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),  // user.getId()로 변경
                wallet.getAddress(),
                wallet.getBalance(),
                coinAmountResponses
        );
    }
}
