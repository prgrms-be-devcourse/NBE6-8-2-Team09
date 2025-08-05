// middlewar2e.ts (또는 src/middlewar2e.ts)
import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

const AUTH_ENABLED = process.env.NEXT_PUBLIC_AUTH_ENABLED !== "false";

export function middlewar2e(req: NextRequest) {
    // 인증 끄고 싶을 때는 그냥 통과
    if (!AUTH_ENABLED) return NextResponse.next();

    const { pathname } = req.nextUrl;
    const token = req.cookies.get("accessToken")?.value; // access_token → accessToken으로 변경
    const role = req.cookies.get("role")?.value;

    // ADMIN 전용
    if (pathname.startsWith("/admin")) {
        if (!token || role !== "ADMIN") {
            return NextResponse.redirect(new URL("/login", req.url));
        }
        return NextResponse.next();
    }

    // 보호가 필요한 경로
    const needAuth = [
        "/dashboard",
        "/wallet",
        "/transactions",
        "/coin-transactions",
    ].some((p) => pathname.startsWith(p));

    if (needAuth && !token) {
        return NextResponse.redirect(new URL("/login", req.url));
    }

    return NextResponse.next();
}


export const config = {
    matcher: [
        "/dashboard/:path*",
        "/wallet/:path*",
        "/transactions/:path*",
        "/coin-transactions/:path*",
        "/admin/:path*",
    ],
};
