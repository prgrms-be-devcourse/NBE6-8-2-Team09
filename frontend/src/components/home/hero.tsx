"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { useState, useEffect, useCallback, useRef } from "react";
import { cn } from "@/lib/utils";
import { PageShell } from "@/components/layout/page-shell";

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

/** 안전한 API 베이스 선택 */
function resolveApiBase(): string {
    // 클라이언트일 때
    if (typeof window !== "undefined") {
        // 프로덕션: 동일 도메인 프록시 사용 권장 (/api)
        if (location.protocol === "https:") return "/api";
        // 개발: 환경변수 우선, 없으면 로컬 백엔드
        return process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    }
    // 서버 측 렌더링 시점에선 빌드타임 주입값만 접근 가능
    return process.env.NEXT_PUBLIC_API_URL || "";
}

export function Hero({
                         title = "Back9 Coin",
                         subtitle = "투자의 기준을 바꾸다. 실시간 지갑, 거래 내역, 관리자 전용 코인 등록까지.",
                         primaryCta = { href: "/exchange", label: "거래소 보러가기" },
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

        const controller = new AbortController();
        try {
            const base = resolveApiBase();
            const url =
                base
                    ? `${base.replace(/\/$/, "")}/api/v1/users/me`
                    : `/api/v1/users/me`; // base가 비어도 프록시 경로 시도

            const res = await fetch(url, {
                method: "GET",
                credentials: "include",
                cache: "no-store",
                signal: controller.signal,
            });

            setIsLoggedIn(res.ok);
        } catch (e) {
            setIsLoggedIn(false);
        } finally {
            fetchingRef.current = false;
            controller.abort();
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
