package com.back.back9.domain.orders.repository;

import com.back.back9.domain.orders.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByWalletId(Long walletId);
}
