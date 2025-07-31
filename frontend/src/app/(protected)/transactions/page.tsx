"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { DataTable } from "@/components/transactions/data-table";
import { columns, Transaction } from "@/components/transactions/columns";

const mock: Transaction[] = [
	{ date: "2025.07.24", name: "Invest reduction", type: "매수", amount: 2000, buySellAmount: 2000, qty: 0.0001 },
	{ date: "2025.07.24", name: "Terms extension", type: "매도", amount: 2500, buySellAmount: 2500, qty: 1 },
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

export default function TransactionsPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(true); // 추가
    const [isAuthenticated, setIsAuthenticated] = useState(false); // 추가

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const response = await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"}/api/v1/users/me`,
                    {
                        method: "GET",
                        credentials: "include",
                    }
                );
                
                if (response.ok) {
                    setIsAuthenticated(true);
                } else {
                    router.replace("/login");
                    return;
                }
            } catch (error) {
                console.error('인증 확인 실패:', error);
                router.replace("/login");
                return;
            } finally {
                setIsLoading(false);
            }
        };

        checkAuth();
    }, [router]);

    // 로딩 중일 때
    if (isLoading) {
        return (
            <div className="container py-8 flex items-center justify-center">
                <p>로딩 중...</p>
            </div>
        );
    }

    // 인증되지 않았을 때 (리다이렉트 중)
    if (!isAuthenticated) {
        return null;
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
				거래내역
			</motion.h1>
			<motion.div variants={fadeInUp}>
				<DataTable columns={columns} data={mock} pageSize={10} />
			</motion.div>
		</motion.div>
	);
}
