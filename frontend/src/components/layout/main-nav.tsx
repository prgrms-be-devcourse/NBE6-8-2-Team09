"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import * as React from "react";

type NavLink = { href: string; label: string };

const defaultLinks: NavLink[] = [
    { href: "/", label: "Home" },
    { href: "/dashboard", label: "Dashboard" },
    { href: "/wallet", label: "Wallet" },
    { href: "/transactions", label: "Transactions" },
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
        setIsLoggedIn(document.cookie.includes("access_token"));
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
                <Link href="/" className="font-bold text-blue-600">
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