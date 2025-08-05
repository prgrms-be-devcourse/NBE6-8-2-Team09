"use client";

import { motion } from "framer-motion";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { walletApi } from "@/lib/api/wallet";
import type { WalletDto } from "@/lib/types/wallet";

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
    const [walletData, setWalletData] = useState<WalletDto | null>(null);
    const [chargeAmount, setChargeAmount] = useState("");
    const [isCharging, setIsCharging] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    // 에러 메시지 state 추가
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const userId = 1; // 실제로는 auth에서 가져와야 함

    // 메시지 초기화 함수
    const clearMessages = () => {
        setErrorMessage("");
        setSuccessMessage("");
    };

    // 지갑 정보 조회 함수
    const fetchWallet = async () => {
        try {
            setIsLoading(true);
            clearMessages();
            const data = await walletApi.getWallet(userId);
            if (data) {
                setWalletData(data);
            }
        } catch (error: any) {
            console.error('지갑 정보 조회 실패:', error);
            setErrorMessage(error.message || '지갑 정보를 불러오는데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    // 충전 함수
    const handleCharge = async () => {
        if (!chargeAmount) return;

        setIsCharging(true);
        clearMessages();
        try {
            const result = await walletApi.charge(userId, parseFloat(chargeAmount));

            if (result !== null) {
                // 충전 후 지갑 정보 다시 조회
                await fetchWallet();
                setChargeAmount("");
                setSuccessMessage("충전이 완료되었습니다.");
            }
        } catch (error: any) {
            console.error('충전 실패:', error);
            setErrorMessage(error.message || '충전에 실패했습니다.');
        } finally {
            setIsCharging(false);
        }
    };

    // 컴포넌트 마운트 시 지갑 정보 조회
    useEffect(() => {
        fetchWallet();
    }, []);

    // 총 자산 계산
    const totalValue = walletData?.coinAmounts.reduce((acc, coin) => acc + coin.totalAmount, 0) || 0;

    if (isLoading) {
        return (
            <div className="container py-8">
                <div>지갑 정보를 불러오는 중...</div>
            </div>
        );
    }

    if (!walletData) {
        return (
            <div className="container py-8">
                <div>지갑 정보를 불러올 수 없습니다.</div>
            </div>
        );
    }

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

            {/* 에러/성공 메시지 표시 */}
            {errorMessage && (
                <motion.div
                    variants={fadeInUp}
                    className="bg-red-50 border border-red-200 rounded-lg p-4"
                >
                    <div className="flex items-center">
                        <div className="text-red-600 text-sm font-medium">
                            ❌ {errorMessage}
                        </div>
                        <button
                            onClick={clearMessages}
                            className="ml-auto text-red-400 hover:text-red-600"
                        >
                            ✕
                        </button>
                    </div>
                </motion.div>
            )}

            {successMessage && (
                <motion.div
                    variants={fadeInUp}
                    className="bg-green-50 border border-green-200 rounded-lg p-4"
                >
                    <div className="flex items-center">
                        <div className="text-green-600 text-sm font-medium">
                            ✅ {successMessage}
                        </div>
                        <button
                            onClick={clearMessages}
                            className="ml-auto text-green-400 hover:text-green-600"
                        >
                            ✕
                        </button>
                    </div>
                </motion.div>
            )}

            {/* 2열 레이아웃 */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* 왼쪽 영역 - 보유 코인 현황 */}
                <div className="lg:col-span-2 space-y-6">
                    {/* 보유 코인 목록 */}
                    <motion.div
                        variants={fadeInUp}
                        className="border rounded-lg p-6"
                    >
                        <h2 className="text-lg font-semibold mb-4">보유 코인 현황</h2>
                        {walletData.coinAmounts.length === 0 ? (
                            <div className="text-center text-muted-foreground py-8">
                                보유한 코인이 없습니다.
                            </div>
                        ) : (
                            <div className="space-y-3">
                                {walletData.coinAmounts.map((coin) => (
                                    <motion.div
                                        key={coin.coinId}
                                        variants={fadeInUp}
                                        className="border rounded-lg p-4 bg-gradient-to-r from-gray-50 to-white hover:shadow-lg transition-all duration-300"
                                    >
                                        <div className="flex justify-between items-start">
                                            <div className="space-y-2">
                                                <div className="flex items-center space-x-2">
                                                    <div className="font-bold text-lg">{coin.coinSymbol}</div>
                                                    <div className="text-sm bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                                        ID: {coin.coinId}
                                                    </div>
                                                </div>
                                                <div className="text-sm text-muted-foreground font-medium">
                                                    {coin.coinName}
                                                </div>
                                                <div className="flex items-center space-x-4 text-sm">
                                                    <div className="bg-green-50 text-green-700 px-3 py-1 rounded-full">
                                                        보유량: <span className="font-semibold">{coin.quantity}</span> {coin.coinSymbol}
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="text-right space-y-1">
                                                <div className="text-lg font-bold text-blue-600">
                                                    ${coin.totalAmount.toLocaleString()}
                                                </div>
                                                <div className="text-xs text-muted-foreground">
                                                    총 투자금액
                                                </div>
                                                <div className="text-xs text-green-600 font-medium">
                                                    평균 단가: ${coin.quantity > 0 ? (coin.totalAmount / coin.quantity).toFixed(2) : '0.00'}
                                                </div>
                                            </div>
                                        </div>
                                    </motion.div>
                                ))}
                            </div>
                        )}
                    </motion.div>

                    {/* 코인별 상세 통계 */}
                    {walletData.coinAmounts.length > 0 && (
                        <motion.div
                            variants={fadeInUp}
                            className="border rounded-lg p-6"
                        >
                            <h2 className="text-lg font-semibold mb-4">투자 분석</h2>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="bg-amber-50 p-4 rounded-lg">
                                    <div className="text-sm text-amber-600 font-medium">가장 많이 투자한 코인</div>
                                    <div className="text-lg font-bold text-amber-800">
                                        {walletData.coinAmounts.reduce((max, coin) =>
                                            coin.totalAmount > max.totalAmount ? coin : max
                                        ).coinSymbol}
                                    </div>
                                    <div className="text-sm text-amber-600">
                                        ${walletData.coinAmounts.reduce((max, coin) =>
                                        coin.totalAmount > max.totalAmount ? coin : max
                                    ).totalAmount.toLocaleString()}
                                    </div>
                                </div>
                                <div className="bg-green-50 p-4 rounded-lg">
                                    <div className="text-sm text-green-600 font-medium">코인 종류 수</div>
                                    <div className="text-lg font-bold text-green-800">
                                        {walletData.coinAmounts.length}개
                                    </div>
                                    <div className="text-sm text-green-600">
                                        포트폴리오 다양성
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                    )}
                </div>

                {/* 오른쪽 영역 - 지갑 정보 */}
                <div className="space-y-6">
                    {/* 지갑 요약 */}
                    <motion.div
                        variants={fadeInUp}
                        className="border rounded-lg p-6 bg-gradient-to-br from-amber-50 to-orange-50"
                    >
                        <h2 className="text-lg font-semibold mb-4">지갑 정보</h2>
                        <div className="space-y-3">
                            <div>
                                <div className="text-sm text-muted-foreground">총 자산</div>
                                <div className="text-2xl font-bold text-amber-600">
                                    ${totalValue.toLocaleString()}
                                </div>
                            </div>
                            <div>
                                <div className="text-sm text-muted-foreground">현재 잔액</div>
                                <div className="text-lg font-semibold">
                                    {walletData.balance.toLocaleString()}원
                                </div>
                            </div>
                            <div>
                                <div className="text-xs text-muted-foreground">지갑 주소</div>
                                <div className="text-xs font-mono bg-gray-100 p-2 rounded text-gray-600 break-all">
                                    {walletData.address}
                                </div>
                            </div>
                        </div>
                    </motion.div>

                    {/* 충전 */}
                    <motion.div
                        variants={fadeInUp}
                        className="border rounded-lg p-6"
                    >
                        <h2 className="text-lg font-semibold mb-4">지갑 충전</h2>
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
                    </motion.div>

                    {/* 요약 통계 */}
                    <motion.div
                        variants={fadeInUp}
                        className="border rounded-lg p-6"
                    >
                        <h2 className="text-lg font-semibold mb-4">보유 현황</h2>
                        <div className="space-y-2">
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">보유 코인 종류</span>
                                <span className="font-medium">{walletData.coinAmounts.length}개</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">총 투자금액</span>
                                <span className="font-medium">${totalValue.toLocaleString()}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">사용 가능 잔액</span>
                                <span className="font-medium">{walletData.balance.toLocaleString()}원</span>
                            </div>
                        </div>
                    </motion.div>
                </div>
            </div>
        </motion.div>
    );
}
