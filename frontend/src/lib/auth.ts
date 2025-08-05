import { cookies } from "next/headers";

export type Role = "ADMIN" | "MEMBER";

export async function getSessionFromCookie() {
    const cookieStore = await cookies(); // Next 15/edge에서 Promise임
    const token = cookieStore.get("accessToken")?.value; // 실제 쿠키 이름인 accessToken으로 수정
    const role = (cookieStore.get("role")?.value as Role) ?? "MEMBER";

    return {
        isAuthenticated: !!token,
        role,
        user: token ? { email: "you@example.com", role } : null,
    };
}
