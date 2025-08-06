"use client";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiCall } from "@/lib/api/client"; // apiCall import 추가

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

const stagger = (delay = 0.1) => ({
    hidden: {},
    show: { transition: { staggerChildren: delay } },
});

type MeResponse = {
    result: {
        id: number;
        userLoginId: string;
        username: string;
        // 필요한 필드만 추가
    };
};

export default function DashboardPage() {
    const [userInfo, setUserInfo] = useState<MeResponse["result"] | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const ctrl = new AbortController();

        const loadMe = async () => {
            try {
                console.log("=== 대시보드: /v1/users/me 호출 ===");

                // fetch 대신 apiCall 사용
                const response = await apiCall<{
                    result: {
                        id: number;
                        userLoginId: string;
                        username: string;
                    };
                    message?: string;
                }>("/v1/users/me");
                console.log("API 응답:", response);

                if (response && response.result) {
                    setUserInfo(response.result);
                } else {
                    console.error("잘못된 응답 형태:", response);
                    router.replace("/login");
                }
            } catch (e) {
                // AbortError는 정상적인 취소이므로 무시
                if (e instanceof Error && e.name === 'AbortError') {
                    console.log("API 요청이 취소되었습니다 (정상)");
                    return;
                }
                console.error("me 호출 중 오류:", e);
                if (!ctrl.signal.aborted) {
                    router.replace("/login");
                }
            } finally {
                if (!ctrl.signal.aborted) {
                    setIsLoading(false);
                }
            }
        };

        loadMe();
        return () => ctrl.abort();
    }, [router]);

    if (isLoading) {
        return (
            <div className="container py-8 flex items-center justify-center">
                <div>
                    <p>로딩 중...</p>
                    <p className="text-sm text-gray-500 mt-2">인증 상태를 확인하고 있습니다...</p>
                </div>
            </div>
        );
    }

    if (!userInfo) {
        return (
            <div className="container py-8 flex items-center justify-center">
                <div className="text-center">
                    <p>인증이 필요합니다.</p>
                    <p className="text-sm text-gray-500 mt-2">잠시 후 로그인 페이지로 이동합니다...</p>
                </div>
            </div>
        );
    }

    return (
        <motion.div
            className="container py-8"
            variants={stagger(0.1)}
            initial="hidden"
            animate="show"
            suppressHydrationWarning
        >
            <motion.h1 variants={fadeInUp} className="text-2xl font-bold mb-4" suppressHydrationWarning>
                Dashboard
            </motion.h1>

            <motion.div variants={fadeInUp} className="mb-6" suppressHydrationWarning>
                <p className="text-lg">안녕하세요, {userInfo.username}님!</p>
                <p className="text-gray-600">{userInfo.userLoginId}</p>
            </motion.div>
        </motion.div>
    );
}
