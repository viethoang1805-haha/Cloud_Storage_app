import axiosInstance from './axios'
import {
    LoginRequest,
    RegisterRequest,
    TokenResponse,
    RefreshTokenRequest,
} from '@/types/auth'
import { ApiResponse } from '@/types/common'

export const authApi = {

    // (1) Đăng ký
    register: async (data: RegisterRequest): Promise<TokenResponse> => {
        const response = await axiosInstance.post<ApiResponse<TokenResponse>>(
            '/auth/register',
            data
        )
        return response.data.data
    },

    // (2) Đăng nhập
    login: async (data: LoginRequest): Promise<TokenResponse> => {
        const response = await axiosInstance.post<ApiResponse<TokenResponse>>(
            '/auth/login',
            data
        )
        return response.data.data
    },

    // (3) Refresh token
    refresh: async (data: RefreshTokenRequest): Promise<TokenResponse> => {
        const response = await axiosInstance.post<ApiResponse<TokenResponse>>(
            '/auth/refresh',
            data
        )
        return response.data.data
    },

    // (4) Logout
    logout: async (refreshToken: string): Promise<void> => {
        await axiosInstance.post('/auth/logout', { refreshToken })
    },

    // (5) Logout tất cả thiết bị
    logoutAll: async (): Promise<void> => {
        await axiosInstance.post('/auth/logout-all')
    },
}