// API 관련 공통 타입 정의

export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
}

// 지갑 관련 타입들
export interface WalletDto {
    walletId: number;
    userId: number;
    address: string;
    balance: number;
}

export interface ChargeDto {
    amount: number;
}

