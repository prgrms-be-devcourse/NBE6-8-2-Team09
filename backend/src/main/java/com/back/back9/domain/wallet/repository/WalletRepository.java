package com.back.back9.domain.wallet.repository;

import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUserId(Long userId);
    Optional<Wallet> findByUser(User user);
    Optional<Wallet> findByUserId(Long userId);  // Long 타입으로 변경
}
