import React from "react";
import { ExchangeDTO } from "@/lib/types/exchange/type";

interface PriceSummaryProps {
    coin: ExchangeDTO;
}

export const PriceSummary = ({ coin }: PriceSummaryProps) => {
    const change = coin.trade_price - coin.opening_price;
    const changeRate = (change / coin.opening_price) * 100;
    const isUp = change > 0;
    const isDown = change < 0;
    const changeColor = isUp
        ? "text-red-600"
        : isDown
            ? "text-blue-600"
            : "text-gray-700";

    return (
        <div className="w-full border p-4 bg-white shadow-sm">
            <div className="flex justify-between items-center">
                {/* 좌측 - 코인 이름 + 시세 */}
                <div>
                    <div className="text-sm font-semibold">
                        {coin.name} <span className="text-xs text-gray-500">{coin.market}</span>
                    </div>
                    <div className={`text-3xl font-bold ${changeColor}`}>
                        {coin.trade_price.toLocaleString()} <span className="text-base">KRW</span>
                    </div>
                    <div className={`text-sm ${changeColor}`}>
                        {changeRate > 0 ? "+" : ""}
                        {changeRate.toFixed(2)}%
                        &nbsp;
                        <span>{change > 0 ? "▲" : change < 0 ? "▼" : ""}</span>
                        {Math.abs(change).toLocaleString()}
                    </div>
                </div>

                {/* 우측 - 고가/저가/시가/현재가 (lg 이상에서만 보임) */}
                <div className="hidden lg:block text-sm text-gray-600 w-100">
                    <div className="grid grid-cols-4 gap-y-4 gap-x-6">
                        <div>고가</div>
                        <div className="text-right text-red-600">{coin.high_price.toLocaleString()}</div>

                        <div>현재가</div>
                        <div className={`text-right ${changeColor}`}>{coin.trade_price.toLocaleString()}</div>

                        <div>저가</div>
                        <div className="text-right text-blue-600">{coin.low_price.toLocaleString()}</div>

                        <div>시가</div>
                        <div className="text-right font-semibold">{coin.opening_price.toLocaleString()}</div>
                    </div>
                </div>
            </div>
        </div>
    );
};
