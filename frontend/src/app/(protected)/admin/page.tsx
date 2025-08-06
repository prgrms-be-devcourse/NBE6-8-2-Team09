"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiCall } from "@/lib/api/client";

export default function AdminPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const checkAdminAccess = async () => {
            try {
                // /me API 호출로 사용자 정보 확인
                const response = await apiCall<{
                    result: {
                        id: number;
                        userLoginId: string;
                        username: string;
                        role?: string;
                    };
                    message?: string;
                }>("/v1/users/me");
                const user = response?.result;

                // 관리자 권한 확인 (백엔드에서 role 확인)
                if (!user || user.role !== 'ADMIN') {
                    router.replace("/login");
                    return;
                }
                setIsLoading(false);
            } catch (error) {
                // 인증 실패 시 로그인 페이지로
                router.replace("/login");
            }
        };

        checkAdminAccess();
    }, [router]);

    if (isLoading) {
        return <div>로딩 중...</div>;
    }

    return <div>관리자 전용 페이지입니다.</div>;
}
