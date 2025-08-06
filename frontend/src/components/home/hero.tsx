"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { useState, useEffect, useCallback, useRef } from "react";
import { cn } from "@/lib/utils";
import { PageShell } from "@/components/layout/page-shell";
import { apiCall } from "@/lib/api/client"; // apiCall import 추가

type HeroProps = {
    title?: string;
    subtitle?: string;
    primaryCta?: { href: string; label: string };
    secondaryCta?: { href: string; label: string };
    className?: string;
    innerClassName?: string;
};

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

const stagger = (delay = 0.08) => ({
    hidden: {},
    show: { transition: { staggerChildren: delay } },
});

export function Hero({
                         title = "Back9 Coin",
                         subtitle = "투자의 기준을 바꾸다. 실시간 지갑, 거래 내역, 관리자 전용 코인 등록까지.",
                         primaryCta = { href: "/dashboard", label: "대시보드 보러가기" },
                         secondaryCta = { href: "/register", label: "회원가입" },
                         className,
                         innerClassName,
                     }: HeroProps) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [mounted, setMounted] = useState(false); // 하이드레이션 가드
    const fetchingRef = useRef(false);             // 중복 호출 방지

    const checkLoginStatus = useCallback(async () => {
        if (fetchingRef.current) return;
        fetchingRef.current = true;

        try {
            // apiCall 함수를 사용하여 일관된 API 호출
            await apiCall('/v1/users/me');
            setIsLoggedIn(true);
        } catch (e) {
            setIsLoggedIn(false);
        } finally {
            fetchingRef.current = false;
        }
    }, []);

    useEffect(() => {
        setMounted(true); // 클라이언트에 붙은 뒤에만 조건부 렌더
        checkLoginStatus();

        const onFocus = () => void checkLoginStatus();
        window.addEventListener("focus", onFocus);
        return () => window.removeEventListener("focus", onFocus);
    }, [checkLoginStatus]);

    return (
        <section className={cn("relative overflow-hidden w-full", className)}>
            <div className="pointer-events-none absolute inset-0 -z-10 bg-gradient-to-b from-orange-50 to-white" />

            <PageShell
                maxW="max-w-[80vw]"
                padded
                innerClassName={cn(
                    "min-h-[60vh] flex flex-col items-center justify-center text-center space-y-6",
                    innerClassName
                )}
            >
                <motion.div variants={stagger(0.1)} initial="hidden" animate="show">
                    <motion.div variants={fadeInUp} className="flex items-center justify-center gap-4">
                        <img
                            src="/images/back9-coin-logo.PNG"
                            alt="BACK9 Coin Logo"
                            className="w-16 h-16 md:w-20 md:h-20 object-contain"
                        />
                        <h1 className="text-4xl md:text-5xl font-bold text-amber-600">{title}</h1>
                    </motion.div>

                    <motion.p variants={fadeInUp} className="text-muted-foreground max-w-xl mx-auto mt-4">
                        {subtitle}
                    </motion.p>

                    <motion.div variants={fadeInUp} className="mt-6 flex justify-center gap-3">
                        <Link
                            href={primaryCta.href}
                            className="inline-flex items-center px-4 py-2 rounded-md bg-amber-600 text-white transition
                         hover:opacity-90 hover:scale-[1.02] active:scale-[0.99]"
                        >
                            {primaryCta.label}
                        </Link>

                        {/* 하이드레이션 후에만 조건부 렌더링 */}
                        {mounted && !isLoggedIn && (
                            <Link
                                href={secondaryCta.href}
                                className="inline-flex items-center px-4 py-2 rounded-md border transition
                           hover:bg-gray-50 hover:scale-[1.02] active:scale-[0.99]"
                            >
                                {secondaryCta.label}
                            </Link>
                        )}
                    </motion.div>
                </motion.div>
            </PageShell>
        </section>
    );
}
