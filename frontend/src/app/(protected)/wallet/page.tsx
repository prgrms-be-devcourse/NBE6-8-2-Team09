"use client";

import { motion } from "framer-motion";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { walletApi } from "@/lib/api/wallet";
import type { WalletDto } from "@/lib/api/types";

// 모의 데이터는 그대로 유지
const mock = [
    { symbol: "BTC", balance: 0.22, valueUSD: 20000, coinId: 1 },
    { symbol: "ETH", balance: 3.12, valueUSD: 9000, coinId: 2 },
    { symbol: "USDT", balance: 1560, valueUSD: 1560, coinId: 3 },
];

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

const stagger = (delay = 0.1) => ({
    hidden: {},
    show: {
        transition: {
            staggerChildren: delay,
        },
    },
});

export default function WalletPage() {
    const [walletData, setWalletData] = useState<{ [coinId: number]: WalletDto }>({});
    const [chargeAmount, setChargeAmount] = useState("");
    const [selectedCoin, setSelectedCoin] = useState<{ symbol: string; coinId: number } | null>(null);
    const [isCharging, setIsCharging] = useState(false);
    const userId = 1; // 실제로는 auth에서 가져와야 함

    // 잔액 조회 함수
    const fetchBalance = async (coinId: number) => {
        try {
            const data = await walletApi.getBalance(userId, coinId);
            if (data) {
                setWalletData(prev => ({ ...prev, [coinId]: data }));
            }
        } catch (error) {
            console.error('잔액 조회 실패:', error);
        }
    };

    // 충전 함수
    const handleCharge = async () => {
        if (!selectedCoin || !chargeAmount) return;

        setIsCharging(true);
        try {
            const result = await walletApi.charge(userId, selectedCoin.coinId, parseFloat(chargeAmount));

            if (result !== null) {
                // 충전 후 잔액 다시 조회
                await fetchBalance(selectedCoin.coinId);
                setChargeAmount("");
                setSelectedCoin(null);
            }
        } catch (error) {
            console.error('충전 실패:', error);
        } finally {
            setIsCharging(false);
        }
    };

    // 컴포넌트 마운트 시 모든 코인의 잔액 조회
    useEffect(() => {
        mock.forEach(coin => {
            fetchBalance(coin.coinId);
        });
    }, []);

    const total = mock.reduce((acc, c) => acc + c.valueUSD, 0);

    return (
        <motion.div
            className="container py-8 space-y-6"
            variants={stagger(0.1)}
            initial="hidden"
            animate="show"
        >
            <motion.h1
                variants={fadeInUp}
                className="text-2xl font-bold"
            >
                지갑
            </motion.h1>

            <motion.div
                variants={fadeInUp}
                className="border rounded-lg p-6"
            >
                <div className="text-sm text-muted-foreground">총 자산</div>
                <div className="text-2xl font-semibold">${total.toLocaleString()}</div>
            </motion.div>

            <motion.div
                variants={stagger(0.05)}
                className="space-y-2"
            >
                {mock.map((c) => {
                    const walletInfo = walletData[c.coinId];
                    const actualBalance = walletInfo?.balance ?? c.balance;

                    return (
                        <motion.div
                            key={c.symbol}
                            variants={fadeInUp}
                            className="flex justify-between items-center border rounded-md p-4 transition hover:shadow-md hover:scale-[1.01]"
                        >
                            <div>
                                <div className="font-medium">{c.symbol}</div>
                                <div className="text-sm text-muted-foreground">
                                    {actualBalance} {c.symbol}
                                </div>
                                {walletInfo && (
                                    <div className="text-xs text-muted-foreground">
                                        주소: {walletInfo.address}
                                    </div>
                                )}
                            </div>
                            <div className="flex items-center gap-4">
                                <div className="text-sm text-muted-foreground">
                                    ${c.valueUSD.toLocaleString()}
                                </div>
                                <Dialog>
                                    <DialogTrigger asChild>
                                        <Button
                                            size="sm"
                                            onClick={() => setSelectedCoin({ symbol: c.symbol, coinId: c.coinId })}
                                        >
                                            충전
                                        </Button>
                                    </DialogTrigger>
                                    <DialogContent>
                                        <DialogHeader>
                                            <DialogTitle>{c.symbol} 충전</DialogTitle>
                                        </DialogHeader>
                                        <div className="space-y-4">
                                            <div>
                                                <label className="text-sm font-medium">충전 금액</label>
                                                <Input
                                                    type="number"
                                                    placeholder="충전할 금액을 입력하세요"
                                                    value={chargeAmount}
                                                    onChange={(e) => setChargeAmount(e.target.value)}
                                                />
                                            </div>
                                            <Button
                                                onClick={handleCharge}
                                                disabled={isCharging || !chargeAmount}
                                                className="w-full"
                                            >
                                                {isCharging ? "충전 중..." : "충전하기"}
                                            </Button>
                                        </div>
                                    </DialogContent>
                                </Dialog>
                            </div>
                        </motion.div>
                    );
                })}
            </motion.div>
        </motion.div>
    );
}
