const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// 토큰 가져오기 함수
const getAuthToken = () => {
    const cookies = document.cookie.split(';');
    const tokenCookie = cookies.find(cookie =>
        cookie.trim().startsWith('accessToken=')
    );
    if (tokenCookie) {
        const token = tokenCookie.split('=')[1];
        return tokenCookie.split('=')[1];
    }
    return null;
};

export const coinApi = {

    // 코인 목록 조회 (관리자용)
    getCoins: async () => {
        const token = getAuthToken();
        const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                ...(token && {"Authorization": `Bearer ${token}`}),
            },
            credentials: "include",
        });

        if (!res.ok) {
            throw new Error(`코인 목록 조회 실패: ${res.status}`);
        }

        return res.json();
    },

    // 코인 등록 (관리자용)
    createCoin: async (coinData: { koreanName: string; englishName: string; symbol: string; }) => {
        const token = getAuthToken();
        const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...(token && {"Authorization": `Bearer ${token}`}),
            },
            credentials: "include",
            body: JSON.stringify(coinData),
        });

        if (!res.ok) {
            throw new Error(`코인 등록 실패: ${res.status}`);
        }

        return res.json();
    },

    // 코인 삭제 (관리자용)
    deleteCoin: async (id: number) => {
        const token = getAuthToken();
        const res = await fetch(`${API_BASE_URL}/api/v1/adm/coins/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                ...(token && {"Authorization": `Bearer ${token}`}),
            },
            credentials: "include",
        });

        if (!res.ok) {
            throw new Error(`코인 삭제 실패: ${res.status}`);
        }
    },
}