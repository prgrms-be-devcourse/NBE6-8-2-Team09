package com.back.back9.domain.order.controller;

import com.back.back9.domain.order.dto.OrderResponse;
import com.back.back9.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {
    private final OrderService orderService;

//    @GetMapping("/users/{userId}")
//    public ResponseEntity<List<OrderResponse>> getOrders(@PathVariable Long userId) {
//        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
//    }
}
