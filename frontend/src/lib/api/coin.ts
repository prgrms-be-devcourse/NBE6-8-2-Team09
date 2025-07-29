import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'
import type { CoinDto, CoinAddRequest } from '@/lib/types/coin'

export const coinApi = {
  // 모든 코인 조회
  getAll: () =>
    apiCall<ApiResponse<CoinDto[]>>('/coins'),

  // 특정 코인 조회
  getById: (id: number) =>
    apiCall<ApiResponse<CoinDto>>(`/coins/${id}`),

  // 새 코인 추가 (관리자)
  create: (coinData: CoinAddRequest) =>
    apiCall<ApiResponse<CoinDto>>('/coins', {
      method: 'POST',
      body: JSON.stringify(coinData),
    }),

  // 코인 수정 (관리자)
  update: (id: number, coinData: Partial<CoinAddRequest>) =>
    apiCall<ApiResponse<CoinDto>>(`/coins/${id}`, {
      method: 'PUT',
      body: JSON.stringify(coinData),
    }),

  // 코인 삭제 (관리자)
  delete: (id: number) =>
    apiCall<ApiResponse<void>>(`/coins/${id}`, {
      method: 'DELETE',
    }),
}
