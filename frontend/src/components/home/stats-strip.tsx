"use client";


import { motion } from "framer-motion";
import { fadeInUp, stagger } from "@/lib/motion";
import { PageShell } from "@/components/layout/page-shell";
import { useEffect, useState } from "react";
import { apiCall } from "@/lib/api/client";
import { walletApi } from "@/lib/api/wallet";
import type { WalletDto } from "@/lib/types/wallet";

export function StatsStrip() {
    const [walletData, setWalletData] = useState<WalletDto | null>(null);
    const [userId, setUserId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const loadUserAndWallet = async () => {
            try {
                // 사용자 정보 먼저 가져오기
                const userResponse = await apiCall<{ result: { id: number } }>('/v1/users/me');
                if (userResponse?.result?.id) {
                    const id = userResponse.result.id;
                    setUserId(id);

                    // 지갑 정보 가져오기
                    const walletResponse = await walletApi.getWallet(id);
                    setWalletData(walletResponse);
                }
            } catch (error) {
                console.error('지갑 데이터 로드 실패:', error);
            } finally {
                setIsLoading(false);
            }
        };

        loadUserAndWallet();
    }, []);

    // 계산된 stats
    const totalInvestment = walletData?.coinAmounts.reduce((acc, coin) => acc + coin.totalAmount, 0) || 0;
    const currentBalance = walletData?.balance || 0;
    const totalAssets = totalInvestment + currentBalance;

    const stats = [
        {
            label: "총 자산",
            value: isLoading ? "로딩중..." : `₩ ${totalAssets.toLocaleString()}`
        },
        {
            label: "총 투자금",
            value: isLoading ? "로딩중..." : `₩ ${totalInvestment.toLocaleString()}`
        },
        {
            label: "투자 가능한 금액",
            value: isLoading ? "로딩중..." : `₩ ${currentBalance.toLocaleString()}`
        },
    ];

    return (
        <section className="py-12">
            <PageShell maxW="max-w-[80vw]" padded>
                <motion.div
                    variants={stagger(0.1)}
                    initial="hidden"
                    whileInView="show"
                    viewport={{ once: true, amount: 0.2 }}
                    className="grid gap-6 md:grid-cols-3"
                >
                </motion.div>
            </PageShell>
        </section>
    );
}
