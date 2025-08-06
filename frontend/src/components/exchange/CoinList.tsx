"use client";

import React, { useEffect, useRef, useState } from "react";
import { ExchangeDTO } from "@/lib/types/exchange/type";
import { exchangeApi } from "@/lib/api/exchange";
import Link from "next/link";
import {apiCall} from "@/lib/api/client";
import { FiSettings } from "react-icons/fi";

type SortKey = "name" | "trade_price" | "change_rate" | "candle_acc_trade_volume";
type SortOrder = "asc" | "desc";

interface CoinListProps {
    onSelect?: (symbol: string, coin: ExchangeDTO) => void;
}

interface FilteredCoin extends ExchangeDTO {
    change: number;
    change_rate: number;
}

export const CoinList = ({ onSelect }: CoinListProps) => {
    const [coin, setCoin] = useState<ExchangeDTO[]>([]);
    const [search, setSearch] = useState("");
    const [sortKey, setSortKey] = useState<SortKey>("trade_price");
    const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
    const [selectedMarket, setSelectedMarket] = useState<string | null>(null);
    const [latestSelectedCoin, setLatestSelectedCoin] = useState<ExchangeDTO | null>(null);
    const socketRef = useRef<WebSocket | null>(null);

    const [role, setRole] = useState<string | null>(null);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const me = await apiCall<any>("/v1/users/me", { method: "GET" });
                setRole(me.result.role);
                console.log(me.result.role);
            } catch (error) {
                console.error("사용자 정보를 불러오는 데 실패했습니다.", error);
            }
        };

        fetchUser();
    }, []);

    useEffect(() => {
        const fetchCoins = async () => {
            try {
                const data = await exchangeApi.getLatest();
                setCoin(data);

                const markets = data.map((c) => c.market);

                const ws = new WebSocket("wss://api.upbit.com/websocket/v1");
                socketRef.current = ws;

                ws.onopen = () => {
                    ws.send(
                        JSON.stringify([
                            { ticket: "coin-list" },
                            {
                                type: "candle.1s",
                                codes: markets,
                                isOnlyRealtime: true,
                            },
                        ])
                    );
                };

                ws.onmessage = async (event) => {
                    const blob = event.data as Blob;
                    const text = await blob.text();
                    const data = JSON.parse(text);

                    if (!data || !data.code) return;

                    setCoin((prevCoins) =>
                        prevCoins.map((c) =>
                            c.market === data.code
                                ? {
                                    ...c,
                                    trade_price: data.trade_price,
                                    opening_price: data.opening_price,
                                    high_price: data.high_price,
                                    low_price: data.low_price,
                                    candle_acc_trade_volume: data.candle_acc_trade_volume,
                                }
                                : c
                        )
                    );
                };
            } catch (error) {
                console.error("코인 데이터를 불러오는 데 실패했습니다.", error);
            }
        };

        fetchCoins();

        return () => {
            socketRef.current?.close();
        };
    }, []);

    // 기본 선택 코인 설정 (KRW-BTC)
    useEffect(() => {
        if (coin.length > 0 && !selectedMarket) {
            const btc = coin.find((c) => c.market === "KRW-BTC");
            if (btc) {
                setSelectedMarket(btc.market);
                setLatestSelectedCoin(btc);
            }
        }
    }, [coin, selectedMarket]);

    // 선택된 마켓이 바뀔 때마다 해당 마켓 코인을 업데이트
    useEffect(() => {
        if (selectedMarket) {
            const selected = coin.find((c) => c.market === selectedMarket);
            if (selected) {
                setLatestSelectedCoin(selected);
            }
        }
    }, [coin, selectedMarket]);

    // 최신 선택 코인이 바뀌면 상위로 전달
    useEffect(() => {
        if (latestSelectedCoin && selectedMarket) {
            onSelect?.(selectedMarket, latestSelectedCoin);
        }
    }, [latestSelectedCoin, selectedMarket]);

    const filtered: FilteredCoin[] = coin
        .filter((coin) => {
            const lower = search.toLowerCase();
            return (
                coin.name?.toLowerCase().includes(lower) ||
                coin.market.toLowerCase().includes(lower)
            );
        })
        .map((coin) => {
            const change = coin.trade_price - coin.opening_price;
            const change_rate = (change / coin.opening_price) * 100;
            return {
                ...coin,
                change,
                change_rate,
            };
        })
        .sort((a, b) => {
            const aVal = a[sortKey];
            const bVal = b[sortKey];
            if (typeof aVal === "number" && typeof bVal === "number") {
                return sortOrder === "asc" ? aVal - bVal : bVal - aVal;
            }
            if (typeof aVal === "string" && typeof bVal === "string") {
                return sortOrder === "asc"
                    ? aVal.localeCompare(bVal)
                    : bVal.localeCompare(aVal);
            }
            return 0;
        });

    const handleSort = (key: SortKey) => {
        if (sortKey === key) {
            setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
        } else {
            setSortKey(key);
            setSortOrder("desc");
        }
    };

    return (
        <div className="w-full h-full border rounded bg-white shadow-sm text-sm flex flex-col overflow-x-hidden">
            <div className="p-2 sticky top-0 bg-white z-10 border-b flex items-center justify-between">
                <input
                    type="text"
                    placeholder="코인명/심볼 검색"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="w-full p-1 border rounded text-sm"
                />
                {role === "ADMIN" &&
                    <Link href="/admin/coins/new">
                        <button
                            className="p-2 rounded w-10 h-10 bg-gray-100 hover:bg-gray-200 border border-gray-300 transition"
                            title="코인 등록 페이지 이동"
                        >
                            <FiSettings className="w-5 h-5 text-gray-700" />
                        </button>
                    </Link>
                }

            </div>

            {/* Header Row */}
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 px-2 py-1 text-xs text-gray-500 bg-gray-50 border-b w-full">
                <div className="cursor-pointer" onClick={() => handleSort("name")}>한글명</div>
                <div className="text-right cursor-pointer" onClick={() => handleSort("trade_price")}>현재가</div>
                <div className="text-right cursor-pointer hidden sm:block" onClick={() => handleSort("change_rate")}>전일대비</div>
                <div className="text-right cursor-pointer hidden md:block" onClick={() => handleSort("candle_acc_trade_volume")}>거래량</div>
            </div>

            {/* Coin List */}
            <div className="overflow-y-auto h-full max-h-full w-full min-w-0">
                {filtered.map((coin) => {
                    const isUp = coin.change > 0;
                    const isDown = coin.change < 0;
                    const color = isUp ? "text-red-500" : isDown ? "text-blue-500" : "text-gray-700";

                    return (
                        <div
                            key={coin.market}
                            onClick={() => setSelectedMarket(coin.market)}
                            className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 items-center px-2 py-2 border-b hover:bg-gray-100 cursor-pointer"
                        >
                            <div className="truncate">
                                <div className="font-medium truncate">{coin.name}</div>
                                <div className="text-xs text-gray-400">{coin.market}</div>
                            </div>

                            <div className={`text-right font-semibold ${color}`}>
                                {coin.trade_price.toLocaleString()}
                            </div>

                            <div className={`text-right text-xs ${color} hidden sm:block`}>
                                {coin.change_rate > 0 ? "+" : ""}
                                {coin.change_rate.toFixed(2)}%<br />
                                {coin.change > 0 ? "+" : ""}
                                {coin.change.toLocaleString()}
                            </div>

                            <div className="text-right text-xs text-gray-600 hidden md:block">
                                {coin.candle_acc_trade_volume.toFixed(2)}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};