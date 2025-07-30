package com.back.back9.domain.log.walletLog.dto;

import com.back.back9.domain.log.walletLog.entity.TransactionType;
import com.back.back9.domain.log.walletLog.entity.WalletLog;

import java.math.BigDecimal;

public record WalletLogDto(
        int id,
        int usrId,
        int walletId,
        TransactionType transactionType,
        BigDecimal price
) {
    public WalletLogDto(WalletLog walletLog){
        this(
                Math.toIntExact(walletLog.getId()),
                Math.toIntExact(walletLog.getId()),
                Math.toIntExact(walletLog.getId()),
                walletLog.getTransactionType(),
                walletLog.getPrice()
        );
    }

    public static WalletLogDto from(WalletLog walletLog) {
        return new WalletLogDto(
                Math.toIntExact(walletLog.getId()),
                Math.toIntExact(walletLog.getId()),
                Math.toIntExact(walletLog.getId()),
                walletLog.getTransactionType(),
                walletLog.getPrice()
        );
    }
}
