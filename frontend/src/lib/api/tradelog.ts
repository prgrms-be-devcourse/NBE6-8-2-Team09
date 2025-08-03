import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'
import type { TradeLogResponse, TradeGetItems } from '@/lib/types/tradelog'

export const tradeLogApi = {
  // 사용자의 거래 내역 조회
  getUserTradeLogs: (userId: number) =>
    apiCall<TradeLogResponse[]>(`/tradeLog/wallet/${userId}`),

  // 특정 코인의 거래 내역 조회
  getCoinTradeLogs: (userId: number, coinId: number) =>
    apiCall<ApiResponse<TradeLogResponse[]>>(`/tradeLog/wallet/${userId}/coins/${coinId}`),

  // 특정 거래 내역 상세 조회
  getTradeLogById: (id: number) =>
    apiCall<ApiResponse<TradeLogResponse>>(`/tradeLog/${id}`),

  getFilteredTradeLogs: async (
      userId: number,
      filters: Record<string, any>
  ): Promise<TradeLogResponse[]> => {
    const query = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        query.append(key, String(value));
      }
    });

    return (
        await apiCall<TradeLogResponse[]>(
            `/tradeLog/wallet/${userId}?${query.toString()}`
        )
    ) ?? []; // null이면 빈 배열 반환
  },
}
