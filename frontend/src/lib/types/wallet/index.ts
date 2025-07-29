// 지갑 관련 타입 정의
export interface WalletDto {
    walletId: number;
    userId: number;
    address: string;
    balance: number;
}

export interface ChargeDto {
    amount: number;
}
