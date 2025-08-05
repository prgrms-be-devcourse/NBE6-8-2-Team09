"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, TrendingDown, Search, Settings } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useState, useRef, useEffect } from "react"
import {CoinInfo} from "@/lib/types/exchange/type";

interface CoinListProps {
    coins: CoinInfo[]
    selectedCoin: string
    onCoinSelect: (coin: CoinInfo) => void
    userRole?: string
}

export default function CoinList({ coins, selectedCoin, onCoinSelect, userRole = "user" }: CoinListProps) {
    const [searchTerm, setSearchTerm] = useState("")
    const scrollContainerRef = useRef<HTMLDivElement>(null)

    const filteredCoins = coins.filter(
        (coin) =>
            coin.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            coin.code.toLowerCase().includes(searchTerm.toLowerCase()),
    )

    // 코인 목록 독립 스크롤 처리
    useEffect(() => {
        const container = scrollContainerRef.current
        if (!container) return

        const handleWheel = (e: WheelEvent) => {
            // 코인 목록 영역에서는 독립적인 스크롤 허용
            e.stopPropagation()
            // preventDefault는 하지 않아서 자연스러운 스크롤 허용
        }

        container.addEventListener("wheel", handleWheel, { passive: true })

        return () => {
            container.removeEventListener("wheel", handleWheel)
        }
    }, [])

    return (
        <Card className="h-fit">
            <CardHeader>
                <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">코인 목록</CardTitle>
                    {userRole === "admin" && (
                        <Button variant="outline" size="sm">
                            <Settings className="w-4 h-4" />
                        </Button>
                    )}
                </div>
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                        placeholder="코인 검색..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-10"
                    />
                </div>
            </CardHeader>
            <CardContent className="p-0">
                <div
                    ref={scrollContainerRef}
                    className="space-y-1 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100"
                    style={{
                        height: "calc(100vh - 300px)", // 헤더와 기타 요소 높이 제외
                        scrollbarWidth: "thin",
                        scrollbarColor: "#d1d5db #f3f4f6",
                    }}
                >
                    {filteredCoins.map((coin) => (
                        <div
                            key={coin.code}
                            className={`p-3 cursor-pointer hover:bg-gray-50 border-l-4 transition-colors ${
                                selectedCoin === coin.code ? "border-l-blue-500 bg-blue-50" : "border-l-transparent"
                            }`}
                            onClick={() => onCoinSelect(coin)}
                        >
                            <div className="flex items-center justify-between mb-1">
                                <div>
                                    <div className="font-medium text-sm">{coin.name}</div>
                                    <div className="text-xs text-gray-500">{coin.code}</div>
                                </div>
                                <div className="text-right">
                                    <div className="font-medium text-sm">{coin.price.toLocaleString()}</div>
                                    <div
                                        className={`flex items-center text-xs ${
                                            coin.changePercent >= 0 ? "text-green-600" : "text-red-600"
                                        }`}
                                    >
                                        {coin.changePercent >= 0 ? (
                                            <TrendingUp className="w-3 h-3 mr-1" />
                                        ) : (
                                            <TrendingDown className="w-3 h-3 mr-1" />
                                        )}
                                        {coin.changePercent >= 0 ? "+" : ""}
                                        {coin.changePercent.toFixed(2)}%
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center justify-between text-xs text-gray-500">
                                <span>거래량: {coin.volume.toFixed(3)}</span>
                                <span className={coin.change >= 0 ? "text-green-600" : "text-red-600"}>
                  {coin.change >= 0 ? "+" : ""}
                                    {coin.change.toLocaleString()}
                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    )
}
