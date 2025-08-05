package com.back.back9.domain.order.dto;

public record OrderResponse(
    String coinName,
    String orderMethod,
    String orderStatus,
    Double price,
    Double quantity,
    Double filledQuantity,
    Double remainingQuantity,
    String createdAt
) {
}
