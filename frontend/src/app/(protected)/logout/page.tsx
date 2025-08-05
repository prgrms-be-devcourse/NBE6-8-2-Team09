"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api/auth";

export default function LogoutPage() {
    const router = useRouter();
    const [status, setStatus] = useState('시작 중...');

    useEffect(() => {
        const logout = async () => {
            try {
                setStatus('백엔드 로그아웃 호출 중...');
                console.log('로그아웃 API 호출 시작');

                // 백엔드에서 모든 로그아웃 처리 (쿠키 삭제 포함)
                const result = await authApi.logout();
                console.log('백엔드 로그아웃 응답:', result);
                setStatus('로그아웃 성공');

            } catch (error) {
                console.warn('백엔드 로그아웃 실패:', error);
                setStatus('로그아웃 실패 - 하지만 로그인 페이지로 이동');
            }

            setStatus('로그인 페이지로 이동 중...');

            // 백엔드 처리 후 페이지 이동 (쿠키 삭제는 백엔드에서 처리됨)
            setTimeout(() => {
                console.log('로그인 페이지로 리다이렉트');
                window.location.href = '/login';
            }, 1000);
        };

        logout();
    }, [router]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-center">
                <p className="text-lg">로그아웃 중...</p>
                <p className="text-sm text-gray-600 mt-2">{status}</p>
                <p className="text-xs text-gray-500 mt-1">
                    백엔드에서 모든 로그아웃 처리를 담당합니다
                </p>
            </div>
        </div>
    );
}
