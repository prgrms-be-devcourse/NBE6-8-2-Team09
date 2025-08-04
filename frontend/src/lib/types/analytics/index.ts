// 분석 관련 타입 정의

export interface ProfitAnalysisDto {
    coinId: number;
    totalQuantity: number;
    averageBuyPrice: number;
    realizedProfitRate: number;
}
export interface ProfitRateResponse {
    userId: number;
    coinAnalytics: ProfitAnalysisDto[];
    profitRateOnInvestment: number;
    profitRateOnTotalAssets: number;
}