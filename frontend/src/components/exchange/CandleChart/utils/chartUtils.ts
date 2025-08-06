import { ExchangeDTO } from "@/lib/types/exchange/type";

// 가격 포맷 함수
export const formatPrice = (price: number): string => {
    return price.toLocaleString(undefined, {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
    });
};

// 시간 포맷 함수
export const formatTime = (date: Date): string => {
    return date.toLocaleString("ko-KR", {
        hour12: false,
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
    });
};

// 이동평균선 계산
export const calculateMA = (candles: ExchangeDTO[], period: number): (number | null)[] => {
    const result: (number | null)[] = [];
    for (let i = 0; i < candles.length; i++) {
        if (i < period - 1) {
            result.push(null);
            continue;
        }
        const sum = candles
            .slice(i - period + 1, i + 1)
            .reduce((acc, cur) => acc + cur.trade_price, 0);
        result.push(sum / period);
    }
    return result;
};