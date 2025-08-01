import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
    const searchParams = request.nextUrl.searchParams;
    const token = searchParams.get('token');
    const apiKey = searchParams.get('apiKey');
    const role = searchParams.get('role');
    const error = searchParams.get('error');

    if (error) {
        return NextResponse.redirect(new URL('/login?error=oauth_error', request.url));
    }

    if (!token) {
        return NextResponse.redirect(new URL('/login?error=no_token', request.url));
    }

    if (!apiKey) {
        return NextResponse.redirect(new URL('/login?error=no_apiKey', request.url));
    }

    try {
        const res = NextResponse.redirect(new URL('/dashboard', request.url));
        
        // 일반 로그인과 동일한 방식으로 쿠키 설정
        res.cookies.set('access_Token', token, {
            path: '/',
            httpOnly: false,
            sameSite: 'lax',
        });

        res.cookies.set('apiKey', apiKey, {
            path: '/',
            httpOnly: false,
            sameSite: 'lax',
        });

        res.cookies.set('role', role || 'USER', {
            path: '/',
            httpOnly: false,
            sameSite: 'lax',
        });

        return res;
    } catch (error) {
        console.error('Google OAuth 콜백 처리 에러:', error);
        return NextResponse.redirect(new URL('/login?error=server_error', request.url));
    }
}