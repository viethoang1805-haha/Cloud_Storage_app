import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import { NotificationPageResponse } from '@/types/notification'

export const notificationApi = {

    getAll: async (
        page = 0,
        size = 20,
        unreadOnly = false
    ): Promise<NotificationPageResponse> => {
        const res = await axiosInstance.get<ApiResponse<NotificationPageResponse>>(
            '/notifications',
            { params: { page, size, unreadOnly } }
        )
        return res.data.data
    },

    getUnreadCount: async (): Promise<number> => {
        const res = await axiosInstance.get<ApiResponse<number>>(
            '/notifications/unread-count'
        )
        return res.data.data
    },

    markAsRead: async (id: string): Promise<void> => {
        await axiosInstance.patch(`/notifications/${id}/read`)
    },

    markAllAsRead: async (): Promise<void> => {
        await axiosInstance.patch('/notifications/read-all')
    },
}