"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LogoutPage() {
    const router = useRouter();

    useEffect(() => {
        const logout = async () => {
            try {
                // 백엔드 로그아웃 API 호출 (HttpOnly 쿠키 삭제는 백엔드에서 처리)
                await fetch(
                    `/api/v1/users/logout`,
                    {
                        method: "DELETE",
                        credentials: "include", // HttpOnly 쿠키 자동 전송
                    }
                );
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
