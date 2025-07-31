import { NextResponse } from "next/server";

// 코인 관련 API 함수들
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// 토큰 가져오기 함수
const getAuthToken = () => {
    const cookies = document.cookie.split(';');
    // access_token 또는 accessToken 둘 다 지원
    const tokenCookie = cookies.find(cookie =>
        cookie.trim().startsWith('access_token=') ||
        cookie.trim().startsWith('accessToken=')
    );
    if (tokenCookie) {
        return tokenCookie.split('=')[1];
    }
    return null;
};

// 코인 목록 조회
export const getCoins = async () => {
    const token = getAuthToken();
    const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            ...(token && { "Authorization": `Bearer ${token}` }),
        },
    });

    if (!res.ok) {
        throw new Error(`코인 목록 조회 실패: ${res.status}`);
    }

    return res.json();
};

// 코인 등록
export const createCoin = async (coinData: {
    koreanName: string;
    englishName: string;
    symbol: string;
}) => {
    const token = getAuthToken();
    const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...(token && { "Authorization": `Bearer ${token}` }),
        },
        body: JSON.stringify(coinData),
    });

    if (!res.ok) {
        throw new Error(`코인 등록 실패: ${res.status}`);
    }

    return res.json();
};

// 코인 삭제
export const deleteCoin = async (id: number) => {
    const token = getAuthToken();
    const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins/${id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            ...(token && { "Authorization": `Bearer ${token}` }),
        },
    });

    if (!res.ok) {
        throw new Error(`코인 삭제 실패: ${res.status}`);
    }

    return res.json();
};