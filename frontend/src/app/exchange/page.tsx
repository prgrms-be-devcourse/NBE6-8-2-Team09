"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import type { CoinPriceResponse } from "@/lib/types/type";

export default function ExchangePage() {
    return (
        <Suspense fallback={<div>로딩 중…</div>}>
            <ExchangeContent />
        </Suspense>
    );
}

function ExchangeContent() {
    const [isRunning, setIsRunning] = useState(false);
    const [coin, setCoin]           = useState("");
    const [price, setPrice]         = useState(0);
    const [time, setTime]           = useState("");

    const searchParams = useSearchParams();   // ✅ 이제 Suspense 안쪽
    const router       = useRouter();

    // 쿼리에서 code 읽어 초기값 설정
    useEffect(() => {
        const code = searchParams.get("code");
        if (code) setCoin(code);
    }, [searchParams]);

    const startFetching = async () => {
        await fetch("/api/ws/start");
        setIsRunning(true);
    };
    const stopFetching = async () => {
        await fetch("/api/ws/stop");
        setIsRunning(false);
    };

    const clicktime = async () => {
        if (!coin.trim()) {
            alert("코인 이름을 입력해주세요.");
            return;
        }
        const now         = new Date();
        const koreaTime   = new Date(now.toLocaleString("en-US", { timeZone: "Asia/Seoul" }));
        const pad         = (n: number) => n.toString().padStart(2, "0");
        const formatted   =
            `${koreaTime.getFullYear()}-${pad(koreaTime.getMonth() + 1)}-${pad(koreaTime.getDate())}` +
            ` ${pad(koreaTime.getHours())}:${pad(koreaTime.getMinutes())}:${pad(koreaTime.getSeconds())}`;

        setTime(formatted);
        router.replace(`/exchange?code=${encodeURIComponent(coin)}`);

        const res  = await fetch(`/api/exchange/call?symbol=${encodeURIComponent(coin)}&time=${encodeURIComponent(formatted)}`);
        const data: CoinPriceResponse = await res.json();
        setPrice(data.price);
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
                    value={coin}
                    onChange={(e) => setCoin(e.target.value)}
                    placeholder="예: KRW-BTC"
                    className="px-4 py-2 rounded bg-white border border-gray-300 focus:outline-none text-black"
                />
                <button onClick={clicktime} className="px-4 py-2 rounded text-white bg-blue-600">
                    시간 코인가격 추출 버튼
                </button>
            </div>

            <div className="p-4 flex justify-center text-lg font-mono text-black bg-white">
                {coin && time && price !== 0 && `${coin} ${time} ${price.toLocaleString()}원`}
            </div>
        </>
    );
}
