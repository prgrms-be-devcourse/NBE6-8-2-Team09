// 거래 로그 관련 타입 정의

export interface TradeLogResponse {
    date: string;                    // "2025.08.03" 형식
    coinSymbol: string;             // 예: "BTC"
    tradeType: 'BUY' | 'SELL' | 'CHARGE';      // 거래 유형
    price: string;                  // 문자열 금액 (ex: "1000000")
    quantity: string;

}

export interface TradeGetItems {
    type: string;
    coinId: number;
    siteId: number;
    startDate: string;
    endDate: string;

}