// API 호출 공통 함수

// 백엔드 에러 응답 타입 정의
export interface ErrorResponse {
    status: number;
    code: string;
    message: string;
    timestamp?: string;
}

// API 기본 설정 - 환경별 구분
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ||
    (process.env.NODE_ENV === 'production'
        ? '/api'  // 프로덕션에서는 nginx 프록시 사용하므로 /api 접두사
        : '/api');  // 로컬에서도 nginx 프록시 사용하므로 /api 접두사


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
            credentials: 'include', // 쿠키 자동 전송
            ...options,
        });

        if (response.ok) {
            return await response.json();
        }

        // 에러 응답 처리
        let errorData: ErrorResponse;
        try {
            errorData = await response.json();
        } catch {
            // JSON 파싱 실패 시 기본 에러 메시지
            errorData = {
                status: response.status,
                code: `${response.status}-ERROR`,
                message: response.statusText || '서버 오류가 발생했습니다.'
            };
        }

        // 에러를 throw해서 catch 블록에서 처리할 수 있도록 함
        const error = new Error(errorData.message) as Error & ErrorResponse;
        error.status = errorData.status;
        error.code = errorData.code;
        throw error;

    } catch (error) {
        // 네트워크 에러 등의 경우
        if (error instanceof Error && !('status' in error)) {
            console.error('네트워크 에러:', error);
            const networkError = new Error('네트워크 연결을 확인해주세요.') as Error & ErrorResponse;
            networkError.status = 0;
            networkError.code = 'NETWORK_ERROR';
            throw networkError;
        }
        throw error;
    }
}
