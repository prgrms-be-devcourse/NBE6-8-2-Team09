"use client";

import React, { forwardRef } from "react";
import { ExchangeDTO } from "@/lib/types/exchange/type";
import { formatPrice, formatTime } from "./utils/chartUtils";

interface Props {
    candles: ExchangeDTO[];
    crosshair: { x: number; y: number; index: number } | null;
    overlayInfo: {
        xLabel: string;
        yLabel: string;
        x: number;
        y: number;
    } | null;
}

export const ChartOverlay = forwardRef<HTMLDivElement, Props>(
    ({ candles, crosshair, overlayInfo }, ref) => {
        if (!crosshair || !overlayInfo || candles.length === 0) return null;

        const candle = candles[crosshair.index];
        if (!candle) return null;

        const { xLabel, yLabel, x, y } = overlayInfo;

        return (
            <div ref={ref} className="absolute inset-0 pointer-events-none text-xs">
                <div className="absolute top-0 bottom-0 w-[1px] bg-black/30" style={{ left: crosshair.x }} />
                <div className="absolute left-0 right-0 h-[1px] bg-black/30" style={{ top: crosshair.y }} />
                <div className="absolute bg-black text-white px-1 py-[1px] rounded text-[10px]"
                     style={{ left: Math.max(0, Math.min(x - 25, 400)), top: "calc(100% - 20px)" }}>
                    {xLabel}
                </div>
                <div className="absolute bg-black text-white px-1 py-[1px] rounded text-[10px]"
                     style={{ top: y - 8, right: 0 }}>
                    {yLabel}
                </div>
                <div className="absolute bg-white border rounded shadow p-2"
                     style={{ left: "50%", transform: "translateX(-50%)", top: 8 }}>
                    <div className="text-[10px] text-gray-500 mb-1">
                        {formatTime(new Date(candle.candle_date_time_kst))}
                    </div>
                    <div className="text-[11px]">시가: {formatPrice(candle.opening_price)}</div>
                    <div className="text-[11px] text-red-500">고가: {formatPrice(candle.high_price)}</div>
                    <div className="text-[11px] text-blue-500">저가: {formatPrice(candle.low_price)}</div>
                    <div className="text-[11px] font-semibold">종가: {formatPrice(candle.trade_price)}</div>
                </div>
            </div>
        );
    }
);

ChartOverlay.displayName = "ChartOverlay";