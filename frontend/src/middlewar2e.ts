// middlewar2e.ts (또는 src/middlewar2e.ts)
import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

// HttpOnly 쿠키 사용 시 middleware에서 인증 확인 불가능하므로 비활성화
const AUTH_ENABLED = false; // process.env.NEXT_PUBLIC_AUTH_ENABLED !== "false";

export function middlewar2e(req: NextRequest) {
    // HttpOnly 쿠키는 middleware에서 읽을 수 없으므로 항상 통과
    // 실제 인증은 각 페이지에서 API 호출로 처리
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
