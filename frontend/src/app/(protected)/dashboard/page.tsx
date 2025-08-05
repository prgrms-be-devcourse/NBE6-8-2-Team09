"use client";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

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
    const [userInfo, setUserInfo] = useState<any>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const checkAuthAndLoadUser = async () => {
            try {
                console.log('=== 대시보드 인증 확인 시작 ===');

                // 먼저 쿠키 확인
                const cookies = document.cookie.split(';');
                const accessTokenCookie = cookies.find(cookie => cookie.trim().startsWith('accessToken='));
                const apiKeyCookie = cookies.find(cookie => cookie.trim().startsWith('apiKey='));

                console.log('=== 대시보드 쿠키 확인 ===');
                console.log('accessToken 쿠키:', accessTokenCookie ? '존재' : '없음');
                console.log('apiKey 쿠키:', apiKeyCookie ? '존재' : '없음');

                // 쿠키가 없으면 한 번만 로그인 페이지로 이동
                if (!accessTokenCookie || !apiKeyCookie) {
                    console.log('인증 쿠키 없음 - 로그인 페이지로 이동');
                    setIsLoading(false);
                    // 즉시 리다이렉트하지 말고 사용자에게 선택권 제공
                    setTimeout(() => {
                        if (window.location.pathname === '/dashboard') {
                            router.replace('/login');
                        }
                    }, 2000);
                    return;
                }

                // 쿠키가 있으면 API 호출
                console.log('쿠키 존재 - 사용자 정보 API 호출');

                const response = await fetch('/api/v1/users/me', {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                console.log('사용자 정보 API 응답 상태:', response.status);

                if (response.ok) {
                    const data = await response.json();
                    console.log('사용자 정보 로드 성공');
                    setUserInfo(data.result);
                } else {
                    console.log('사용자 정보 로드 실패 - 로그인 페이지로 이동');
                    setTimeout(() => {
                        router.replace('/login');
                    }, 1000);
                }
            } catch (error) {
                console.error('대시보드 로드 중 오류:', error);
                setTimeout(() => {
                    router.replace('/login');
                }, 1000);
            } finally {
                setIsLoading(false);
            }
        };

        checkAuthAndLoadUser();
    }, []); // router 의존성 제거 - 무한 루프 방지

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

    // 쿠키가 없을 때 표시할 메시지
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
        >
            <motion.h1
                variants={fadeInUp}
                className="text-2xl font-bold mb-4"
            >
                Dashboard
            </motion.h1>

            {userInfo && (
                <motion.div variants={fadeInUp} className="mb-6">
                    <p className="text-lg">안녕하세요, {userInfo.username}님!</p>
                    <p className="text-gray-600">{userInfo.userLoginId}</p>
                </motion.div>
            )}

            {/* ...existing dashboard content... */}
        </motion.div>
    );
}
