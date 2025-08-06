// src/app/layout.tsx
import "./globals.css";
import React from "react";

export const metadata: { title: string; description: string } = {
    title: "Back9 Coin",
    description: "투자의 기준을 바꾸다.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="ko">
        <body>{children}</body>
        </html>
    );
}
