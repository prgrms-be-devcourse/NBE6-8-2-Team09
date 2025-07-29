// API 호출 공통 함수

// API 기본 설정
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

/**
 * 공통 API 호출 헬퍼 함수
 */
export async function apiCall<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<T | null> {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        });

        if (response.ok) {
            return await response.json();
        }

        console.error('API 호출 실패:', response.status, response.statusText);
        return null;
    } catch (error) {
        console.error('API 호출 에러:', error);
        throw error;
    }
}
