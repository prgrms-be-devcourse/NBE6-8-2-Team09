"use client";

import {useRef} from "react";
import {ExchangeDTO} from "@/lib/types/exchange/type";

interface Props {
    onPan: (delta: number) => void;
    isInChartArea: (x: number, y: number) => boolean;
    canvas: React.RefObject<HTMLCanvasElement|null>;
    candles: ExchangeDTO[];
    padding: number;
    xAxisHeight: number;
    yAxisWidth: number;
    onHover: (data: ExchangeDTO | null, index?: number, x?: number, y?: number) => void;
    startIndex: number;
    candlesPerScreen: number;
}

export const useChartInteraction = ({
                                        onPan,
                                        isInChartArea,
                                        canvas,
                                        candles,
                                        padding,
                                        xAxisHeight,
                                        yAxisWidth,
                                        onHover,
                                        startIndex,
                                        candlesPerScreen,
                                    }: Props) => {
    const isDragging = useRef(false);
    const lastX = useRef<number | null>(null);

    const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!isInChartArea(e.nativeEvent.offsetX, e.nativeEvent.offsetY)) return;
        isDragging.current = true;
        lastX.current = e.clientX;
    };

    const handleMouseUp = () => {
        isDragging.current = false;
        lastX.current = null;
    };

    const handleMouseLeave = () => {
        isDragging.current = false;
        lastX.current = null;
        onHover(null);
    };

    const handleMouseMoveDrag = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!isDragging.current || lastX.current === null) return;
        const delta = e.clientX - lastX.current;
        lastX.current = e.clientX;
        onPan(delta);
    };

    const handleWheel = (e: React.WheelEvent<HTMLCanvasElement>) => {
        if (!isInChartArea(e.nativeEvent.offsetX, e.nativeEvent.offsetY)) return;
        onPan(e.deltaY);
    };

    const handleMouseMoveHover = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!canvas.current) return;

        const rect = canvas.current.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        const chartWidth = rect.width - yAxisWidth;
        const spacing = 1;
        const candleWidth = (chartWidth - (candlesPerScreen - 1) * spacing) / candlesPerScreen;
        const totalWidthPerCandle = candleWidth + spacing;

        const indexFromRight = Math.floor((rect.width - yAxisWidth - x) / totalWidthPerCandle);
        const index = startIndex + indexFromRight;

        if (index < 0 || index >= candles.length) {
            onHover(null);
            return;
        }

        const data = candles[index];
        if (!data) {
            onHover(null);
            return;
        }

        onHover(data, index, x, y);
    };

    return {
        handleMouseDown,
        handleMouseUp,
        handleMouseLeave,
        handleMouseMoveDrag,
        handleWheel,
        handleMouseMoveHover,
    };
};