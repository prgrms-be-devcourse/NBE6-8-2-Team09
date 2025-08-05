import { redirect } from "next/navigation";
import { getSessionFromCookie } from "./auth";

const AUTH_ENABLED = process.env.NEXT_PUBLIC_AUTH_ENABLED !== "false";

export async function requireAuth() {
    if (!AUTH_ENABLED) {
        return { isAuthenticated: false, role: "MEMBER" as const, user: null };
    }
    const session = await getSessionFromCookie();
    if (!session.isAuthenticated) redirect("/login");
    return session;
}

export async function requireRole(roles: ("ADMIN" | "MEMBER")[]) {
    const session = await requireAuth();
    if (!roles.includes(session.role)) {
        redirect("/dashboard");
    }
    return session;
}
