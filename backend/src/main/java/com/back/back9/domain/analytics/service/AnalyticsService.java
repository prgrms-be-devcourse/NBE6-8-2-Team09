package com.back.back9.domain.analytics.service;

import com.back.back9.domain.analytics.dto.ProfitAnalysisDto;
import com.back.back9.domain.analytics.dto.ProfitRateResponse;
import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.domain.exchange.service.ExchangeService;
import com.back.back9.domain.log.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.service.TradeLogService;
import com.back.back9.domain.log.walletLog.dto.WalletLogDto;
import com.back.back9.domain.log.walletLog.entity.TransactionType;
import com.back.back9.domain.log.walletLog.service.WalletLogService;
import com.back.back9.domain.wallet.dto.CoinHoldingInfo;
import com.back.back9.domain.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class AnalyticsService {
    private final TradeLogService tradeLogService;
    private final WalletService walletService;
    private final WalletLogService walletLogService;
    private final ExchangeService exchangeService;
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    public AnalyticsService(TradeLogService tradeLogService,
                            WalletService walletService,
                            WalletLogService walletLogService,
                            ExchangeService exchangeService) {
        this.tradeLogService = tradeLogService;
        this.walletService = walletService;
        this.walletLogService = walletLogService;
        this.exchangeService = exchangeService;
    }

    /*
     * 가상화폐 별 실현 수익률 계산, 실제로 자산을 매도해서 확정된 수익률
     * 1. 지갑 ID에 해당하는 모든 거래 로그 조회 (매수/매도 포함)
     * 2. 거래 로그를 코인 ID별로 그룹화
     * 3. 코인별 수익 분석 결과를 담을 리스트 생성
     * 4. 총 투자금 = walletId에 해당하는 충전 합계 - 출금 합계
     * 5. 코인별로 수익률 계산
     *   5-1. 각 거래 내역을 순회하며 매수/매도별 수량 및 금액 누적
     *   5-2. 평균 매수가 = 총 매수 금액 / 총 매수 수량
     *         // (매수 내역이 없으면 0으로 처리)
     *   5-3. 실현 원가 = 평균 매수가 * 매도 수량
     *   5-4. 실현 수익률 = (총 매도 금액 - 실현 원가) / 총 투자금 * 100
     *   5-5. 분석 결과 객체에 저장
     */
    public ProfitRateResponse calculateRealizedProfitRates(int walletId) {

        List<TradeLogDto> tradeLogs = tradeLogService.findByWalletId(walletId);
        Map<Integer, List<TradeLogDto>> tradeLogsByCoin = tradeLogs.stream()
                .collect(Collectors.groupingBy(TradeLogDto::coinId));
        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        List<WalletLogDto> walletLogs = walletLogService.findByWalletId(walletId);
        BigDecimal baseInvestment = new BigDecimal("500000000"); // 5억
        BigDecimal walletLogSum = walletLogs.stream()
                .map(log -> log.transactionType() == TransactionType.CHARGE
                        ? log.price()
                        : log.price().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvested = baseInvestment.add(walletLogSum);
        BigDecimal totalSellAmountSum = BigDecimal.ZERO;
        BigDecimal totalRealizedCostSum = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<TradeLogDto>> entry : tradeLogsByCoin.entrySet()) {
            int coinId = entry.getKey();
            List<TradeLogDto> logs = entry.getValue();

            BigDecimal totalBuyQuantity = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;
            BigDecimal totalSellQuantity = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;

            for (TradeLogDto log : logs) {
                BigDecimal tradeAmount = log.price().multiply(log.quantity());
                if (log.tradeType() == TradeType.BUY) {
                    totalBuyQuantity = totalBuyQuantity.add(log.quantity());
                    totalBuyAmount = totalBuyAmount.add(tradeAmount);
                } else {
                    totalSellQuantity = totalSellQuantity.add(log.quantity());
                    totalSellAmount = totalSellAmount.add(tradeAmount);
                }
            }

            BigDecimal averageBuyPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalBuyAmount.divide(totalBuyQuantity, 8, RoundingMode.DOWN)
                    : BigDecimal.ZERO;
            BigDecimal realizedCost = averageBuyPrice.multiply(totalSellQuantity);
            BigDecimal realizedProfitRate = totalBuyAmount.compareTo(BigDecimal.ZERO) > 0
                    ? totalSellAmount.subtract(realizedCost)
                    .divide(totalBuyAmount, 8, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            totalSellAmountSum = totalSellAmountSum.add(totalSellAmount);
            totalRealizedCostSum = totalRealizedCostSum.add(realizedCost);

            BigDecimal currentHolding = totalBuyQuantity.subtract(totalSellQuantity);

            coinAnalytics.add(new ProfitAnalysisDto(
                    coinId,
                    currentHolding,
                    averageBuyPrice,
                    realizedProfitRate
            ));
        }

        BigDecimal totalRealizedProfitRate = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalSellAmountSum.subtract(totalRealizedCostSum)
                .divide(totalInvested, 8, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new ProfitRateResponse(walletId, coinAnalytics, totalRealizedProfitRate);
    }
    /*
     * 지갑 내 가상화폐별 미실현 평가 수익률 계산 (실시간 시세 기반)
     *
     * 1. 현재 wallet에 보유 중인 코인 목록 및 평균 매입 단가, 보유 수량 조회
     * 2. 각 코인에 대해 실시간 현재가 조회
     * 3. 수익률 계산
     *   3-1. 평가금액 = 현재가 * 보유 수량
     *   3-2. 투자원금 = 평균 매입가 * 보유 수량
     *   3-3. 수익률 = (평가금액 - 투자원금) / 투자원금 * 100
     * 4. 코인 ID, 보유 수량, 평균 매입가, 수익률을 분석 결과 객체에 저장
     * 5. 전체 자산 기준 수익률 계산을 위해 총 투자금액, 총 평가금액 누적
     */
    public ProfitRateResponse calculateUnRealizedProfitRates(int walletId) {
        List<CoinHoldingInfo> coinHoldingInfos = walletService.getCoinHoldingsByUserId((long) walletId);
        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalEvaluation = BigDecimal.ZERO;

        for (CoinHoldingInfo info : coinHoldingInfos) {
            CoinPriceResponse coinPriceResponse = exchangeService.getLatestCandleByScan(info.coinName());

            BigDecimal quantity = info.quantity();
            BigDecimal avgBuyPrice = info.averageBuyPrice();
            BigDecimal currentPrice = new BigDecimal(coinPriceResponse.getPrice()); // getter 이름 확인 필요
            log.info(
                    "코인: {}, 현재가: {}, 수량: {}, 평균단가: {}",
                    info.coinName(),
                    coinPriceResponse.getPrice(),
                    info.quantity(),
                    info.averageBuyPrice()
            );
            BigDecimal profitRate = currentPrice.subtract(avgBuyPrice)
                    .divide(avgBuyPrice, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            coinAnalytics.add(new ProfitAnalysisDto(
                    (int) info.coinId(),
                    quantity,
                    avgBuyPrice,
                    profitRate
            ));

            totalInvested = totalInvested.add(avgBuyPrice.multiply(quantity));
            totalEvaluation = totalEvaluation.add(currentPrice.multiply(quantity));
        }

        BigDecimal totalProfitRate = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitRate = totalEvaluation.subtract(totalInvested)
                    .divide(totalInvested, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new ProfitRateResponse(walletId, coinAnalytics, totalProfitRate);
    }
}
