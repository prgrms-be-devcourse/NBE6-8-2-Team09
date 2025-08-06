"use client";

import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { fadeInUp } from "@/lib/motion";
import { authApi } from "@/lib/api/auth";
import type { userRegisterDto } from '@/lib/types/user';

const schema = z.object({
    userLoginId: z.string().min(1, "아이디를 입력해주세요."),
    password: z.string().min(3, "비밀번호는 3자 이상"),
    confirmPassword: z.string().min(1, "비밀번호 확인을 입력해주세요."),
    username: z.string().min(1, "유저이름을 입력해주세요."),
});

type FormValues = z.infer<typeof schema>;

export default function RegisterPage() {
    const router = useRouter();
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    // 회원가입 에러 메시지 설정
    useEffect(() => {
        // URL 파라미터에서 에러 확인
        const searchParams = new URLSearchParams(window.location.search);
        const registerError = searchParams.get('error');

        if (registerError) {
            const errorMessages: { [key: string]: string } = {
                'duplicate_id': '이미 사용 중인 아이디입니다.',
                'duplicate_username': '이미 사용 중인 유저이름입니다.',
                'server_error': '서버 오류가 발생했습니다.',
                'validation_error': '입력 정보를 확인해주세요.'
            };
            setError(errorMessages[registerError] || '알 수 없는 오류가 발생했습니다.');
        }
    }, []);

    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            userLoginId: "",
            password: "",
            confirmPassword: "",
            username: ""
        },
    });
    //(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/users/register`
    const onRegister = async (values: any) => {
        setIsLoading(true);
        setError(null);

        try {
            // authApi.register 호출 - 정확한 타입 매핑
            const data = await authApi.register({
                userLoginId: values.userLoginId,
                password: values.password,
                confirmPassword: values.confirmPassword,
                username: values.username,
            });

            // 회원가입 성공 처리 - ApiResponse 구조에 맞게 수정
            if (data && data.data) {
                alert('회원가입이 완료되었습니다!');
                setTimeout(() => {
                    router.replace("/login?message=register_success");
                }, 500);
            } else if (data) {
                // 성공했지만 data가 없는 경우도 성공으로 처리
                alert('회원가입이 완료되었습니다!');
                setTimeout(() => {
                    router.replace("/login?message=register_success");
                }, 500);
            }

        } catch (error: any) {
            console.error('회원가입 요청 에러:', error);

            // 에러 메시지에 따라 특정 필드에 에러 설정
            if (error.message?.includes('아이디') || error.message?.includes('ID')) {
                form.setError("userLoginId", {
                    type: "server",
                    message: error.message || "이미 존재하는 아이디입니다."
                });
            } else if (error.message?.includes('유저이름') || error.message?.includes('이름')) {
                form.setError("username", {
                    type: "server",
                    message: error.message || "이미 존재하는 유저이름입니다."
                });
            } else if (error.message?.includes('비밀번호')) {
                form.setError("password", {
                    type: "server",
                    message: error.message || "비밀번호 오류입니다."
                });
            } else {
                // 기타 에러는 전체 에러로 표시
                setError(error.message || "회원가입에 실패했습니다. 다시 시도해주세요.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-[calc(100vh-64px-260px)] flex items-center justify-center">
            <motion.div
                variants={fadeInUp}
                initial="hidden"
                animate="show"
                className="w-full max-w-md border rounded-lg p-6 bg-card shadow"
            >
                <h1 className="text-2xl font-bold mb-6 text-center">회원가입</h1>
                <form onSubmit={form.handleSubmit(onRegister)} className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="userLoginId">아이디</Label>
                        <Input id="userLoginId" type="text" {...form.register("userLoginId")} />
                        {form.formState.errors.userLoginId && (
                            <p className="text-sm text-red-500">
                                {form.formState.errors.userLoginId.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="password">비밀번호</Label>
                        <Input id="password" type="password" {...form.register("password")} />
                        {form.formState.errors.password && (
                            <p className="text-sm text-red-500">
                                {form.formState.errors.password.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="confirmPassword">비밀번호 확인</Label>
                        <Input id="confirmPassword" type="password" {...form.register("confirmPassword")} />
                        {form.formState.errors.confirmPassword && (
                            <p className="text-sm text-red-500">
                                {form.formState.errors.confirmPassword.message}
                            </p>
                        )}
                    </div>



                    <div className="space-y-2">
                        <Label htmlFor="name">유저이름</Label>
                        <Input id="name" type="text" {...form.register("username")} />
                        {form.formState.errors.username && (
                            <p className="text-sm text-red-500">
                                {form.formState.errors.username.message}
                            </p>
                        )}
                    </div>

                    {error && <p className="text-sm text-red-500">{error}</p>}

                    <Button type="submit" className="w-full" disabled={isLoading}>
                        {isLoading ? "가입 중..." : "회원가입"}
                    </Button>
                </form>

                <div className="mt-6 text-center">
                    <p className="text-sm text-muted-foreground">
                        이미 계정이 있으신가요?{" "}
                        <Button
                            variant="link"
                            className="p-0 h-auto font-normal text-primary"
                            onClick={() => router.push('/login')}
                        >
                            로그인
                        </Button>
                    </p>
                </div>
            </motion.div>
        </div>
    );
}
