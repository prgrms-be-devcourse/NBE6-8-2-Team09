"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LogoutPage() {
    const router = useRouter();

    useEffect(() => {
        const logout = async () => {
            try {
                // 백엔드 로그아웃 (accessToken, apiKey 삭제)
                await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"}/api/v1/users/logout`,
                    {
                        method: "DELETE",
                        credentials: "include",
                    }
                );
            } catch (error) {
                console.warn('백엔드 로그아웃 실패:', error);
            } finally {
                // 프론트엔드가 만든 중복 쿠키들 삭제
                document.cookie = "access_Token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
                document.cookie = "role=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
                document.cookie = "apiKey=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
                
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