// lib/api/wallet.ts
import { apiCall } from "./client";
import type { WalletDto, ChargeDto, BuyCoinRequest } from "@/lib/types/wallet";

// 지갑 API 호출들
export const walletApi = {
    // 사용자 지갑 전체 조회
    getWallet: (userId: number) =>
        apiCall<WalletDto>(`/wallets/users/${userId}`),

    // 충전
    charge: (userId: number, amount: number) => {
        const chargeDto: ChargeDto = { amount };
        return apiCall(`/wallets/users/${userId}/charge`, {
            method: "PUT",
            body: JSON.stringify(chargeDto),
        });
    },

    // 코인 구매
    purchase: (userId: number, request: BuyCoinRequest) =>
        apiCall<WalletDto>(`/wallets/users/${userId}/purchase`, {
            method: "PUT",
            body: JSON.stringify(request),
        }),

    // 코인 판매
    sell: (userId: number, request: BuyCoinRequest) =>
        apiCall<WalletDto>(`/wallets/users/${userId}/sell`, {
            method: "PUT",
            body: JSON.stringify(request),
        }),
};
