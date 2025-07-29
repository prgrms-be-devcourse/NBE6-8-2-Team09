import { NextResponse } from "next/server";

export async function POST(req: Request) {
    const { userLoginId, password, name } = await req.json();

    try {
        // 스프링부트 서버로 회원가입 요청
        const springApiUrl = process.env.SPRING_API_URL || 'http://localhost:8080/api';
        const response = await fetch(`${springApiUrl}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userLoginId,
                password,
                name
            }),
        });

        const data = await response.json();

        if (!response.ok) {
            return NextResponse.json(
                { message: data.message || "회원가입에 실패했습니다." }, 
                { status: response.status }
            );
        }

        return NextResponse.json({
            ok: true,
            message: "회원가입이 완료되었습니다.",
            user: data.user
        });
    } catch (error) {
        console.error('회원가입 API 호출 에러:', error);
        return NextResponse.json(
            { message: "서버 연결에 실패했습니다." }, 
            { status: 500 }
        );
    }
} 