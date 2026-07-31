import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import {
    Workspace,
    WorkspaceCreateRequest,
    WorkspaceUpdateRequest,
} from '@/types/workspace'

export const workspaceApi = {

    getMyWorkspaces: async (): Promise<Workspace[]> => {
        const res = await axiosInstance.get<ApiResponse<Workspace[]>>('/workspaces')
        return res.data.data
    },

    getById: async (id: string): Promise<Workspace> => {
        const res = await axiosInstance.get<ApiResponse<Workspace>>(
            `/workspaces/${id}`
        )
        return res.data.data
    },

    create: async (data: WorkspaceCreateRequest): Promise<Workspace> => {
        const res = await axiosInstance.post<ApiResponse<Workspace>>(
            '/workspaces', data
        )
        return res.data.data
    },

    update: async (
        id: string,
        data: WorkspaceUpdateRequest
    ): Promise<Workspace> => {
        const res = await axiosInstance.put<ApiResponse<Workspace>>(
            `/workspaces/${id}`, data
        )
        return res.data.data
    },

    delete: async (id: string): Promise<void> => {
        await axiosInstance.delete(`/workspaces/${id}`)
    },
}