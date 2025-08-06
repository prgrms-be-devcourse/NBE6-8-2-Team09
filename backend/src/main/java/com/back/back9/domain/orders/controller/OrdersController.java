package com.back.back9.domain.orders.controller;

import com.back.back9.domain.orders.dto.OrdersRequest;
import com.back.back9.domain.orders.dto.OrderResponse;
import com.back.back9.domain.orders.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrdersController {
    private final OrdersService ordersService;

    @GetMapping("/wallet/{wallet_id}")
    public ResponseEntity<List<OrderResponse>> getOrders(@PathVariable Long wallet_id) {
        return ResponseEntity.ok(ordersService.getOrdersByWalletId(wallet_id));
    }

    @PostMapping("/wallet/{wallet_id}")
    public ResponseEntity<OrderResponse> executeTrade(
            @PathVariable("wallet_id") Long walletId,
            @RequestBody OrdersRequest ordersRequest) {

        OrderResponse response = ordersService.executeTrade(walletId, ordersRequest);
        return ResponseEntity.ok(response);
    }
}
