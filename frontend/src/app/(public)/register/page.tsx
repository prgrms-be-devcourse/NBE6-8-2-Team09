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
    confirmPassword: z.string().min(1, "비밀번호 확인을 입력해주세요."),
    name: z.string().min(1, "이름을 입력해주세요."),
}).refine((data) => data.password === data.confirmPassword, {
    message: "비밀번호가 일치하지 않습니다.",
    path: ["confirmPassword"],
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
                'duplicate_email': '이미 사용 중인 이메일입니다.',
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
            name: ""
        },
    });

    const onSubmit = async (values: FormValues) => {
        setError(null);
        setIsLoading(true);
        
        try {
            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    userLoginId: values.userLoginId,
                    password: values.password,
                    name: values.name
                }),
            });

            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                setError(data.message ?? "회원가입에 실패했습니다.");
                return;
            }

            // 회원가입 성공 시 로그인 페이지로 이동
            router.replace("/login?message=register_success");
        } catch (error) {
            setError("서버 연결에 실패했습니다.");
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
                        <Label htmlFor="name">이름</Label>
                        <Input id="name" type="text" {...form.register("name")} />
                        {form.formState.errors.name && (
                            <p className="text-sm text-red-500">
                                {form.formState.errors.name.message}
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