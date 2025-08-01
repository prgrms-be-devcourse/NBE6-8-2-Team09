"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { CoinPriceResponse } from "@/lib/types/type";

export default function ExchangePage() {
    const [isRunning, setIsRunning] = useState(false);
    const [coin, setCoin] = useState("");
    const [price, setPrice] = useState(0);
    const [time, setTime] = useState("");

    const searchParams = useSearchParams();
    const router = useRouter();

    // 쿼리에서 code 읽어서 초기값으로 설정
    useEffect(() => {
        const code = searchParams.get("code");
        if (code) setCoin(code);
    }, [searchParams]);

    const startFetching = async () => {
        try {
            const response = await fetch("/api/ws/start", { method: "GET" });
            await response.text();
            setIsRunning(true);
        } catch (error) {
            console.error("WebSocket 연결 실패:", error);
        }
    };

    const stopFetching = async () => {
        try {
            const response = await fetch("/api/ws/stop", { method: "GET" });
            await response.text();
            setIsRunning(false);
        } catch (error) {
            console.error("중단 요청 실패:", error);
        }
    };

    const clicktime = async () => {
        if (!coin.trim()) {
            alert("코인 이름을 입력해주세요.");
            return;
        }

        try {
            const now = new Date();
            const koreaTime = new Date(now.toLocaleString("en-US", { timeZone: "Asia/Seoul" }));
            const pad = (n: number) => n.toString().padStart(2, "0");
            const formattedTime =
                `${koreaTime.getFullYear()}-${pad(koreaTime.getMonth() + 1)}-${pad(koreaTime.getDate())}` +
                ` ${pad(koreaTime.getHours())}:${pad(koreaTime.getMinutes())}:${pad(koreaTime.getSeconds())}`;

            setTime(formattedTime);
            router.replace(`/exchange?code=${encodeURIComponent(coin)}`);

            const response = await fetch(
                `/api/exchange/call?symbol=${encodeURIComponent(coin)}&time=${encodeURIComponent(formattedTime)}`,
                { method: "GET" }
            );

            const data: CoinPriceResponse = await response.json();
            setPrice(data.price);
        } catch (error) {
            console.error("가격 정보 요청 실패:", error);
        }
    };

    return (
        <>
            <div className="p-4 flex justify-center">
                <button
                    onClick={isRunning ? stopFetching : startFetching}
                    className={`px-4 py-2 rounded text-white ${isRunning ? "bg-red-600" : "bg-green-600"}`}
                >
                    {isRunning ? "중단" : "실행"}
                </button>
            </div>
            <div className="p-4 flex justify-center gap-2">
                <input
                    type="text"
                    value={coin}
                    onChange={(e) => setCoin(e.target.value)}
                    placeholder="예: KRW-BTC"
                    className="px-4 py-2 rounded bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 text-black"
                />
                <button
                    onClick={clicktime}
                    className="px-4 py-2 rounded text-white bg-blue-600"
                >
                    시간 코인가격 추출 버튼
                </button>
            </div>
            <div className="p-4 flex justify-center text-lg font-mono text-black bg-white">
                {coin && time && price !== 0 && `${coin} ${time} ${price.toLocaleString()}원`}
            </div>
        </>
    );
}
