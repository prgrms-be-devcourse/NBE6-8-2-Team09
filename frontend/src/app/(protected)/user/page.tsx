"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { fadeInUp } from "@/lib/motion";
import { apiCall } from "@/lib/api/client";

type UserInfo = {
    userLoginId: string;
    username: string;
    role?: string;
    id?: number;
};

export default function MyPage() {
    const router = useRouter();
    const [user, setUser] = useState<UserInfo | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchMyInfo = async () => {
            try {
                const data = await apiCall(`/v1/users/me`);

                if (data && data.result) {
                    setUser(data.result);
                } else {
                    setError(data.message || "유저 정보를 불러올 수 없습니다.");
                }
            } catch (error: any) {
                console.error("유저 정보 조회 실패:", error);
                if (error.status === 401) {
                    router.replace("/login");
                } else {
                    setError(error.message || "유저 정보를 불러올 수 없습니다.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchMyInfo();
    }, [router]);

    return (
        <div className="min-h-[calc(100vh-64px-260px)] flex items-center justify-center">
            <motion.div
                variants={fadeInUp}
                initial="hidden"
                animate="show"
                className="w-full max-w-sm border rounded-lg p-6 bg-card shadow"
            >
                <h1 className="text-2xl font-bold mb-6 text-center">내 정보</h1>
                {loading ? (
                    <div className="text-center text-muted-foreground">불러오는 중...</div>
                ) : error ? (
                    <p className="text-sm text-red-500 text-center">{error}</p>
                ) : user ? (
                    <form className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="userLoginId">아이디</Label>
                            <Input id="userLoginId" type="text" value={user.userLoginId} disabled />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="username">닉네임</Label>
                            <Input id="username" type="text" value={user.username} disabled />
                        </div>
                        {user.role && (
                            <div className="space-y-2">
                                <Label htmlFor="role">권한</Label>
                                <Input id="role" type="text" value={user.role} disabled />
                            </div>
                        )}
                        {user.id && (
                            <div className="space-y-2">
                                <Label htmlFor="id">회원번호</Label>
                                <Input id="id" type="text" value={user.id} disabled />
                            </div>
                        )}
                        <Button
                            type="button"
                            className="w-full"
                            variant="outline"
                            onClick={() => router.push("/dashboard")}
                        >
                            대시보드로 이동
                        </Button>
                    </form>
                ) : (
                    <p className="text-sm text-muted-foreground text-center">정보 없음</p>
                )}

                <div className="mt-6 text-center">
                    <p className="text-sm text-muted-foreground">
                        <Button
                            variant="link"
                            className="p-0 h-auto font-normal text-primary"
                            onClick={() => router.push('/logout')}
                        >
                            로그아웃
                        </Button>
                    </p>
                </div>
            </motion.div>
        </div>
    );
}
