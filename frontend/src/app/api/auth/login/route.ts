import { NextResponse } from "next/server";

export async function POST(req: Request) {
    const { userLoginId, password } = await req.json();

    try {
        // 스프링부트 서버로 로그인 요청
        const springApiUrl = process.env.SPRING_API_URL || 'http://localhost:8080/api';
        const response = await fetch(`${springApiUrl}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userLoginId,
                password
            }),
        });

        const data = await response.json();

        if (!response.ok) {
            return NextResponse.json(
                { message: data.message || "로그인에 실패했습니다." }, 
                { status: response.status }
            );
        }

        const res = NextResponse.json({
            ok: true,
            user: data.user || { userLoginId, role: data.role || "USER" },
        });

        // 스프링부트에서 받은 토큰을 쿠키에 설정
        if (data.accessToken) {
            res.cookies.set("access_token", data.accessToken, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                path: "/",
                maxAge: 60 * 60 * 24, // 24시간
            });
        }

        if (data.role) {
            res.cookies.set("role", data.role, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                path: "/",
                maxAge: 60 * 60 * 24, // 24시간
            });
        }

        return res;
    } catch (error) {
        console.error('로그인 API 호출 에러:', error);
        return NextResponse.json(
            { message: "서버 연결에 실패했습니다." }, 
            { status: 500 }
        );
    }
}
