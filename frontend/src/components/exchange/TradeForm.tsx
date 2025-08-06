"use client";

import React, {useEffect, useState} from "react";
import {ExchangeDTO} from "@/lib/types/exchange/type";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from "@/components/ui/select";
import {apiCall} from "@/lib/api/client";

interface TradeFormProps {
    selectedCoin: ExchangeDTO | null;
}

export const TradeForm: React.FC<TradeFormProps> = ({ selectedCoin }) => {
    const [orderType, setOrderType] = useState("limit");
    const [tab, setTab] = useState<"buy" | "sell">("buy");

    const [price, setPrice] = useState("");
    const [volume, setVolume] = useState("");
    const [total, setTotal] = useState("");
    const [lastChanged, setLastChanged] = useState<"volume" | "total" | null>(null);
    const [userId, setUserId] = useState<number | null>(null);

    useEffect(() => {
        if (selectedCoin) {
            const currentPrice = selectedCoin.trade_price.toString();
            setPrice(currentPrice);
        }
    }, [selectedCoin]);

    useEffect(() => {
        const priceVal = parseFloat(price);
        const volumeVal = parseFloat(volume);
        const totalVal = parseFloat(total);

        if (orderType === "market") return;

        if (lastChanged === "volume" && !isNaN(priceVal) && !isNaN(volumeVal)) {
            const calculated = priceVal * volumeVal;
            setTotal(calculated > 0 ? calculated.toFixed(2) : "");
        }

        if (lastChanged === "total" && !isNaN(priceVal) && !isNaN(totalVal)) {
            const calculated = totalVal / priceVal;
            setVolume(calculated > 0 ? calculated.toFixed(6) : "");
        }
    }, [price, volume, total, lastChanged, orderType]);


    const handleSubmit = async () => {
        const typeText = tab === "buy" ? "매수" : "매도";
        const tradeType = tab === "buy" ? "BUY" : "SELL";
        const ordersMethod = orderType === "limit" ? "LIMIT" : "MARKET";

        try {
            const me = await apiCall<any>("/v1/users/me", { method: "GET" });
            const walletId = me?.result?.id;

            if (!walletId) {
                alert("유저 ID를 가져올 수 없습니다.");
                return;
            }

            const payload = {
                coinSymbol: selectedCoin?.market || "",
                tradeType,                     // BUY or SELL
                ordersMethod,                 // LIMIT or MARKET
                quantity: volume,
                price: price,
            };

            const orderResponse = await apiCall("/orders/wallet/" + walletId, {
                method: "POST",
                body: JSON.stringify(payload),
                headers: { "Content-Type": "application/json" },
            });

            alert(
                `[주문 성공]\n유형: ${typeText}\n종목: ${selectedCoin?.market}\n수량: ${volume}개\n금액: ${total}원\n단가: ${price}`
            );
        } catch (error) {
            console.error("주문 실패", error);
            alert("주문 요청 중 오류가 발생했습니다.");
        }
    };


    const handleQuickAdd = (amount: number) => {
        const currentTotal = parseFloat(total) || 0;
        const newTotal = currentTotal + amount;
        setTotal(newTotal.toFixed(2));
        setLastChanged("total");
    };

    const handleReset = () => {
        setVolume("");
        setTotal("");
        setLastChanged(null);
    };

    const buttonColor =
        tab === "buy" ? "bg-blue-500 hover:bg-blue-600" : "bg-red-500 hover:bg-red-600";

    return (
        <div className="border p-4 bg-white text-sm rounded max-h-[700px]">
            {/* 주문 유형 */}
            <Select value={orderType} onValueChange={setOrderType}>
                <SelectTrigger className="w-full mb-2">
                    <SelectValue placeholder="주문유형 선택" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="market">시장가 (Market)</SelectItem>
                    <SelectItem value="limit">지정가 (Limit)</SelectItem>
                </SelectContent>
            </Select>

            {/* 탭 */}
            <div className="grid grid-cols-2 text-center mb-2 border rounded overflow-hidden">
                <button
                    className={`py-2 ${tab === "buy" ? "bg-blue-100 font-semibold" : "bg-white"}`}
                    onClick={() => setTab("buy")}
                >
                    매수하기
                </button>
                <button
                    className={`py-2 ${tab === "sell" ? "bg-red-100 font-semibold" : "bg-white"}`}
                    onClick={() => setTab("sell")}
                >
                    매도하기
                </button>
            </div>

            {/* 단가 */}
            <div className="mb-2">
                <label className="text-xs text-gray-500">구매 단가</label>
                <Input
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    disabled={orderType === "market"}
                    placeholder="시세 가져오기"
                />
            </div>

            {/* 수량 */}
            <div className="mb-2">
                <label className="text-xs text-gray-500">구매 수량</label>
                <Input
                    value={volume}
                    onChange={(e) => {
                        setVolume(e.target.value);
                        setLastChanged("volume");
                    }}
                    placeholder="수량 입력"
                />
            </div>

            {/* 총 금액 */}
            <div className="mb-2">
                <label className="text-xs text-gray-500">총 구매 금액</label>
                <Input
                    value={total}
                    onChange={(e) => {
                        setTotal(e.target.value);
                        setLastChanged("total");
                    }}
                    placeholder="금액 입력"
                />
            </div>

            {/* 빠른입력 버튼 */}
            <div className="flex gap-2 sm:gap-3 mb-4 justify-between overflow-hidden">
                {[1000, 10000].map((amount) => (
                    <Button
                        key={amount}
                        type="button"
                        variant="outline"
                        className="text-xs px-2 py-1 text-blue-500 border-blue-300 flex-1 min-w-[70px]"
                        onClick={() => handleQuickAdd(amount)}
                    >
                        +{amount.toLocaleString()}원
                    </Button>
                ))}

                {/* 큰 화면에서만 표시되는 버튼 */}
                {[50000, 100000].map((amount) => (
                    <Button
                        key={amount}
                        type="button"
                        variant="outline"
                        className="hidden sm:flex text-xs px-2 py-1 text-blue-500 border-blue-300 flex-1 min-w-[70px]"
                        onClick={() => handleQuickAdd(amount)}
                    >
                        +{amount.toLocaleString()}원
                    </Button>
                ))}

                <Button
                    type="button"
                    variant="outline"
                    className="text-xs px-2 py-1 text-red-500 border-red-300 w-[70px]"
                    onClick={handleReset}
                >
                    초기화
                </Button>
            </div>

            {/* 주문 버튼 */}
            <Button className={`w-full text-white ${buttonColor}`} onClick={handleSubmit}>
                {tab === "buy" ? "구매하기" : "판매하기"}
            </Button>
        </div>
    );
};