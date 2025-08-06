import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'

export const coinApi = {
    // 코인 목록 조회 (관리자용)
    getCoins: () =>
        apiCall<ApiResponse<any>>('/v1/adm/coins', {
            method: 'GET',
        }),

    // 코인 등록 (관리자용)
    createCoin: (coinData: { koreanName: string; englishName: string; symbol: string; }) =>
        apiCall<ApiResponse<any>>('/v1/adm/coins', {
            method: 'POST',
            body: JSON.stringify(coinData),
        }),

    // 코인 삭제 (관리자용)
    deleteCoin: (id: number) =>
        apiCall<ApiResponse<void>>(`/v1/adm/coins/${id}`, {
            method: 'DELETE',
        }),
}