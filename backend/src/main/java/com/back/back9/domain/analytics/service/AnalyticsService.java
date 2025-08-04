package com.back.back9.domain.analytics.service;

import com.back.back9.domain.analytics.dto.ProfitAnalysisDto;
import com.back.back9.domain.analytics.dto.ProfitRateResponse;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.service.CoinService;
import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.domain.exchange.service.ExchangeService;
import com.back.back9.domain.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import com.back.back9.domain.wallet.dto.CoinHoldingInfo;
import com.back.back9.domain.wallet.dto.WalletResponse;
import com.back.back9.domain.wallet.service.WalletService;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ExchangeService exchangeService;
    private final CoinService coinService;
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    public AnalyticsService(TradeLogService tradeLogService,
                            WalletService walletService,
                            ExchangeService exchangeService,
                            CoinService coinService) {
        this.tradeLogService = tradeLogService;
        this.walletService = walletService;
        this.exchangeService = exchangeService;
        this.coinService = coinService;
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
    @Transactional(readOnly = true)
    public ProfitRateResponse calculateRealizedProfitRates(int walletId) {
        // 지갑에 해당하는 모든 트레이드 로그 조회 (매수, 매도, 충전 포함)
        List<TradeLogDto> tradeLogs = tradeLogService.findByWalletId(walletId);

        // 코인별로 트레이드 로그 그룹핑
        Map<Integer, List<TradeLogDto>> tradeLogsByCoin = tradeLogs.stream()
                .collect(Collectors.groupingBy(TradeLogDto::coinId));

        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        // 충전(CHARGE) 로그만 추출하여 총 투자금 계산 (단순 가격 합산)
        List<TradeLogDto> walletLogs = tradeLogService.findByWalletIdAndTypeCharge(walletId);

        BigDecimal baseInvestment = new BigDecimal(500_000_000L); // 초기 투자금 (예: 5억 원)
        BigDecimal walletLogSum = walletLogs.stream()
                .map(TradeLogDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInvested = baseInvestment.add(walletLogSum);

        // 전체 매도금액 및 전체 실현 원가 누적용
        BigDecimal totalSellAmountSum = BigDecimal.ZERO;
        BigDecimal totalRealizedCostSum = BigDecimal.ZERO;

        // 코인별 수익률 계산
        for (Map.Entry<Integer, List<TradeLogDto>> entry : tradeLogsByCoin.entrySet()) {
            int coinId = entry.getKey();
            String coinName= String.valueOf(coinId);
            List<TradeLogDto> logs = entry.getValue();

            BigDecimal totalBuyQuantity = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;
            BigDecimal totalSellQuantity = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;

            // 매수/매도 금액 및 수량 계산
            for (TradeLogDto log : logs) {
                BigDecimal tradeAmount = log.price().multiply(log.quantity());

                if (log.tradeType() == TradeType.BUY) {
                    totalBuyQuantity = totalBuyQuantity.add(log.quantity());
                    totalBuyAmount = totalBuyAmount.add(tradeAmount);
                } else if (log.tradeType() == TradeType.SELL) {
                    totalSellQuantity = totalSellQuantity.add(log.quantity());
                    totalSellAmount = totalSellAmount.add(tradeAmount);
                }
            }

            // 평균 매수가 = 매수 총금액 / 매수 총수량
            BigDecimal averageBuyPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalBuyAmount.divide(totalBuyQuantity, 8, RoundingMode.DOWN)
                    : BigDecimal.ZERO;

            // 실현 원가 = 평균 매수가 * 매도 수량
            BigDecimal realizedCost = averageBuyPrice.multiply(totalSellQuantity);

            // 실현 수익 = 매도 총금액 - 실현 원가
            BigDecimal realizedProfit = totalSellAmount.subtract(realizedCost);

            // 실현 수익률 = 실현 수익 / 실현 원가 × 100
            BigDecimal realizedProfitRate = realizedCost.compareTo(BigDecimal.ZERO) > 0
                    ? realizedProfit.divide(realizedCost, 8, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            // 전체 수익 계산을 위한 누적
            totalSellAmountSum = totalSellAmountSum.add(totalSellAmount);
            totalRealizedCostSum = totalRealizedCostSum.add(realizedCost);

            // 현재 보유 수량 = 총 매수 수량 - 총 매도 수량
            BigDecimal currentHolding = totalBuyQuantity.subtract(totalSellQuantity);

            coinAnalytics.add(new ProfitAnalysisDto(
                    coinName,
                    currentHolding,
                    averageBuyPrice,
                    realizedProfitRate
            ));
        }

        // 전체 실현 수익률 = (전체 매도금액 - 전체 실현 원가) / 총 투자금 × 100
        BigDecimal realizedProfitRateTotal = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalSellAmountSum.subtract(totalRealizedCostSum)
                .divide(totalInvested, 8, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new ProfitRateResponse(
                walletId,
                coinAnalytics,
                realizedProfitRateTotal,     // 전체 실현 수익률
                realizedProfitRateTotal            // 평가 수익률은 이 메서드에서는 계산하지 않음
        );
    }
    /*
     * 지갑 내 가상화폐별 미실현 평가 수익률 계산 (실시간 시세 기반), 투자 금액 대비 수익률
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
    @Transactional(readOnly = true)
    public ProfitRateResponse calculateUnRealizedProfitRates(int walletId) {
        // 사용자 지갑 내 보유 코인 정보 조회 (코인 ID, 수량, 평균 매수가 등 포함)
//        List<CoinHoldingInfo> coinHoldingInfos = walletService.getCoinHoldingsByUserId((long) walletId);
        Coin coin1 = coinService.findBySymbol("BTC")
                .orElseThrow(() -> new IllegalArgumentException("코인 심볼 ETH에 해당하는 코인이 존재하지 않습니다."));

        Coin coin2 = coinService.findBySymbol("ETH")
                .orElseThrow(() -> new IllegalArgumentException("코인 심볼 ETH에 해당하는 코인이 존재하지 않습니다."));
        List<Coin> coins = List.of(coin1, coin2);
        List<CoinHoldingInfo> coinHoldingInfos  = new ArrayList<>();
        BigDecimal coinQuantity = BigDecimal.valueOf(3);
        BigDecimal totalInvestAmount = BigDecimal.valueOf(435_000_000);
        BigDecimal averageBuyPrice = totalInvestAmount.divide(coinQuantity, 2, RoundingMode.HALF_UP);
        CoinHoldingInfo coinHoldingInfo = new CoinHoldingInfo(
                coin1.getId(),
                coin1.getSymbol(),
                coin1.getKoreanName() != null ? coin1.getKoreanName() : coin1.getSymbol(),
                coinQuantity,
                totalInvestAmount, // 총 투자 금액
                averageBuyPrice  // 평균 매수가
        );
        coinHoldingInfos.add(coinHoldingInfo);
        coinQuantity = BigDecimal.valueOf(2);
        totalInvestAmount = BigDecimal.valueOf(440_000_000);
        averageBuyPrice = totalInvestAmount.divide(coinQuantity, 2, RoundingMode.HALF_UP);
        coinHoldingInfo = new CoinHoldingInfo(
                coin2.getId(),
                coin2.getSymbol(),
                coin2.getKoreanName() != null ? coin2.getKoreanName() : coin2.getSymbol(),
                coinQuantity,
                totalInvestAmount, // 총 투자 금액
                averageBuyPrice  // 평균 매수가
        );
        coinHoldingInfos.add(coinHoldingInfo);
        List<ProfitAnalysisDto> coinAnalytics = new ArrayList<>();

        BigDecimal totalInvestedAmount = BigDecimal.ZERO;      // 총 투자 원금 (코인별 매수가 * 수량)
        BigDecimal totalEvaluationAmount = BigDecimal.ZERO;    // 총 평가 금액 (현재가 * 수량)

        for (CoinHoldingInfo info : coinHoldingInfos) {
            // 최신 시세 정보 조회
            CoinPriceResponse coinPriceResponse = exchangeService.getLatestCandleByScan(info.coinName());

            BigDecimal quantity = info.quantity();               // 현재 보유 수량
            BigDecimal avgBuyPrice = info.averageBuyPrice();     // 평균 매수가
            BigDecimal currentPrice = coinPriceResponse.getPrice(); // 현재가

            log.info("코인: {}, 현재가: {}, 수량: {}, 평균단가: {}",
                    info.coinName(), currentPrice, quantity, avgBuyPrice);

            // 수익률 = (현재가 - 평균단가) / 평균단가 * 100
            BigDecimal profitRate = avgBuyPrice.compareTo(BigDecimal.ZERO) > 0
                    ? currentPrice.subtract(avgBuyPrice)
                    .divide(avgBuyPrice, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            // 개별 코인 수익률 분석 정보 추가
            coinAnalytics.add(new ProfitAnalysisDto(
                    String.valueOf(info.coinId()),
                    quantity,
                    avgBuyPrice,
                    profitRate
            ));

            // 총 매수금액 += 평균단가 * 수량
            totalInvestedAmount = totalInvestedAmount.add(avgBuyPrice.multiply(quantity));

            // 총 평가금액 += 현재가 * 수량
            totalEvaluationAmount = totalEvaluationAmount.add(currentPrice.multiply(quantity));
        }

        // 코인 투자 수익률 = (총 평가 - 총 매수) / 총 매수 * 100
        BigDecimal investmentProfitRate = totalInvestedAmount.compareTo(BigDecimal.ZERO) > 0
                ? totalEvaluationAmount.subtract(totalInvestedAmount)
                .divide(totalInvestedAmount, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // 현금 포함 자산 정보 조회
        WalletResponse walletResponse = walletService.getUserWallet((long) walletId).getBody();
        if (walletResponse == null) {
            throw new ErrorException(ErrorCode.WALLET_NOT_FOUND, "null");
        }

        BigDecimal walletBalance = walletResponse.balance(); // 지갑 내 현금 잔액

        // (현금 + 매수금액) 대비 평가 수익률 계산
        BigDecimal totalInvestedWithCash = walletBalance.add(totalInvestedAmount);         // 현금 + 총 투자금
        BigDecimal totalEvaluationWithCash = walletBalance.add(totalEvaluationAmount);     // 현금 + 총 평가금

        BigDecimal totalAssetProfitRate = totalInvestedWithCash.compareTo(BigDecimal.ZERO) > 0
                ? totalEvaluationWithCash.subtract(totalInvestedWithCash)
                .divide(totalInvestedWithCash, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new ProfitRateResponse(
                walletId,
                coinAnalytics,
                investmentProfitRate,     // 코인 평가 수익률
                totalAssetProfitRate      // 현금 포함 총 자산 기준 수익률
        );
    }
}
