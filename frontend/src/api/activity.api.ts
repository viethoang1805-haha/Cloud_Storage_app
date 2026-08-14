import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'

export interface ActivityLog {
    id: string
    actor: {
        email: string
        name: string
    }
    action: string
    actionDisplay: string
    workspaceId: string | null
    workspaceName: string | null
    targetType: string | null
    targetId: string | null
    targetName: string | null
    metadata: Record<string, unknown> | null
    ipAddress: string | null
    createdAt: string
}

export interface ActivityPageResponse {
    activities: ActivityLog[]
    currentPage: number
    totalPages: number
    totalElements: number
    hasNext: boolean
    hasPrevious: boolean
}

export const activityApi = {
    getWorkspaceActivities: async (
        workspaceId: string,
        params: {
            page?: number
            size?: number
            action?: string
            targetType?: string
        } = {}
    ): Promise<ActivityPageResponse> => {
        const res = await axiosInstance.get<ApiResponse<ActivityPageResponse>>(
            `/workspaces/${workspaceId}/activities`,
            { params: { page: 0, size: 20, ...params } }
        )
        return res.data.data
    },

    getMyActivities: async (
        workspaceId: string,
        params: { page?: number; size?: number } = {}
    ): Promise<ActivityPageResponse> => {
        const res = await axiosInstance.get<ApiResponse<ActivityPageResponse>>(
            `/workspaces/${workspaceId}/activities/mine`,
            { params: { page: 0, size: 20, ...params } }
        )
        return res.data.data
    },

    getAllActivities: async (
        params: {
            page?: number
            size?: number
            action?: string
        } = {}
    ): Promise<ActivityPageResponse> => {
        const res = await axiosInstance.get<ApiResponse<ActivityPageResponse>>(
            '/admin/activities',
            { params: { page: 0, size: 50, ...params } }
        )
        return res.data.data
    },
}