"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiCall } from "@/lib/api/client"; // apiCall import 추가

export default function LogoutPage() {
    const router = useRouter();

    useEffect(() => {
        const logout = async () => {
            try {
                // fetch 대신 apiCall 사용
                await apiCall("/v1/users/logout", {
                    method: "DELETE",
                });
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
