import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
    const searchParams = request.nextUrl.searchParams;
    const code = searchParams.get('code');
    const error = searchParams.get('error');

    if (error) {
        // OAuth 에러 처리
        return NextResponse.redirect(new URL('/login?error=oauth_error', request.url));
    }

    if (!code) {
        return NextResponse.redirect(new URL('/login?error=no_code', request.url));
    }

    try {
        // 스프링부트 서버로 인증 코드 전달
        const springApiUrl = process.env.SPRING_API_URL || 'http://localhost:8080';
        const response = await fetch(`${springApiUrl}/oauth2/callback/google?code=${code}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        const data = await response.json();

        if (!response.ok) {
            console.error('OAuth 콜백 에러:', response.status, data);
            return NextResponse.redirect(new URL(`/login?error=auth_failed&details=${encodeURIComponent(JSON.stringify(data))}`, request.url));
        }

        // 성공 시 대시보드로 리다이렉트
        const redirectUrl = new URL('/dashboard', request.url);
        
        // 토큰을 쿼리 파라미터로 전달 (실제로는 쿠키나 세션 사용 권장)
        if (data.accessToken) {
            redirectUrl.searchParams.set('token', data.accessToken);
        }

        return NextResponse.redirect(redirectUrl);
    } catch (error) {
        console.error('Google OAuth 콜백 처리 에러:', error);
        return NextResponse.redirect(new URL('/login?error=server_error', request.url));
    }
} 