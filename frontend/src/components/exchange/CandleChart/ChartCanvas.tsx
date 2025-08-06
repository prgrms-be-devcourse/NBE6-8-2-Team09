"use client";

import React, {useEffect, useRef, useState} from "react";
import {CandleInterval, ExchangeDTO} from "@/lib/types/exchange/type";
import {exchangeApi} from "@/lib/api/exchange";

const X_AXIS_HEIGHT = 50;
const Y_AXIS_WIDTH = 100;
const CANDLE_COUNT = 50;
const CANDLE_GAP = 2;

interface Props {
    interval: CandleInterval;
    symbol: string;
    candles: ExchangeDTO[];
}

export const ChartCanvas: React.FC<Props> = ({ interval, symbol, candles: initialCandles }) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [mousePos, setMousePos] = useState<{ x: number; y: number } | null>(null);
    const [candles, setCandles] = useState<ExchangeDTO[]>(initialCandles);
    const socketRef = useRef<WebSocket | null>(null);

    const intervalToUpbitType = (interval: CandleInterval): string => {
        switch (interval) {
            case "SEC":
                return "candle.1s";
            case "MIN_1":
                return "candle.1m";
            case "MIN_30":
                return "candle.30m";
            case "HOUR_1":
                return "candle.60m";
            case "DAY":
                return "candle.day";
            case "WEEK":
                return "candle.week";
            case "MONTH":
                return "candle.month";
            default:
                return "candle.1m";
        }
    };

    // ✅ WebSocket 연결 및 데이터 수신
    useEffect(() => {
        let ws: WebSocket | null = null;
        let isMounted = true;

        const fetchInitialCandles = async () => {
            try {
                const response = await exchangeApi.getInitialCandles({
                    market: symbol,
                    interval,
                });

                if (isMounted) {
                    setCandles(response.reverse());
                }
            } catch (err) {
                console.error("초기 캔들 데이터 로딩 실패:", err);
            }
        };

        const connectWebSocket = () => {
            ws = new WebSocket("wss://api.upbit.com/websocket/v1");

            ws.onopen = () => {
                const upbitIntervalMap: Record<CandleInterval, string> = {
                    SEC: "1s",
                    MIN_1: "1m",
                    MIN_30: "30m",
                    HOUR_1: "60m",
                    DAY: "day",
                    WEEK: "week",
                    MONTH: "month",
                    YEAR: "month", // Upbit에는 year 없음. 임시 처리
                };

                const type = `candle.${upbitIntervalMap[interval] || "1m"}`;

                ws?.send(
                    JSON.stringify([
                        { ticket: "chart-realtime" },
                        {
                            type,
                            codes: [symbol],
                            isOnlyRealtime: true,
                        },
                    ])
                );
            };

            ws.onmessage = async (event) => {
                const blob = event.data as Blob;
                const text = await blob.text();
                const data = JSON.parse(text);

                if (!data || data.code !== symbol) return;

                setCandles((prev) => {
                    const latest = prev[prev.length - 1];

                    if (!latest || latest.candle_date_time_kst !== data.candle_date_time_kst) {
                        return [...prev.slice(-CANDLE_COUNT + 1), data];
                    }

                    const merged = {
                        ...latest,
                        high_price: Math.max(latest.high_price, data.high_price),
                        low_price: Math.min(latest.low_price, data.low_price),
                        trade_price: data.trade_price,
                        timestamp: data.timestamp,
                    };

                    return [...prev.slice(0, -1), merged];
                });
            };

            ws.onerror = (e) => console.error("WebSocket 에러", e);
            ws.onclose = () => console.log("WebSocket 종료됨");
        };

        fetchInitialCandles();
        connectWebSocket();

        return () => {
            isMounted = false;
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.close();
            }
        };
    }, [symbol, interval]);

    const intervalToMs = (interval: CandleInterval): number => {
        switch (interval) {
            case "SEC": return 1000;
            case "MIN_1": return 60 * 1000;
            case "MIN_30": return 30 * 60 * 1000;
            case "HOUR_1": return 60 * 60 * 1000;
            case "DAY": return 24 * 60 * 60 * 1000;
            case "WEEK": return 7 * 24 * 60 * 60 * 1000;
            case "MONTH": return 30 * 24 * 60 * 60 * 1000;
            case "YEAR": return 365 * 24 * 60 * 60 * 1000;
            default: return 60 * 1000;
        }
    };

    const drawYAxisPriceLabels = (ctx: CanvasRenderingContext2D, chartWidth: number, chartHeight: number, candles: ExchangeDTO[]) => {
        const graphHeight = chartHeight - X_AXIS_HEIGHT;
        const visible = candles.slice(-CANDLE_COUNT);
        if (visible.length === 0) return;

        const prices = visible.flatMap(c => [c.opening_price, c.high_price, c.low_price, c.trade_price]);
        const maxPrice = Math.max(...prices);
        const minPrice = Math.min(...prices);
        const stepCount = 5;
        const step = (maxPrice - minPrice) / stepCount;

        ctx.fillStyle = "#333";
        ctx.font = "10px sans-serif";
        ctx.textAlign = "right";
        ctx.textBaseline = "middle";
        ctx.strokeStyle = "#e0e0e0";

        for (let i = 0; i <= stepCount; i++) {
            const price = minPrice + step * i;
            const y = graphHeight - ((price - minPrice) / (maxPrice - minPrice)) * graphHeight;

            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(chartWidth - Y_AXIS_WIDTH, y);
            ctx.stroke();
            ctx.fillText(price.toFixed(2), chartWidth - 5, y);
        }
    };

    const drawXAxisTimeLabels = (ctx: CanvasRenderingContext2D, chartWidth: number, chartHeight: number, interval: CandleInterval) => {
        const ms = intervalToMs(interval);
        const graphWidth = chartWidth - Y_AXIS_WIDTH;
        const graphHeight = chartHeight - X_AXIS_HEIGHT;
        const alignedNow = Math.floor(Date.now() / ms) * ms;
        const candleWidth = Math.floor(graphWidth / CANDLE_COUNT);
        const labelEvery = Math.ceil(CANDLE_COUNT / 6);

        ctx.fillStyle = "#000";
        ctx.font = "10px sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "top";

        for (let i = 0; i < CANDLE_COUNT; i++) {
            if (i % labelEvery !== 0) continue;
            const x = graphWidth - (i + 0.5) * candleWidth;
            const timestamp = alignedNow - i * ms;
            const timeStr = new Date(timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
            ctx.fillText(timeStr, x, graphHeight + 4);
        }
    };

    const drawCandles = (ctx: CanvasRenderingContext2D, chartWidth: number, chartHeight: number, candles: ExchangeDTO[]) => {
        const graphWidth = chartWidth - Y_AXIS_WIDTH;
        const graphHeight = chartHeight - X_AXIS_HEIGHT;
        const visible = candles.slice(-CANDLE_COUNT);
        if (visible.length === 0) return;

        const prices = visible.flatMap(c => [c.opening_price, c.high_price, c.low_price, c.trade_price]);
        const maxPrice = Math.max(...prices);
        const minPrice = Math.min(...prices);
        const scaleY = (price: number) => graphHeight - ((price - minPrice) / (maxPrice - minPrice)) * graphHeight;
        const candleWidth = Math.floor(graphWidth / CANDLE_COUNT) - CANDLE_GAP;

        visible.forEach((candle, i) => {
            const x = i * (candleWidth + CANDLE_GAP);
            const openY = scaleY(candle.opening_price);
            const closeY = scaleY(candle.trade_price);
            const highY = scaleY(candle.high_price);
            const lowY = scaleY(candle.low_price);
            const isUp = candle.trade_price >= candle.opening_price;
            const color = isUp ? "#e74c3c" : "#3498db";

            ctx.strokeStyle = color;
            ctx.fillStyle = color;
            ctx.beginPath();
            ctx.moveTo(x + candleWidth / 2, highY);
            ctx.lineTo(x + candleWidth / 2, lowY);
            ctx.stroke();

            const bodyY = Math.min(openY, closeY);
            const bodyH = Math.max(Math.abs(openY - closeY), 1);
            ctx.fillRect(x, bodyY, candleWidth, bodyH);
        });
    };

    const drawCrosshairOverlay = (ctx: CanvasRenderingContext2D, mousePos: { x: number; y: number }, chartWidth: number, chartHeight: number) => {
        const graphWidth = chartWidth - Y_AXIS_WIDTH;
        const graphHeight = chartHeight - X_AXIS_HEIGHT;
        const { x, y } = mousePos;
        if (x > graphWidth || y > graphHeight) return;

        const ms = intervalToMs(interval);
        const alignedNow = Math.floor(Date.now() / ms) * ms;
        const candleWidth = Math.floor(graphWidth / CANDLE_COUNT);
        const offsetIndex = Math.floor((graphWidth - x) / candleWidth);
        const timestamp = alignedNow - offsetIndex * ms;
        const timeStr = new Date(timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

        const visible = candles.slice(-CANDLE_COUNT);
        const prices = visible.flatMap(c => [c.opening_price, c.high_price, c.low_price, c.trade_price]);
        const maxPrice = Math.max(...prices);
        const minPrice = Math.min(...prices);
        const price = minPrice + ((graphHeight - y) / graphHeight) * (maxPrice - minPrice);
        const priceStr = price.toFixed(2);

        ctx.strokeStyle = "#999";
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, graphHeight);
        ctx.moveTo(0, y);
        ctx.lineTo(graphWidth, y);
        ctx.stroke();
        ctx.setLineDash([]);

        ctx.fillStyle = "#000";
        ctx.font = "10px sans-serif";
        ctx.fillRect(x - 25, graphHeight + 2, 50, 16);
        ctx.fillStyle = "#fff";
        ctx.textAlign = "center";
        ctx.fillText(timeStr, x, graphHeight + 4);

        ctx.fillStyle = "#000";
        ctx.fillRect(chartWidth - Y_AXIS_WIDTH, y - 8, Y_AXIS_WIDTH, 16);
        ctx.fillStyle = "#fff";
        ctx.textAlign = "right";
        ctx.fillText(priceStr, chartWidth - 5, y);
    };

    const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
        const rect = e.currentTarget.getBoundingClientRect();
        setMousePos({ x: e.clientX - rect.left, y: e.clientY - rect.top });
    };

    const handleMouseLeave = () => setMousePos(null);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        const dpr = window.devicePixelRatio || 1;
        const width = canvas.clientWidth * dpr;
        const height = canvas.clientHeight * dpr;
        canvas.width = width;
        canvas.height = height;
        ctx.scale(dpr, dpr);

        const drawWidth = width / dpr;
        const drawHeight = height / dpr;
        ctx.clearRect(0, 0, drawWidth, drawHeight);

        drawYAxisPriceLabels(ctx, drawWidth, drawHeight, candles);
        drawXAxisTimeLabels(ctx, drawWidth, drawHeight, interval);
        drawCandles(ctx, drawWidth, drawHeight, candles);
        if (mousePos) drawCrosshairOverlay(ctx, mousePos, drawWidth, drawHeight);
    }, [mousePos, interval, candles]);

    return (
        <div className="relative w-full h-full">
            <canvas
                ref={canvasRef}
                className="absolute inset-0 w-full h-full"
                onMouseMove={handleMouseMove}
                onMouseLeave={handleMouseLeave}
            />
        </div>
    );
};