"use client";

import { useEffect, useRef, useState } from "react";
import { PriceSummary } from "@/components/exchange/PriceSummary";
import { CoinList } from "@/components/exchange/CoinList";
import { ExchangeDTO, CandleInterval } from "@/lib/types/exchange/type";
import { TradeForm } from "@/components/exchange/TradeForm";
import { CandleChart } from "@/components/exchange/CandleChart/CandleChart";

export default function TradingPage() {
    const [selectedCoin, setSelectedCoin] = useState<ExchangeDTO | null>(null);
    const leftRef = useRef<HTMLDivElement>(null);
    const [leftHeight, setLeftHeight] = useState<number>(0);
    const [isWide, setIsWide] = useState<boolean>(typeof window !== "undefined" ? window.innerWidth > 1480 : true);

    // 창 너비에 따라 레이아웃 전환
    const updateLayout = () => {
        setIsWide(window.innerWidth > 1480);
    };

    // 높이 측정 + 레이아웃 반응형 감지
    useEffect(() => {
        const update = () => {
            updateLayout();
            if (leftRef.current) {
                setLeftHeight(leftRef.current.offsetHeight);
            }
        };

        update();
        window.addEventListener("resize", update);
        return () => window.removeEventListener("resize", update);
    }, [selectedCoin]);

    return (
        <div
            className={`flex gap-4 p-4 overflow-x-hidden justify-between ${
                isWide ? "flex-row" : "flex-col"
            }`}
        >
            {/* 차트 영역 */}
            <div
                ref={leftRef}
                className="flex flex-col flex-1 min-h-[600px] gap-4 justify-between w-full min-w-[300px] sm:min-w-[500px] [@media(max-width:2240px)]:min-w-[800px] lg:min-w-[1400px]"
            >
                {selectedCoin && (
                    <>
                        <PriceSummary coin={selectedCoin} />
                        <CandleChart market={selectedCoin.market} intval={CandleInterval.MIN_1} />
                        <TradeForm selectedCoin={selectedCoin} />
                    </>
                )}
            </div>

            {/* 코인 리스트 */}
            <div
                className="w-full min-w-[280px] flex-1"
                style={selectedCoin ? { height: leftHeight } : undefined}
            >
                <CoinList onSelect={(market, coin) => setSelectedCoin(coin)} />
            </div>
        </div>
    );
}
