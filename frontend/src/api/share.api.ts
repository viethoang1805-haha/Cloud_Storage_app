import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import {
    ShareLink,
    FilePermission,
    CreateShareLinkRequest,
    ShareWithUserRequest,
} from '@/types/share'

export const shareApi = {

    // (1) getLink trả về null nếu 404, không throw
    getLink: async (
        workspaceId: string,
        fileId: string
    ): Promise<ShareLink | null> => {
        try {
            const res = await axiosInstance.get<ApiResponse<ShareLink>>(
                `/workspaces/${workspaceId}/files/${fileId}/share/link`
            )
            return res.data.data
        } catch (err: any) {
            if (err?.response?.status === 404) return null
            throw err
        }
    },

    createLink: async (
        workspaceId: string,
        fileId: string,
        data: CreateShareLinkRequest = {}
    ): Promise<ShareLink> => {
        const res = await axiosInstance.post<ApiResponse<ShareLink>>(
            `/workspaces/${workspaceId}/files/${fileId}/share/link`,
            data
        )
        return res.data.data
    },

    deactivateLink: async (
        workspaceId: string,
        fileId: string
    ): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/files/${fileId}/share/link`
        )
    },

    shareWithUser: async (
        workspaceId: string,
        fileId: string,
        data: ShareWithUserRequest
    ): Promise<FilePermission> => {
        const res = await axiosInstance.post<ApiResponse<FilePermission>>(
            `/workspaces/${workspaceId}/files/${fileId}/share/users`,
            data
        )
        return res.data.data
    },

    getPermissions: async (
        workspaceId: string,
        fileId: string
    ): Promise<FilePermission[]> => {
        const res = await axiosInstance.get<ApiResponse<FilePermission[]>>(
            `/workspaces/${workspaceId}/files/${fileId}/share/users`
        )
        return res.data.data
    },

    updatePermission: async (
        workspaceId: string,
        fileId: string,
        userId: string,
        permission: string
    ): Promise<FilePermission> => {
        const res = await axiosInstance.put<ApiResponse<FilePermission>>(
            `/workspaces/${workspaceId}/files/${fileId}/share/users/${userId}`,
            { permission }
        )
        return res.data.data
    },

    revokePermission: async (
        workspaceId: string,
        fileId: string,
        userId: string
    ): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/files/${fileId}/share/users/${userId}`
        )
    },
}