package com.back.back9.domain.order.entity;

public enum OrderStatus {
    PENDING,  // 주문 대기 중
    FILLED,   // 주문 체결 완료
    CANCELLED, // 주문 취소됨, 사용자가 실제 취소
    FAILED, // 주문 실패, 시스템 오류 등으로 인해 처리되지 못함
    EXPIRED, // 주문 만료, 지정가 주문이 유효 기간 내에 체결되지 않음
    PARTIALLY_FILLED // 일부만 체결된 상태, 나머지 주문은 여전히 대기 중
}
