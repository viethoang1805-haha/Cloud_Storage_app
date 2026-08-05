import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import { UserProfile, UpdateProfileRequest, ChangePasswordRequest } from '@/types/user'

export const userApi = {
    getMe: async (): Promise<UserProfile> => {
        const res = await axiosInstance.get<ApiResponse<UserProfile>>('/users/me')
        return res.data.data
    },

    updateProfile: async (data: UpdateProfileRequest): Promise<UserProfile> => {
        const res = await axiosInstance.put<ApiResponse<UserProfile>>('/users/me', data)
        return res.data.data
    },

    changePassword: async (data: ChangePasswordRequest): Promise<void> => {
        await axiosInstance.put('/users/me/password', data)
    },

    uploadAvatar: async (file: File): Promise<UserProfile> => {
        const formData = new FormData()
        formData.append('file', file)
        const res = await axiosInstance.post<ApiResponse<UserProfile>>(
            '/users/me/avatar',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        )
        return res.data.data
    },

    getStorageInfo: async () => {
        const res = await axiosInstance.get<ApiResponse<UserProfile['storage']>>(
            '/users/me/storage'
        )
        return res.data.data
    },
}