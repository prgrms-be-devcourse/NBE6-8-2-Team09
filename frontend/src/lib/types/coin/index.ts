// 코인 관련 타입 정의
export interface CoinDto {
    id: number;
    symbol: string;
    koreanName: string;
    englishName: string;
}

export interface CoinAddRequest {
    symbol: string;
    koreanName: string;
    englishName: string;
}

export interface CoinApiResponse {
    coins: CoinDto[];
    totalCount: number;
}