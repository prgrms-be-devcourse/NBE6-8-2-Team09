package com.back.back9.domain.wallet.repository;

import com.back.back9.domain.wallet.entity.CoinAmount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinAmountRepository extends JpaRepository<CoinAmount, Integer> {
}
