"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { ArrowUpRight, ArrowDownRight } from "lucide-react";

import { cn } from "@/lib/utils";
import { PageShell } from "@/components/layout/page-shell";
import { Card, CardContent } from "@/components/ui/card";
import { ProfitRateResponse, ProfitAnalysisDto } from "@/lib/types/analytics";
import { analyticsApi } from "@/lib/api/analytics";
import { DataTable } from "@/components/ui/data-table";
import { ColumnDef } from "@tanstack/react-table";
import { apiCall } from "@/lib/api/client";

const columns: ColumnDef<ProfitAnalysisDto>[] = [
    { accessorKey: "coinName", header: "코인 이름" },
    {
        accessorKey: "totalQuantity",
        header: "보유 수량",
        cell: ({ row }) => `${row.getValue("totalQuantity") as number} 주`,
    },
    {
        accessorKey: "averageBuyPrice",
        header: "평균 구매 금액",
        cell: ({ row }) => `$ ${Number(row.getValue("averageBuyPrice")).toLocaleString()}`,
    },
    {
        accessorKey: "realizedProfitRate",
        header: "실현 수익률",
        cell: ({ row }) => `${Number(row.getValue("realizedProfitRate")).toFixed(2)} %`,
    },
];

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};
const stagger = (delay = 0.1) => ({
    hidden: {},
    show: { transition: { staggerChildren: delay } },
});

export default function TransactionsPage() {
    const [tab, setTab] = useState<"realized" | "evaluated">("realized");
    const [isLoading, setIsLoading] = useState(true);
    const [analyticsData, setAnalyticsData] = useState<ProfitRateResponse | null>(null);
    const [userId, setUserId] = useState<number | null>(null);

    // 1. 사용자 ID 조회
    useEffect(() => {
        const fetchUser = async () => {
            const response = await apiCall<{
                result: {
                    id: number;
                    userLoginId: string;
                    username: string;
                };
                message?: string;
            }>('/v1/users/me');

            if (response?.result?.id) {
                const currentUserId = response.result.id;
                setUserId(currentUserId);
                console.log('현재 사용자 ID:', currentUserId);
            } else {
                console.warn('사용자 정보를 가져오지 못했습니다:', response);
            }
        };

        fetchUser();
    }, []);

    // 2. userId가 세팅되면 실현 수익률 불러오기
    useEffect(() => {
        if (userId !== null) {
            fetchAnalyticsRealized(userId);
        }
    }, [userId]);

    // 실현 수익률 API 호출
    const fetchAnalyticsRealized = async (userId: number) => {
        try {
            setIsLoading(true);
            const response = await analyticsApi.getUserAnalyticsRealized(userId);
            console.log("실현 수익률 응답:", response);
            if (response) {
                // setAnalyticsData(response);
            }
        } catch (error) {
            console.error("실현 수익률 조회 실패:", error);
        } finally {
            setIsLoading(false);
        }
    };

    // 평가 수익률 API 호출
    const fetchAnalyticsEvaluated = async () => {
        if (userId === null) return;

        try {
            setIsLoading(true);
            const response = await analyticsApi.getUserAnalyticsUnrealized(userId);
            console.log("평가 수익률 응답:", response);
            if (response) {
                setAnalyticsData(response as any);
            }
        } catch (error) {
            console.error("평가 수익률 조회 실패:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleTabClick = (selectedTab: "realized" | "evaluated") => {
        setTab(selectedTab);
        if (selectedTab === "realized") {
            if (userId !== null) fetchAnalyticsRealized(userId);
        } else {
            fetchAnalyticsEvaluated();
        }
    };

    return (
        <PageShell
            maxW="max-w-[80vw]"
            padded
            innerClassName={cn("min-h-[60vh] flex flex-col items-center justify-center text-center space-y-6")}
        >
            <motion.div
                className="container py-8 space-y-6"
                variants={stagger(0.1)}
                initial="hidden"
                animate="show"
                suppressHydrationWarning
            >
                <motion.h1 variants={fadeInUp} className="text-2xl font-bold w-full text-left" suppressHydrationWarning>
                    분석 페이지
                </motion.h1>

                <motion.div
                    key={tab}
                    variants={fadeInUp}
                    initial="hidden"
                    animate="show"
                    suppressHydrationWarning
                >
                    {/* 탭 메뉴 */}
                    <div className="flex gap-6 text-sm font-medium">
                        <button
                            className={`border-b-2 pb-1 ${
                                tab === "realized" ? "border-black" : "border-transparent text-muted-foreground"
                            }`}
                            onClick={() => handleTabClick("realized")}
                        >
                            실현 수익률
                        </button>
                        <button
                            className={`border-b-2 pb-1 ${
                                tab === "evaluated" ? "border-black" : "border-transparent text-muted-foreground"
                            }`}
                            onClick={() => handleTabClick("evaluated")}
                        >
                            평가 수익률
                        </button>
                    </div>

                    {/* 수익 카드 영역 */}
                    <div className="flex gap-4 mt-6">
                        <Card className="flex-1 bg-[#eef0fe]">
                            <CardContent className="py-6">
                                <div className="text-sm text-muted-foreground">총 자산 대비 수익률</div>
                                <div className="text-3xl font-bold mt-1">
                                    {analyticsData?.profitRateOnTotalAssets ?? "0"}
                                </div>
                                <div
                                    className={cn(
                                        "text-sm font-medium flex items-center gap-1",
                                        (analyticsData?.profitRateOnTotalAssets ?? 0) >= 0
                                            ? "text-green-600"
                                            : "text-red-500"
                                    )}
                                >
                                    {(analyticsData?.profitRateOnTotalAssets ?? 0) >= 0 ? (
                                        <ArrowUpRight size={16} />
                                    ) : (
                                        <ArrowDownRight size={16} />
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                        <Card className="flex-1 bg-[#e6f1fb]">
                            <CardContent className="py-6">
                                <div className="text-sm text-muted-foreground">투자금 대비 수익률</div>
                                <div className="text-3xl font-bold mt-1">
                                    {analyticsData?.profitRateOnInvestment ?? "0"}
                                </div>
                                <div
                                    className={cn(
                                        "text-sm font-medium flex items-center gap-1",
                                        (analyticsData?.profitRateOnTotalAssets ?? 0) >= 0
                                            ? "text-green-600"
                                            : "text-red-500"
                                    )}
                                >
                                    {(analyticsData?.profitRateOnTotalAssets ?? 0) >= 0 ? (
                                        <ArrowUpRight size={16} />
                                    ) : (
                                        <ArrowDownRight size={16} />
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    {/* 테이블 */}
                    <div className="border rounded-xl overflow-hidden shadow-sm mt-6">
                        {analyticsData && (
                            <DataTable
                                columns={columns}
                                data={analyticsData.coinAnalytics ?? []}
                            />
                        )}
                    </div>
                </motion.div>
            </motion.div>
        </PageShell>
    );
}
