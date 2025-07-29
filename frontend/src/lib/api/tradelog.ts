import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'
import type { TradeLogDto, TradeGetItems } from '@/lib/types/tradelog'

export const tradeLogApi = {
  // 사용자의 거래 내역 조회
  getUserTradeLogs: (userId: number) =>
    apiCall<ApiResponse<TradeLogDto[]>>(`/tradelog/users/${userId}`),

  // 특정 코인의 거래 내역 조회
  getCoinTradeLogs: (userId: number, coinId: number) =>
    apiCall<ApiResponse<TradeLogDto[]>>(`/tradelog/users/${userId}/coins/${coinId}`),

  // 특정 거래 내역 상세 조회
  getTradeLogById: (id: number) =>
    apiCall<ApiResponse<TradeLogDto>>(`/tradelog/${id}`),

  // 거래 내역 필터링 조회
  getFilteredTradeLogs: (filters: TradeGetItems) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value) params.append(key, String(value));
    });
    return apiCall<ApiResponse<TradeLogDto[]>>(`/tradelog/filtered?${params.toString()}`);
  },
}
