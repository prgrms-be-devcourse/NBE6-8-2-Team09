package com.back.back9.domain.tradeLog.service;

import com.back.back9.domain.tradeLog.dto.ProfitAnalysisDto;
import com.back.back9.domain.tradeLog.dto.ProfitRateResponse;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.repository.TradeLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class AnalyticsService {
    private final TradeLogRepository tradeLogRepository;
    public AnalyticsService(TradeLogRepository tradeLogRepository) {
        this.tradeLogRepository = tradeLogRepository;
    }
    /*가상화폐 별 실현 수익률 계산, 실제로 자산을 매도해서 확정된 수익률
    * 1. 지갑 ID에 해당하는 모든 거래 로그 조회 (매수/매도 포함)
    * 2. 거래 로그를 코인 ID별로 그룹화
    * 3. 코인별 수익 분석 결과를 담을 리스트 생성
    * 4. 코인별로 수익률 계산
    * 4-1. 각 거래 내역을 순회하며 매수/매도별로 수량 및 금액 누적
    * 4-2.평균 매수가 = 총 매수 금액 / 총 매수 수량
    *    // (매수 내역이 없으면 0으로 처리)
    * 4-3. 실현 원가 = 평균 매수가 * 매도 수량
    * 4-4. 실현 수익률 = (총 매도 금액 - 실현 원가) / 실현 원가 * 100
    * 4-5. 현재 보유 수량 = 매수 수량 - 매도 수량
    * 4-6. 분석 결과 객체에 저장
    * */
    public ProfitRateResponse calculateRealizedProfitRates(int walletId) {
        List<TradeLog> tradeLogs = tradeLogRepository.findByWalletId(walletId);

        Map<Integer, List<TradeLog>> tradeLogsByCoin = tradeLogs.stream()
                .collect(Collectors.groupingBy(TradeLog::getCoinId));

        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        for (Map.Entry<Integer, List<TradeLog>> entry : tradeLogsByCoin.entrySet()) {
            int coinId = entry.getKey();
            List<TradeLog> logs = entry.getValue();

            BigDecimal totalBuyQuantity = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;

            BigDecimal totalSellQuantity = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;

            for (TradeLog log : logs) {
                BigDecimal tradeAmount = log.getPrice().multiply(log.getQuantity());
                if (log.getType() == TradeType.BUY) {
                    totalBuyQuantity = totalBuyQuantity.add(log.getQuantity());
                    totalBuyAmount = totalBuyAmount.add(tradeAmount);
                } else {
                    totalSellQuantity = totalSellQuantity.add(log.getQuantity());
                    totalSellAmount = totalSellAmount.add(tradeAmount);
                }
            }

            BigDecimal averageBuyPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalBuyAmount.divide(totalBuyQuantity, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal realizedCost = averageBuyPrice.multiply(totalSellQuantity);

            BigDecimal realizedProfitRate = realizedCost.compareTo(BigDecimal.ZERO) > 0
                    ? totalSellAmount.subtract(realizedCost)
                    .divide(realizedCost, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            BigDecimal currentHolding = totalBuyQuantity.subtract(totalSellQuantity);

            coinAnalytics.add(new ProfitAnalysisDto(
                    coinId,
                    currentHolding,
                    averageBuyPrice,
                    realizedProfitRate
            ));
        }

        return new ProfitRateResponse(walletId, coinAnalytics);
    }

    /*가상화폐 별 평가 수익률 계산(실시간 기반 or UTC 기반), 보유 자산의 시세 기준 잠재 수익률
    * 1. 현재 user가 보유하고 있는 Coin 목록 및 보유량, 평균단가 조회
    * 2. 각 코인에 대해 현재가 
    * 3. 현재가를 기준으로 예상 수익률 계산
    * 3-1. 현재가 * 보유량 = 현재 평가 금액
    * 3-2. 평균 단가 * 보유량 = 현재 평가 원가
    * 3-3. 예상 수익률 = (현재 평가 금액 - 현재 평가 원가) / 현재 평가 원가 * 100
    * 3-4. 현재 보유량 = 보유량
    * 3-5. 분석 결과 객체에 저장
    */
    public ProfitRateResponse calculateUnRealizedProfitRates(int walletId) {
        List<TradeLog> tradeLogs = tradeLogRepository.findByWalletId(walletId);

        Map<Integer, List<TradeLog>> tradeLogsByCoin = tradeLogs.stream()
                .collect(Collectors.groupingBy(TradeLog::getCoinId));

        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        for (Map.Entry<Integer, List<TradeLog>> entry : tradeLogsByCoin.entrySet()) {
            int coinId = entry.getKey();
            List<TradeLog> logs = entry.getValue();

            BigDecimal totalBuyQuantity = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;

            BigDecimal totalSellQuantity = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;

            for (TradeLog log : logs) {
                BigDecimal tradeAmount = log.getPrice().multiply(log.getQuantity());
                if (log.getType() == TradeType.BUY) {
                    totalBuyQuantity = totalBuyQuantity.add(log.getQuantity());
                    totalBuyAmount = totalBuyAmount.add(tradeAmount);
                } else {
                    totalSellQuantity = totalSellQuantity.add(log.getQuantity());
                    totalSellAmount = totalSellAmount.add(tradeAmount);
                }
            }

            BigDecimal averageBuyPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalBuyAmount.divide(totalBuyQuantity, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal realizedCost = averageBuyPrice.multiply(totalSellQuantity);

            BigDecimal realizedProfitRate = realizedCost.compareTo(BigDecimal.ZERO) > 0
                    ? totalSellAmount.subtract(realizedCost)
                    .divide(realizedCost, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            BigDecimal currentHolding = totalBuyQuantity.subtract(totalSellQuantity);

            coinAnalytics.add(new ProfitAnalysisDto(
                    coinId,
                    currentHolding,
                    averageBuyPrice,
                    realizedProfitRate
            ));
        }

        return new ProfitRateResponse(walletId, coinAnalytics);
    }
}
