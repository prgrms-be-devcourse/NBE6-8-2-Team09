//주문 dto
export type TradeType = 'BUY' | 'SELL';

export type OrdersMethod = 'LIMIT' | 'MARKET';

export interface OrdersRequest {
    coinSymbol: string;       // 코인 심볼 (예: "BTC", "ETH")
    coinName: string;         // 코인 이름 (예: "Bitcoin", "Ethereum")
    tradeType: TradeType;     // 매수 또는 매도
    ordersMethod: OrdersMethod; // 주문 방식: 지정가(LIMIT) 또는 시장가(MARKET)
    quantity: string;         // 수량 (BigDecimal은 문자열로 처리)
    price: string;            // 단가 (BigDecimal은 문자열로 처리)
}

export interface OrderResponse {
    coinId: number;          // 코인 ID
    coinSymbol: string;      // 코인 심볼 (예: "BTC")
    coinName: string;        // 코인 이름 (예: "비트코인")
    orderMethod: 'LIMIT' | 'MARKET';  // 주문 방식
    orderStatus: 'WAIT' | 'COMPLETE' | 'CANCEL' | string; // 주문 상태 (백엔드 enum에 맞게 수정)
    tradeType: 'BUY' | 'SELL';        // 거래 타입
    price: string;           // 단가 (BigDecimal → string)
    quantity: string;        // 수량 (BigDecimal → string)
    createdAt: string;       // ISO-8601 날짜 문자열
}