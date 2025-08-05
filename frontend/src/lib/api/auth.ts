import { apiCall } from './client'
import type { ApiResponse } from '@/lib/types/common'
import type { userRegisterDto, userLoginReqBody, userLoginResBody } from '@/lib/types/user'

export const authApi = {
    // 회원가입
    register: (userData: userRegisterDto) =>
        apiCall<ApiResponse<userLoginResBody>>('/users', {
            method: 'POST',
            body: JSON.stringify(userData),
        }),

    // 로그인
    login: (credentials: userLoginReqBody) =>
        apiCall<ApiResponse<userLoginResBody>>('/users/login', {
            method: 'POST',
            body: JSON.stringify(credentials),
        }),

    // 로그아웃
    logout: () =>
        apiCall<ApiResponse<void>>('/users/logout', {
            method: 'POST',
        }),

    // 회원 탈퇴
    deleteUser: (userLoginId: string) =>
        apiCall<ApiResponse<void>>(`/users?userLoginId=${userLoginId}`, {
            method: 'DELETE',
        }),
}
