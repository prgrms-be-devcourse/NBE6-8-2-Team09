"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import {format, formatDate} from "date-fns";
import { ko } from "date-fns/locale";
import { CalendarIcon, RotateCcw, AlertCircle } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { DataTable } from "@/components/ui/data-table";
import { ColumnDef } from "@tanstack/react-table";
import { cn } from "@/lib/utils";
import { PageShell } from "@/components/layout/page-shell";
import { tradeLogApi } from "@/lib/api/tradelog";
import { apiCall } from "@/lib/api/client";
import { walletApi } from "@/lib/api/wallet";
import type { TradeLogResponse } from "@/lib/types/tradelog";

const columns: ColumnDef<TradeLogResponse>[] = [
    {
        accessorKey: "date",
        header: () => <div className="text-center">거래날짜</div>,
        cell: ({ getValue }) => <div className="text-center">{getValue() as string}</div>,
    },
    {
        accessorKey: "coinSymbol",
        header: () => <div className="text-center">가상화폐 이름</div>,
        cell: ({ getValue }) => <div className="text-center">{getValue() as string}</div>,
    },
    {
        accessorKey: "tradeType",
        header: () => <div className="text-center">거래 구분</div>,
        cell: ({ getValue }) => <div className="text-center">{getValue() as string}</div>,
    },
    {
        accessorKey: "price",
        header: () => <div className="text-center">구매/판매 금액</div>,
        cell: ({ getValue }) => <div className="text-center">$ {Number(getValue()).toLocaleString()}</div>,
    },
    {
        accessorKey: "quantity",
        header: () => <div className="text-center">구매/판매 수</div>,
        cell: ({ getValue }) => <div className="text-center">{getValue() as string}</div>,
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

interface FilterState {
    startDate: Date | undefined;
    endDate: Date | undefined;
    transactionType: string;
}

export default function TransactionsPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [filters, setFilters] = useState<FilterState>({
        startDate: undefined,
        endDate: undefined,
        transactionType: "전체",
    });
    const [tradeLogData, setTradeLogData] = useState<TradeLogResponse[]>([]);
    const [dateError, setDateError] = useState("");
    const [isStartOpen, setIsStartOpen] = useState(false);
    const [isEndOpen, setIsEndOpen] = useState(false);
    const [userId, setUserId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const checkAuthAndFetchData = async () => {
            try {
                // API 클라이언트를 사용하여 일관된 URL과 설정으로 인증 확인
                const response = await apiCall<{
                    result: {
                        id: number;
                        userLoginId: string;
                        username: string;
                    };
                    message?: string;
                }>('/v1/users/me');
                if (response && (response as any).result?.id) {
                    const currentUserId = (response as any).result.id;
                    setIsAuthenticated(true);
                    setUserId(currentUserId);
                    console.log('현재 사용자 ID:', currentUserId);

                    // userId로 직접 거래 내역 조회 (지갑 조회 과정 생략)
                    await fetchTradeLogWithUserId(currentUserId);
                } else {
                    router.replace("/login");
                    return;
                }
            } catch (error) {
                console.error("인증 확인 실패:", error);
                router.replace("/login");
                return;
            } finally {
                setIsLoading(false);
            }
        };

        checkAuthAndFetchData();
    }, [router]);

    // userId로 직접 거래 내역 조회하는 함수
    const fetchTradeLogWithUserId = async (userId: number) => {
        try {
            setIsLoading(true);
            console.log('거래 내역 조회 시작 - userId:', userId);
            const response = await tradeLogApi.getUserTradeLogs(userId);
            console.log("거래 내역 응답:", response);

            if (response !== null) {
                // @ts-ignore
                setTradeLogData(response);
            } else {
                console.warn("거래 내역이 없습니다.");
            }
        } catch (error) {
            console.error("거래 내역 조회 실패:", error);
            setError("거래 내역을 불러오는데 실패했습니다: " + (error as any)?.message);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchTradeLog = async () => {
        if (!userId) return;
        await fetchTradeLogWithUserId(userId);
    };

    const handleFilterChange = (key: keyof FilterState, value: any) => {
        const newFilters = { ...filters, [key]: value };
        setFilters(newFilters);
        const startDate = key === "startDate" ? value : newFilters.startDate;
        const endDate = key === "endDate" ? value : newFilters.endDate;

        if (startDate && !endDate) setDateError("종료일을 선택해주세요.");
        else if (!startDate && endDate) setDateError("시작일을 선택해주세요.");
        else if (startDate && endDate && startDate > endDate)
            setDateError("시작일은 종료일보다 이전이어야 합니다.");
        else setDateError("");
    };

    const handleClear = () => {
        setFilters({
            startDate: undefined,
            endDate: undefined,
            transactionType: "전체",
        });
        setDateError("");
    };

    const handleFilter = async () => {
        if (!userId) return;
        
        const params: Record<string, any> = {};

        if (filters.startDate) {
            params.startDate = formatDate(filters.startDate, 'yyyy-MM-dd');
        }
        if (filters.endDate) {
            params.endDate = formatDate(filters.endDate, 'yyyy-MM-dd');
        }

        // 거래 유형이 존재할 경우만 추가
        if (filters.transactionType === '매수') {
            params.type = 'BUY';
        } else if (filters.transactionType === '매도') {
            params.type = 'SELL';
        } else if (filters.transactionType === '충전') {
            params.type = 'CHARGE';
        }


        try {
            const response = await tradeLogApi.getFilteredTradeLogs(userId, params);
            // @ts-ignore
            setTradeLogData(response);
        } catch (error) {
            console.error("필터 적용 중 오류 발생:", error);
        }
    };

    if (isLoading) {
        return (
            <div className="container py-8 flex items-center justify-center">
                <p>로딩 중...</p>
            </div>
        );
    }

    if (!isAuthenticated) {
        return null;
    }

    return (
        <PageShell
            maxW="max-w-[80vw]"
            padded
            innerClassName={cn("min-h-[60vh] flex flex-col items-center justify-center text-center space-y-6")}
        >
            <motion.div className="container py-8 space-y-6" variants={stagger(0.1)} initial="hidden" animate="show" suppressHydrationWarning>
                <motion.h1 variants={fadeInUp} className="text-2xl font-bold w-full text-left" suppressHydrationWarning>
                    가상화폐 주문 내역 페이지
                </motion.h1>

                {/* 필터 */}
                <motion.div variants={fadeInUp} suppressHydrationWarning>
                    <div className="bg-white p-4 rounded-lg border shadow-sm mb-6">
                        <div className="flex flex-wrap items-center justify-around gap-4">
                            {/* 날짜 선택 */}
                            <div className="flex items-center gap-2">
                                <span className="text-sm font-medium text-gray-700">기간 선택</span>
                                <Popover open={isStartOpen} onOpenChange={setIsStartOpen}>
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" className={`w-40 justify-start ${dateError ? "border-red-500" : ""}`}>
                                            <CalendarIcon className="mr-2 h-4 w-4" />
                                            {filters.startDate ? format(filters.startDate, "yyyy.MM.dd", { locale: ko }) : "시작일 선택"}
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0">
                                        <Calendar
                                            mode="single"
                                            selected={filters.startDate}
                                            onSelect={(date) => {
                                                setIsStartOpen(false);
                                                handleFilterChange("startDate", date);
                                            }}
                                            initialFocus
                                        />
                                    </PopoverContent>
                                </Popover>
                                <span className="text-gray-500">~</span>
                                <Popover open={isEndOpen} onOpenChange={setIsEndOpen}>
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" className={`w-40 justify-start ${dateError ? "border-red-500" : ""}`}>
                                            <CalendarIcon className="mr-2 h-4 w-4" />
                                            {filters.endDate ? format(filters.endDate, "yyyy.MM.dd", { locale: ko }) : "종료일 선택"}
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0">
                                        <Calendar
                                            mode="single"
                                            selected={filters.endDate}
                                            onSelect={(date) => {
                                                setIsEndOpen(false);
                                                handleFilterChange("endDate", date);
                                            }}
                                            initialFocus
                                        />
                                    </PopoverContent>
                                </Popover>
                            </div>

                            {/* 거래 구분 */}
                            <div className="flex items-center gap-2">
                                <span className="text-sm font-medium text-gray-700">거래구분 선택</span>
                                <Select
                                    value={filters.transactionType}
                                    onValueChange={(value) => handleFilterChange("transactionType", value)}
                                >
                                    <SelectTrigger className="w-32">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="전체">전체</SelectItem>
                                        <SelectItem value="매수">매수</SelectItem>
                                        <SelectItem value="매도">매도</SelectItem>
                                        <SelectItem value="충전">충전</SelectItem>

                                    </SelectContent>
                                </Select>
                            </div>

                            {/* 버튼 */}
                            <div className="flex items-center gap-2 justify-end">
                                <Button variant="outline" size="sm" onClick={handleClear} className="flex items-center gap-2">
                                    <RotateCcw className="h-4 w-4" />
                                    초기화
                                </Button>
                                <Button
                                    size="sm"
                                    onClick={handleFilter}
                                    className="bg-blue-600 hover:bg-blue-700"
                                    disabled={!!dateError}
                                >
                                    적용
                                </Button>
                            </div>
                        </div>

                        {dateError && (
                            <div className="mt-3 flex items-center gap-2 text-red-600 text-sm">
                                <AlertCircle className="h-4 w-4" />
                                {dateError}
                            </div>
                        )}
                    </div>
                </motion.div>

                {/* 거래 내역 테이블 */}
                <motion.div variants={fadeInUp}>
                    <DataTable columns={columns} data={tradeLogData} pageSize={10} />
                </motion.div>
            </motion.div>
            {/*<Button*/}
            {/*    size="sm"*/}
            {/*    className="bg-green-600 hover:bg-green-700"*/}
            {/*    onClick={async () => {*/}
            {/*        try {*/}
            {/*            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/tradeLog/mock`, {*/}
            {/*                method: "POST",*/}
            {/*                credentials : "include",*/}
            {/*            });*/}
            {/*            if (res.ok) {*/}
            {/*                alert("거래 내역 15개 생성 완료!");*/}
            {/*                fetchTradeLog(); // 다시 불러오기*/}
            {/*            } else {*/}
            {/*                alert("생성 실패");*/}
            {/*            }*/}
            {/*        } catch (err) {*/}
            {/*            console.error("Mock 생성 실패:", err);*/}
            {/*            alert("에러 발생");*/}
            {/*        }*/}
            {/*    }}*/}
            {/*>*/}
            {/*    거래내역 생성*/}
            {/*</Button>*/}
        </PageShell>
    );
}