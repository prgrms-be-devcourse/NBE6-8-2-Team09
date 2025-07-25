package com.back.back9.domain.wallet.repository;

import com.back.back9.domain.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUserIdAndCoinId(int userId, int coinId);

    List<Wallet> findByUserId(int userId);

}
