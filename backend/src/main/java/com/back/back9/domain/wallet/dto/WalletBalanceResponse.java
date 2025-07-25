package com.back.back9.domain.wallet.dto;

import com.back.back9.domain.wallet.entity.Wallet;

import java.math.BigDecimal;
// 지갑 잔액 응답 DTO
public record  WalletBalanceResponse (
        int walletId,
        int userId,
        String address,
        BigDecimal balance
) {
    public WalletBalanceResponse(int walletId, int userId, String address, BigDecimal balance) {
        this.walletId = walletId;
        this.userId = userId;
        this.address = address;
        this.balance = balance;
    }

    // 정적 팩토리 메서드
    public WalletBalanceResponse of(int walletId, int userId, String address, BigDecimal balance) {
        return new WalletBalanceResponse(walletId, userId, address, balance);
    }
    // Wallet 엔티티를 WalletBalanceResponse로 변환하는 정적 팩토리 메서드
    public static WalletBalanceResponse from(Wallet wallet){
        return new WalletBalanceResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getAddress(),
                wallet.getBalance()
        );
    }

}
