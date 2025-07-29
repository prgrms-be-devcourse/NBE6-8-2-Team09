"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";
import { PageShell } from "@/components/layout/page-shell";

type HeroProps = {
    title?: string;
    subtitle?: string;
    primaryCta?: { href: string; label: string };
    secondaryCta?: { href: string; label: string };
    className?: string;        // <section>에 적용
    innerClassName?: string;   // PageShell 내부 래퍼에 적용
};

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

const stagger = (delay = 0.08) => ({
    hidden: {},
    show: {
        transition: {
            staggerChildren: delay,
        },
    },
});

export function Hero({
                         title = "Back9 Coin",
                         subtitle = "투자의 기준을 바꾸다. 실시간 지갑, 거래 내역, 관리자 전용 코인 등록까지.",
                         primaryCta = { href: "/dashboard", label: "대시보드 보러가기" },
                         secondaryCta = { href: "/register", label: "회원가입" },
                         className,
                         innerClassName,
                     }: HeroProps) {
    return (
        <section
            className={cn("relative overflow-hidden w-full", className)}
        >
            {/* full-bleed 배경 */}
            <div className="pointer-events-none absolute inset-0 -z-10 bg-gradient-to-b from-blue-50 to-white" />

            {/* 가운데 정렬 & max-width는 PageShell로 통일 */}
            <PageShell
                maxW="max-w-[80vw]"
                padded
                innerClassName={cn(
                    "min-h-[60vh] flex flex-col items-center justify-center text-center space-y-6",
                    innerClassName
                )}
            >
                <motion.div variants={stagger(0.1)} initial="hidden" animate="show">
                    <motion.h1
                        variants={fadeInUp}
                        className="text-4xl md:text-5xl font-bold text-blue-600"
                    >
                        {title}
                    </motion.h1>

                    <motion.p
                        variants={fadeInUp}
                        className="text-muted-foreground max-w-xl mx-auto mt-4"
                    >
                        {subtitle}
                    </motion.p>

                    <motion.div
                        variants={fadeInUp}
                        className="mt-6 flex justify-center gap-3"
                    >
                        <Link
                            href={primaryCta.href}
                            className="inline-flex items-center px-4 py-2 rounded-md bg-blue-600 text-white transition
                         hover:opacity-90 hover:scale-[1.02] active:scale-[0.99]"
                        >
                            {primaryCta.label}
                        </Link>
                        <Link
                            href={secondaryCta.href}
                            className="inline-flex items-center px-4 py-2 rounded-md border transition
                         hover:bg-gray-50 hover:scale-[1.02] active:scale-[0.99]"
                        >
                            {secondaryCta.label}
                        </Link>
                    </motion.div>
                </motion.div>
            </PageShell>
        </section>
    );
}
