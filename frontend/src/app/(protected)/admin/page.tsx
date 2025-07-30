"use client";
import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function AdminPage() {
    const router = useRouter();
    useEffect(() => {
        const cookies = document.cookie.split(';').map(c => c.trim());
        const roleCookie = cookies.find(c => c.startsWith('role='));
        const role = roleCookie ? roleCookie.split('=')[1] : null;
        if (role !== 'ADMIN') {
            router.replace("/login");
        }
    }, [router]);
    return <div>관리자 전용 페이지입니다.</div>;
} 