package com.back.back9.domain.coin.repository;

import com.back.back9.domain.coin.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CoinRepository extends JpaRepository<Coin, Integer> {

    Optional<Coin> findFirstByOrderByIdDesc();
}
