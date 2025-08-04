import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'
import type { ProfitRateResponse, ProfitAnalysisDto } from '@/lib/types/analytics'

export const analyticsApi = {
    // 사용자의 거래 내역 조회
    getUserAnalyticsRealized: (userId: number) =>
        apiCall<ProfitRateResponse[]>(`/analytics/wallet/${userId}/realized`),
    getUserAnalyticsUnrealized: (userId: number) =>
        apiCall<ProfitRateResponse[]>(`/analytics/wallet/${userId}/unrealized`),


}
