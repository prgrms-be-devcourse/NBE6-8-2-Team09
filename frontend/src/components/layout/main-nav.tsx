"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { apiCall } from "@/lib/api/client";
import * as React from "react";

type NavLink = { href: string; label: string };

const defaultLinks: NavLink[] = [
    { href: "/", label: "Home" },
    { href: "/dashboard", label: "Dashboard" },
    { href: "/wallet", label: "Wallet" },
    { href: "/transactions", label: "Transactions" },
    { href: "/analytics", label: "Analytics" },
    { href: "/admin/coins/new", label: "Admin" },
];

type MainNavProps = React.ComponentPropsWithoutRef<"header"> & {
    innerClassName?: string;
    links?: NavLink[];
};

export function MainNav({
    className,
    innerClassName,
    links = defaultLinks,
    ...props
}: MainNavProps) {
    const pathname = usePathname();
    const [isLoggedIn, setIsLoggedIn] = React.useState(false);

    React.useEffect(() => {
        const checkLoginStatus = async () => {
            try {
                // API 클라이언트를 사용하여 일관된 URL과 설정으로 로그인 상태 확인
                const response = await apiCall('/v1/users/me');

                setIsLoggedIn(!!response);
                console.log('🔐 로그인 상태:', !!response);
            } catch (error) {
                console.error('로그인 상태 확인 실패:', error);
                setIsLoggedIn(false);
            }
        };
        
        // 초기 체크
        checkLoginStatus();
        
        // 페이지 포커스 시에도 체크
        window.addEventListener('focus', checkLoginStatus);
        
        // 주기적 체크 (30초마다)
        const interval = setInterval(checkLoginStatus, 30000);
        
        return () => {
            window.removeEventListener('focus', checkLoginStatus);
            clearInterval(interval);
        };
    }, []);

    return (
        <header className={cn("border-b bg-white", className)} {...props}>
            <div
                className={cn(
                    "w-full px-4 md:px-6 lg:px-8",
                    "flex h-16 items-center justify-between",
                    innerClassName
                )}
            >

                <Link href="/" className="flex items-center gap-2 font-bold text-amber-600">
                    <img 
                        src="/images/back9-coin-logo.PNG" 
                        alt="BACK9 Coin Logo" 
                        className="w-8 h-8 object-contain"
                    />
                    Back9 Coin
                </Link>

                <nav className="hidden md:flex gap-6">
                    {links.map((l) => {
                        const active = pathname === l.href;
                        return (
                            <Link
                                key={l.href}
                                href={l.href}
                                className={cn(
                                    "text-sm font-medium text-muted-foreground hover:text-foreground transition-colors",
                                    active && "text-foreground"
                                )}
                                aria-current={active ? "page" : undefined}
                                prefetch
                            >
                                {l.label}
                            </Link>
                        );
                    })}
                </nav>

                <Button asChild variant="outline" size="sm">
                    <Link href={isLoggedIn ? "/user" : "/login"}>
                        {isLoggedIn ? "MyPage" : "Login"}
                    </Link>
                </Button>
            </div>
        </header>
    );
}