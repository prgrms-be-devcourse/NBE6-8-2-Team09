"use client";
import { motion } from "framer-motion";

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

export default function DashboardPage() {
    return (
        <motion.div
            className="container py-8"
            variants={stagger(0.1)}
            initial="hidden"
            animate="show"
        >
            <motion.h1
                variants={fadeInUp}
                className="text-2xl font-bold mb-4"
            >
                Dashboard
            </motion.h1>
            <motion.p
                variants={fadeInUp}
                className="text-muted-foreground"
            >
                로그인 붙이기 전, 공개 상태의 대시보드 예시 페이지입니다.
            </motion.p>
        </motion.div>
    );
}
