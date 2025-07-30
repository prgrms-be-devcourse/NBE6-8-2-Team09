"use client";
import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LogoutPage() {
    const router = useRouter();
    useEffect(() => {
        document.cookie = "access_token=; Max-Age=0; path=/";
        document.cookie = "role=; Max-Age=0; path=/";
        router.replace("/login");
    }, [router]);
    return <div>로그아웃 중...</div>;
} 