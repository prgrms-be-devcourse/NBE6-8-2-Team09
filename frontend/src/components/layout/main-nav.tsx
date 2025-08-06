"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { apiCall } from "@/lib/api/client";
import * as React from "react";

type NavLink = { href: string; label: string };

type MainNavProps = React.ComponentPropsWithoutRef<"header"> & {
    innerClassName?: string;
    links?: NavLink[];
};

export function MainNav({
    className,
    innerClassName,
    links,
    ...props
}: MainNavProps) {
    const pathname = usePathname();
    const [isLoggedIn, setIsLoggedIn] = React.useState(false);
    const [mounted, setMounted] = React.useState(false);

    // 서버와 클라이언트에서 동일한 기본 링크 사용
    const defaultLinks: NavLink[] = [
        { href: "/", label: "Home" },
        { href: "/dashboard", label: "Dashboard" },
        { href: "/wallet", label: "Wallet" },
        { href: "/transactions", label: "Transactions" },
        { href: "/analytics", label: "Analytics" },
        { href: "/admin/coins/new", label: "Admin" },
    ];

    const navigationLinks = links || defaultLinks;

    React.useEffect(() => {
        setMounted(true);

        const checkLoginStatus = async () => {
            try {
                const response = await apiCall('/v1/users/me');
                setIsLoggedIn(!!response);
            } catch (error) {
                setIsLoggedIn(false);
            }
        };

        checkLoginStatus();

        window.addEventListener('focus', checkLoginStatus);
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

                <nav className="hidden md:flex gap-6" suppressHydrationWarning>
                    {navigationLinks.map((l) => {
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
                                suppressHydrationWarning
                            >
                                {l.label}
                            </Link>
                        );
                    })}
                </nav>

                <Button asChild variant="outline" size="sm" suppressHydrationWarning>
                    <Link href={mounted ? (isLoggedIn ? "/user" : "/login") : "/login"} suppressHydrationWarning>
                        {mounted ? (isLoggedIn ? "MyPage" : "Login") : "Login"}
                    </Link>
                </Button>
            </div>
        </header>
    );
}