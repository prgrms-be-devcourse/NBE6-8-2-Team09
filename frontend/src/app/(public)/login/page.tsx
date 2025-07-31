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

const schema = z.object({
    userLoginId: z.string().min(1, "아이디를 입력해주세요."),
    password: z.string().min(6, "비밀번호는 6자 이상"),
});
type FormValues = z.infer<typeof schema>;

export default function LoginPage() {
    const router = useRouter();
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    
    // OAuth 에러 메시지 설정
    useEffect(() => {
        // URL 파라미터에서 에러 확인
        const searchParams = new URLSearchParams(window.location.search);
        const oauthError = searchParams.get('error');
        const message = searchParams.get('message');
        const details = searchParams.get('details');
        
        if (oauthError) {
            const errorMessages: { [key: string]: string } = {
                'oauth_error': 'OAuth 인증 중 오류가 발생했습니다.',
                'no_code': '인증 코드를 받지 못했습니다.',
                'auth_failed': '인증에 실패했습니다.',
                'server_error': '서버 오류가 발생했습니다.'
            };
            
            let errorMsg = errorMessages[oauthError] || '알 수 없는 오류가 발생했습니다.';
            
            // 상세 에러 정보가 있으면 추가
            if (details) {
                try {
                    const errorDetails = JSON.parse(decodeURIComponent(details));
                    if (errorDetails.message) {
                        errorMsg += ` (${errorDetails.message})`;
                    }
                } catch (e) {
                    console.error('에러 상세 정보 파싱 실패:', e);
                }
            }
            
            setError(errorMsg);
        }
        
        if (message === 'register_success') {
            setSuccessMessage('회원가입이 완료되었습니다. 로그인해주세요.');
        }
    }, []);

    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: { userLoginId: "", password: "" },
    });

    const onSubmit = async (values: FormValues) => {
        setError(null);
        const res = await fetch("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(values),
        });

        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            setError(data.message ?? "로그인 실패");
            return;
        }

        router.replace("/dashboard");
    };

    return (
        <div className="min-h-[calc(100vh-64px-260px)] flex items-center justify-center">
            <motion.div
                variants={fadeInUp}
                initial="hidden"
                animate="show"
                className="w-full max-w-sm border rounded-lg p-6 bg-card shadow"
            >
                <h1 className="text-2xl font-bold mb-6 text-center">로그인</h1>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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

                    {error && <p className="text-sm text-red-500">{error}</p>}
                    {successMessage && <p className="text-sm text-green-600">{successMessage}</p>}

                    <Button type="submit" className="w-full">
                        로그인
                    </Button>
                    
                    <div className="relative">
                        <div className="absolute inset-0 flex items-center">
                            <span className="w-full border-t" />
                        </div>
                        <div className="relative flex justify-center text-xs uppercase">
                            <span className="bg-background px-2 text-muted-foreground">
                                또는
                            </span>
                        </div>
                    </div>

                    <Button 
                        type="button" 
                        variant="outline" 
                        className="w-full"
                        onClick={() => {
                            // 구글 OAuth 리다이렉트
                            const googleAuthUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/oauth2/authorization/google`;
                            window.location.href = googleAuthUrl;
                        }}
                    >
                        <svg className="mr-2 h-4 w-4" viewBox="0 0 24 24">
                            <path
                                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                                fill="#4285F4"
                            />
                            <path
                                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                                fill="#34A853"
                            />
                            <path
                                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                                fill="#FBBC05"
                            />
                            <path
                                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                                fill="#EA4335"
                            />
                        </svg>
                        Google로 로그인
                    </Button>
                </form>
                
                <div className="mt-6 text-center">
                    <p className="text-sm text-muted-foreground">
                        계정이 없으신가요?{" "}
                        <Button 
                            variant="link" 
                            className="p-0 h-auto font-normal text-primary"
                            onClick={() => router.push('/register')}
                        >
                            회원가입
                        </Button>
                    </p>
                </div>
            </motion.div>
        </div>
    );
}
