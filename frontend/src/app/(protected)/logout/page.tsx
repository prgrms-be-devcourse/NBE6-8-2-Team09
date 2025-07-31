"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LogoutPage() {
    const router = useRouter();

    useEffect(() => {
        const logout = async () => {
            try {
                // DELETE 메서드로 변경
                await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"}/api/v1/users/logout`,
                    {
                        method: "DELETE", // POST에서 DELETE로 변경
                        credentials: "include",
                    }
                );
                console.log('로그아웃 성공');
            } catch (error) {
                console.warn('백엔드 로그아웃 실패:', error);
            } finally {
                // 백엔드에서 쿠키를 삭제하므로 바로 리다이렉트
                setTimeout(() => {
                    router.replace("/login");
                }, 500);
            }
        };

        logout();
    }, [router]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-center">
                <p className="text-lg">로그아웃 중...</p>
            </div>
        </div>
    );
}