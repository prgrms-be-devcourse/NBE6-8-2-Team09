// src/components/layout/footer.tsx
"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

type FooterProps = React.ComponentPropsWithoutRef<"footer"> & {
    /** 내부 컨텐츠 래퍼에 주고 싶은 클래스 (선택) */
    innerClassName?: string;
};

export function Footer({ className, innerClassName, ...props }: FooterProps) {
    return (
        <footer
            className={cn("bg-blue-600 text-white mt-8", className)}
            {...props}
        >
            {/* PageShell에서 이미 max-width를 관리하므로 여기서는 제거 */}
            <div
                className={cn(
                    "w-full px-4 md:px-6 lg:px-8 py-10",
                    innerClassName
                )}
            >
                <div className="grid gap-8 md:grid-cols-4">
                    <div>
                        <div className="font-bold text-lg">Back9 Coin</div>
                        <div className="text-sm mt-2">투자의 기준을 바꾸다.</div>
                    </div>
                    <div>
                        <div className="font-semibold mb-2">Information</div>
                        <ul className="space-y-1 text-sm opacity-90">
                            <li>About</li>
                        </ul>
                    </div>
                    <div>
                        <div className="font-semibold mb-2">Company</div>
                        <ul className="space-y-1 text-sm opacity-90">
                            <li>Community</li>
                        </ul>
                    </div>
                    <div>
                        <div className="font-semibold mb-2">Contact</div>
                        <ul className="space-y-1 text-sm opacity-90">
                            <li>Getting Started</li>
                        </ul>
                    </div>
                </div>

                <div className="mt-10 text-xs opacity-75">
                    2025 all Right Reserved
                    <br />Team BackGoo
                </div>
            </div>
        </footer>
    );
}
