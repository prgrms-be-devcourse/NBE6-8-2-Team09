package com.back.back9.domain.exchange.repository;

import com.back.back9.domain.exchange.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
}
