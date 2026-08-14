import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import {
    PersonalDashboard,
    WorkspaceDashboard,
    SystemDashboard,
} from '@/types/dashboard'

export const dashboardApi = {
    getPersonal: async (): Promise<PersonalDashboard> => {
        const res = await axiosInstance.get<ApiResponse<PersonalDashboard>>(
            '/dashboard'
        )
        return res.data.data
    },

    getWorkspace: async (workspaceId: string): Promise<WorkspaceDashboard> => {
        const res = await axiosInstance.get<ApiResponse<WorkspaceDashboard>>(
            `/workspaces/${workspaceId}/dashboard`
        )
        return res.data.data
    },

    getSystem: async (): Promise<SystemDashboard> => {
        const res = await axiosInstance.get<ApiResponse<SystemDashboard>>(
            '/admin/dashboard'
        )
        return res.data.data
    },
}