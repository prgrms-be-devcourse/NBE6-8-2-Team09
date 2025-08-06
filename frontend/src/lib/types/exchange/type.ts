export interface ExchangeDTO {
    /**
     * UNIX 타임스탬프 (문자열로 들어옴)
     * 예: "1754417836169"
     */
    timestamp: string;

    /**
     * KST 기준 캔들 시간 (ISO 8601 문자열)
     * 예: "2025-08-06T03:17:00"
     */
    candle_date_time_kst: string;

    /**
     * 마켓 심볼
     * 예: "KRW-BTC"
     */
    market: string;

    /**
     * 시가 (number로 들어옴)
     * 예: 159029000
     */
    opening_price: number;

    /**
     * 고가
     */
    high_price: number;

    /**
     * 저가
     */
    low_price: number;

    /**
     * 종가 또는 거래 가격
     */
    trade_price: number;

    /**
     * 누적 거래량
     */
    candle_acc_trade_volume: number;

    /**
     * 코인 이름
     * 예: "비트코인"
     */
    name: string;
}


export enum CandleInterval {
    SEC = "SEC",
    MIN_1 = "MIN_1",
    MIN_30 = "MIN_30",
    HOUR_1 = "HOUR_1",
    DAY = "DAY",
    WEEK = "WEEK",
    MONTH = "MONTH",
    YEAR = "YEAR",
}



export interface InitialRequestDTO {
    /**
     * 캔들 간격
     */
    interval: CandleInterval;

    /**
     * 마켓 정보
     */
    market: string;
}

export interface PreviousDTO {
    /**
     * 캔들 간격
     */
    interval: CandleInterval;

    /**
     * 마켓 정보
     */
    market: string;

    /**
     * Initial요청으로 받아온 170개이후로 50개씩 페이징 했을때의 요청할 데이터들의 페이지 번호
     */
    page: number;

    /**
     * 시간 정보 (ISO 8601 문자열 형식)
     */
    time: string; // `LocalDateTime`에 대응하여 문자열로 사용
}