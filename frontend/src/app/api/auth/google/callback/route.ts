import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
    console.log('=== Google OAuth 콜백 API 호출됨 ===');
    console.log('요청 URL:', request.url);

    const searchParams = request.nextUrl.searchParams;
    const token = searchParams.get('token');
    const apiKey = searchParams.get('apiKey');
    const role = searchParams.get('role');
    const error = searchParams.get('error');

    console.log('=== URL 파라미터 확인 ===');
    console.log('token:', token ? `존재 (길이: ${token.length})` : 'null');
    console.log('apiKey:', apiKey ? `존재 (${apiKey})` : 'null');
    console.log('role:', role || 'null');
    console.log('error:', error || 'null');

    // 환경별 기본 URL 설정
    const baseUrl = process.env.NEXT_PUBLIC_FRONTEND_URL ||
        (process.env.NODE_ENV === 'production'
            ? 'https://peuronteuendeu.onrender.com'
            : 'http://localhost:8888');  // 로컬에서는 nginx 포트 사용

    if (error) {
        console.log('OAuth 에러 발생, 로그인 페이지로 리다이렉트');
        return NextResponse.redirect(`${baseUrl}/login?error=oauth_error`);
    }

    if (!token) {
        console.log('토큰 없음, 로그인 페이지로 리다이렉트');
        return NextResponse.redirect(`${baseUrl}/login?error=no_token`);
    }

    if (!apiKey) {
        console.log('API 키 없음, 로그인 페이지로 리다이렉트');
        return NextResponse.redirect(`${baseUrl}/login?error=no_apiKey`);
    }

    try {
        console.log('=== 쿠키 설정 시작 ===');

        const res = NextResponse.redirect(`${baseUrl}/dashboard`);

        // 환경별 쿠키 설정
        const isProduction = process.env.NODE_ENV === 'production';

        res.cookies.set('accessToken', token, {
            path: '/',
            httpOnly: false,
            secure: isProduction,  // 프로덕션에서는 HTTPS이므로 secure true
            sameSite: isProduction ? 'none' : 'lax',  // 프로덕션에서는 크로스 도메인
            maxAge: 3600
        });

        res.cookies.set('apiKey', apiKey, {
            path: '/',
            httpOnly: false,
            secure: isProduction,  // 프로덕션에서는 HTTPS이므로 secure true
            sameSite: isProduction ? 'none' : 'lax',  // 프로덕션에서는 크로스 도메인
            maxAge: 3600
        });

        res.cookies.set('role', role || 'MEMBER', {
            path: '/',
            httpOnly: false,
            secure: isProduction,  // 프로덕션에서는 HTTPS이므로 secure true
            sameSite: isProduction ? 'none' : 'lax',  // 프로덕션에서는 크로스 도메인
            maxAge: 3600
        });

        console.log('=== 쿠키 설정 완료 ===');
        console.log('- accessToken 쿠키 설정');
        console.log('- apiKey 쿠키 설정');
        console.log('- role 쿠키 설정');
        console.log(`대시보드로 리다이렉트: ${baseUrl}/dashboard`);

        return res;
    } catch (error) {
        console.error('=== Google OAuth 콜백 처리 에러 ===');
        console.error('에러:', error);
        return NextResponse.redirect(`${baseUrl}/login?error=server_error`);
    }
}
