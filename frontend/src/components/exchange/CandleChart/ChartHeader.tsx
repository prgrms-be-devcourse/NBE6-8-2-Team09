"use client";

import React from "react";
import { CandleInterval } from "@/lib/types/exchange/type";

interface Props {
    market: string;
    interval: CandleInterval;
    onChange: (interval: CandleInterval) => void;
    maType: number[];
    setMAType: (ma: number[]) => void;
    onReset: () => void;
}

// 인터벌 값과 한글 텍스트 매핑
const IntervalDisplayMap: Record<CandleInterval, string> = {
    [CandleInterval.SEC]: "초",
    [CandleInterval.MIN_1]: "1분",
    [CandleInterval.MIN_30]: "30분",
    [CandleInterval.HOUR_1]: "1시간",
    [CandleInterval.DAY]: "1일",
    [CandleInterval.WEEK]: "1주",
    [CandleInterval.MONTH]: "1개월",
    [CandleInterval.YEAR]: "1년", // 추가된 항목
};

const intervals: CandleInterval[] = [
    CandleInterval.SEC,
    CandleInterval.MIN_1,
    CandleInterval.MIN_30,
    CandleInterval.HOUR_1,
    CandleInterval.DAY,
    CandleInterval.WEEK,
    CandleInterval.MONTH,
];

export const ChartHeader: React.FC<Props> = ({ market, interval, onChange, maType, setMAType, onReset }) => {
    const toggleMA = (value: number) => {
        setMAType(maType.includes(value) ? maType.filter((v) => v !== value) : [...maType, value]);
    };

    return (
        <div className="flex justify-between items-center p-2 bg-gray-100 border-b text-sm">
            <div className="flex items-center gap-2">
                <span className="font-semibold">{market}</span>
                {/* Dropdown Select로 변경 */}
                <select
                    value={interval}
                    onChange={(e) => onChange(e.target.value as CandleInterval)}
                    className="px-2 py-1 rounded border"
                >
                    {intervals.map((intv) => (
                        <option key={intv} value={intv}>
                            {IntervalDisplayMap[intv]}
                        </option>
                    ))}
                </select>
            </div>
            <div className="flex items-center gap-2">
                <span>MA:</span>
                {[15, 50].map((v) => (
                    <button
                        key={v}
                        onClick={() => toggleMA(v)}
                        className={`px-2 py-1 rounded text-xs ${maType.includes(v) ? "bg-green-500 text-white" : "bg-white border"}`}
                    >
                        {v}
                    </button>
                ))}
                <button onClick={onReset} className="text-xs px-2 py-1 border rounded">
                    Reset
                </button>
            </div>
        </div>
    );
};