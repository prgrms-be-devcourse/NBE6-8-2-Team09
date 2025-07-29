// 거래 로그 관련 타입 정의

export interface TradeLogDto {
    id: number;
    walletId: number;
    date: string;
    coinId: number;
    tradeType: 'BUY' | 'SELL';
    quantity: bigint;
    price: bigint;

}

export interface TradeGetItems {
    type: string;
    coinId: number;
    siteId: number;
    startDate: string;
    endDate: string;

}