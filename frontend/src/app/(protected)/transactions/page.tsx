"use client";
import { useEffect } from "react";
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
    useEffect(() => {
        const hasToken = document.cookie.includes("access_token");
        if (!hasToken) {
            router.replace("/login");
        }
    }, [router]);
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
