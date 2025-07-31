"use client";

import { useEffect, useState } from "react";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import {
    useForm,
    type Resolver,
    type UseFormReturn,
    type SubmitHandler,
} from "react-hook-form";
import { motion } from "framer-motion";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

// symbol 컬럼 추가
const schema = z.object({
    koreanName: z.string().min(1, "한글 이름은 필수입니다."),
    englishName: z.string().min(1, "영문 이름은 필수입니다."),
    symbol: z.string().min(1, "심볼은 필수입니다."),
});
type FormValues = z.infer<typeof schema>;

// Coin 타입을 백엔드 응답에 맞게 수정
type Coin = {
    id: number;
    createdAt: string;  // camelCase로 수정
    modifiedAt: string; // camelCase로 수정 (또는 updatedAt)
    koreanName: string | null;
    englishName: string | null;
    symbol: string;
};

const fadeInUp = {
    hidden: { opacity: 0, y: 16 },
    show: { opacity: 1, y: 0, transition: { duration: 0.4 } },
};

const stagger = (delay = 0.1) => ({
    hidden: {},
    show: {
        transition: {
            staggerChildren: delay,
        },
    },
});

// 날짜 포맷 함수 추가
function formatDate(dateString: string) {
    if (!dateString) return "";
    // ISO 8601 또는 타임스탬프 등 다양한 포맷을 지원하도록 시도
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString; // 파싱 실패시 원본 반환
    // YYYY-MM-DD HH:mm:ss 형식으로 반환
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}:${String(date.getSeconds()).padStart(2, "0")}`;
}

export default function AdminCoinNewPage() {
    const resolver = zodResolver(schema) as Resolver<FormValues>;
    const form = useForm<FormValues>({
        resolver,
        defaultValues: { koreanName: "", englishName: "", symbol: "" },
    });

    const [coins, setCoins] = useState<Coin[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 코인 목록 불러오기
    const fetchCoins = async () => {
        setLoading(true);
        setError(null);
        try {
            // 실제 API 엔드포인트로 변경
            const res = await fetch("http://localhost:8080/api/v1/adm/coins", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!res.ok) {
                // 응답이 실패일 때 에러 메시지 출력
                const text = await res.text();
                setError(`코인 목록 불러오기 실패: ${text}`);
                setCoins([]);
                setLoading(false);
                return;
            }

            // 응답 데이터가 배열이 아닐 경우를 대비한 방어 코드
            const data = await res.json();
        
            console.log("서버 응답:", data);
            console.log("첫 번째 코인:", data[0]); // 확인용

            if (!Array.isArray(data)) {
                setError("서버에서 올바른 코인 목록을 반환하지 않았습니다.");
                setCoins([]);
            } else {
                setCoins(data);
            }
        } catch (e: any) {
            setError(`코인 목록 불러오기 중 오류 발생: ${e?.message ?? String(e)}`);
            setCoins([]);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchCoins();
    }, []);

    const onSubmit: SubmitHandler<FormValues> = async (values) => {
        try {
            // 실제 API 엔드포인트로 변경
            const res = await fetch("http://localhost:8080/api/v1/adm/coins", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(values),
            });
            if (!res.ok) throw new Error("코인 등록 실패");
            await res.json();
            form.reset();
            fetchCoins();
        } catch (e) {
            // 에러 처리 필요시 여기에 추가
            alert("코인 등록에 실패했습니다.");
        }
    };

    // 코인 삭제 함수 추가
    const deleteCoin = async (id: number) => {
        if (!confirm(`정말로 이 코인을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            const res = await fetch(`http://localhost:8080/api/v1/adm/coins/${id}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                },
            });
            
            if (!res.ok) {
                const errorText = await res.text();
                alert(`삭제 실패: ${errorText}`);
                return;
            }
            
            alert("코인이 성공적으로 삭제되었습니다.");
            fetchCoins(); // 목록 새로고침
        } catch (e) {
            alert("코인 삭제 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="container py-8 flex flex-col lg:flex-row gap-8">
            {/* 왼쪽: 코인 등록 폼 */}
            <motion.div
                className="w-full lg:w-1/3 max-w-md"
                variants={stagger(0.08)}
                initial="hidden"
                animate="show"
            >
                <motion.h2
                    variants={fadeInUp}
                    className="text-xl font-semibold mb-4"
                >
                    가상화폐 추가 (Admin)
                </motion.h2>
                <motion.form
                    variants={stagger(0.05)}
                    onSubmit={form.handleSubmit(onSubmit)}
                    className="space-y-4"
                >
                    <motion.div variants={fadeInUp}>
                        <Field name="koreanName" label="한글 이름" placeholder="비트코인" form={form} />
                    </motion.div>
                    <motion.div variants={fadeInUp}>
                        <Field name="englishName" label="영문 이름" placeholder="Bitcoin" form={form} />
                    </motion.div>
                    <motion.div variants={fadeInUp}>
                        <Field name="symbol" label="심볼" placeholder="KRW-BTC" form={form} />
                    </motion.div>
                    <motion.div variants={fadeInUp}>
                        <Button type="submit" className="w-full transition hover:scale-[1.02] hover:bg-blue-500 active:scale-[0.99]">추가</Button>
                    </motion.div>
                </motion.form>
            </motion.div>
            {/* 오른쪽: 코인 전체 목록 */}
            <motion.div
                className="w-full lg:w-2/3"
                variants={stagger(0.08)}
                initial="hidden"
                animate="show"
            >
                <motion.h2
                    variants={fadeInUp}
                    className="text-xl font-semibold mb-4"
                >
                    코인 전체 목록
                </motion.h2>
                {error && (
                    <motion.div variants={fadeInUp} className="text-red-500 mb-2">
                        {error}
                    </motion.div>
                )}
                {loading ? (
                    <motion.div variants={fadeInUp} className="text-gray-500">불러오는 중...</motion.div>
                ) : (
                    <motion.div variants={fadeInUp} className="overflow-x-auto">
                        <table className="w-full border text-sm">
                            <thead>
                                <tr className="bg-gray-100">
                                    <th className="border px-2 py-1">ID</th>
                                    <th className="border px-2 py-1">생성일</th>
                                    <th className="border px-2 py-1">수정일</th>
                                    <th className="border px-2 py-1">한글 이름</th>
                                    <th className="border px-2 py-1">영문 이름</th>
                                    <th className="border px-2 py-1">심볼</th>
                                    <th className="border px-2 py-1 text-center">작업</th>
                                </tr>
                            </thead>
                            <tbody>
                                {coins.length === 0 && !error ? (
                                    <tr>
                                        <td colSpan={7} className="text-center py-4 text-gray-400">등록된 코인이 없습니다.</td>
                                    </tr>
                                ) : (
                                    coins.map((coin, idx) => (
                                        <motion.tr
                                            key={coin.id}
                                            variants={fadeInUp}
                                            transition={{ delay: idx * 0.05 }}
                                        >
                                            <td className="border px-2 py-1">{coin.id}</td>
                                            {/* 생성일, 수정일이 제대로 표시되지 않는 경우를 위해 포맷팅 추가 */}
                                            <td className="border px-2 py-1">
                                                {coin.createdAt ? formatDate(coin.createdAt) : <span className="text-gray-400">-</span>}
                                            </td>
                                            <td className="border px-2 py-1">
                                                {coin.modifiedAt ? formatDate(coin.modifiedAt) : <span className="text-gray-400">-</span>}
                                            </td>
                                            <td className="border px-2 py-1">{coin.koreanName || <span className="text-gray-400">-</span>}</td>
                                            <td className="border px-2 py-1">{coin.englishName || <span className="text-gray-400">-</span>}</td>
                                            <td className="border px-2 py-1">{coin.symbol}</td>
                                            <td className="border px-2 py-1 text-center">
                                                <div className="flex justify-center">
                                                    <Button
                                                        onClick={() => deleteCoin(coin.id)}
                                                        variant="destructive"
                                                        size="sm"
                                                        className="text-xs px-2 py-1"
                                                    >
                                                        삭제
                                                    </Button>
                                                </div>
                                            </td>
                                        </motion.tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                        {/* 생성일, 수정일이 안 뜨는 경우 안내 메시지 */}
                        {coins.length > 0 && coins.some(c => !c.createdAt || !c.modifiedAt) && (
                            <div className="mt-2 text-xs text-yellow-600">
                                ※ 생성일/수정일이 비어있다면 서버에서 해당 필드가 누락되어 내려오고 있을 수 있습니다.<br />
                                API 응답의 createdAt, modifiedAt 필드 값을 확인해 주세요.
                            </div>
                        )}
                    </motion.div>
                )}
            </motion.div>
        </div>
    );
}

type FieldProps = {
    name: keyof FormValues;
    label: string;
    placeholder?: string;
    type?: React.InputHTMLAttributes<HTMLInputElement>["type"];
    registerOptions?: Parameters<UseFormReturn<FormValues>["register"]>[1];
    form: UseFormReturn<FormValues>;
};

function Field({ name, label, placeholder, type = "text", registerOptions, form }: FieldProps) {
    const { register, formState: { errors } } = form;
    return (
        <div className="space-y-2">
            <Label htmlFor={name}>{label}</Label>
            <Input id={name} type={type} placeholder={placeholder} {...register(name, registerOptions)} />
            {errors[name]?.message && (
                <p className="text-sm text-red-500">{String(errors[name]?.message)}</p>
            )}
        </div>
    );
}
