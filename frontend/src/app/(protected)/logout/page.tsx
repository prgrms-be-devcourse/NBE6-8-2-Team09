"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api/auth"; // authApi import로 변경

export default function LogoutPage() {
    const router = useRouter();

    useEffect(() => {
        const logout = async () => {
            try {
                // authApi.logout 사용 (일관성 있는 API 호출)
                await authApi.logout();
                console.log('백엔드 로그아웃 성공');
            } catch (error) {
                console.warn('백엔드 로그아웃 실패:', error);
            } finally {
                // 백엔드에서 쿠키 삭제를 처리하므로 프론트엔드에서는 바로 리다이렉트
                setTimeout(() => {
                    router.replace("/login");
                }, 500);
            }
        };

        logout();
    }, [router]);

    return (
        <div className="container py-8 flex items-center justify-center">
            <div className="text-center">
                <p>로그아웃 중...</p>
                <p className="text-sm text-gray-500 mt-2">잠시만 기다려주세요.</p>
            </div>
        </div>
    );
}
