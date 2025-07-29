import { apiCall } from './client';
import type { WalletDto, ChargeDto } from '@/lib/types/wallet';

// 지갑 API 호출들
export const walletApi = {
    // 코인 잔액 조회
    getBalance: (userId: number, coinId: number) =>
        apiCall<WalletDto>(`/wallet/users/${userId}/coins/${coinId}`),

    // 충전
    charge: (userId: number, coinId: number, amount: number) => {
        const chargeDto: ChargeDto = { amount };
        return apiCall(`/wallet/users/${userId}/coins/${coinId}/charge`, {
            method: 'POST',
            body: JSON.stringify(chargeDto),
        });
    },
};
