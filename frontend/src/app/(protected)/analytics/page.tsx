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

const columns: ColumnDef<ProfitAnalysisDto>[] = [
    { accessorKey: "coinId", header: "코인 ID" },
    {
        accessorKey: "totalQuantity",
        header: "보유 수량",
        cell: ({ row }) => `${row.getValue("totalQuantity") as number} 주`
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

    const userId = 1;

    useEffect(() => {
        fetchAnalyticsRealized();
    }, []);

    const fetchAnalyticsRealized = async () => {
        try {
            setIsLoading(true);
            const response = await analyticsApi.getUserAnalyticsRealized(userId);
            console.log("거래 내역 응답:", response);
            if (response) {
                // @ts-ignore
                setAnalyticsData(response);
            }
        } catch (error) {
            console.error("거래 내역 조회 실패:", error);
        } finally {
            setIsLoading(false);
        }
    };
    const handleTabClick = (selectedTab: "realized" | "evaluated") => {
        setTab(selectedTab);
        if (selectedTab === "realized") {
            fetchAnalyticsRealized();
        } else {
            fetchAnalyticsEvaluated(); // 이 함수도 따로 구현 필요
        }
    };
    const fetchAnalyticsEvaluated = async () => {
        try {
            setIsLoading(true);
            const response = await analyticsApi.getUserAnalyticsUnrealized(userId);
            console.log("거래 내역 응답:", response);
            if (response) {
                // @ts-ignore
                setAnalyticsData(response);
            }
        } catch (error) {
            console.error("거래 내역 조회 실패:", error);
        } finally {
            setIsLoading(false);
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
            >
                <motion.h1 variants={fadeInUp} className="text-2xl font-bold w-full text-left">
                    분석 페이지
                </motion.h1>

                <motion.div
                    key={tab}
                    variants={fadeInUp}
                    initial="hidden"
                    animate="show"
                    exit="hidden"
                >                    {/* 탭 메뉴 */}
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
