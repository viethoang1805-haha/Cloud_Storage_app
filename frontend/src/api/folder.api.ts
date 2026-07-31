import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'

export interface FolderItem {
    id: string
    name: string
    workspaceId: string
    parent: { id: string; name: string } | null
    childCount: number
    createdAt: string
}

export interface FolderTree extends FolderItem {
    depth: number
    children: FolderTree[]
}

export const folderApi = {

    getRootFolders: async (workspaceId: string): Promise<FolderItem[]> => {
        const res = await axiosInstance.get<ApiResponse<FolderItem[]>>(
            `/workspaces/${workspaceId}/folders`
        )
        return res.data.data
    },

    getTree: async (
        workspaceId: string,
        folderId: string
    ): Promise<FolderTree> => {
        const res = await axiosInstance.get<ApiResponse<FolderTree>>(
            `/workspaces/${workspaceId}/folders/${folderId}/tree`
        )
        return res.data.data
    },

    create: async (
        workspaceId: string,
        name: string,
        parentId?: string
    ): Promise<FolderItem> => {
        const res = await axiosInstance.post<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders`,
            { name, parentId }
        )
        return res.data.data
    },

    rename: async (
        workspaceId: string,
        folderId: string,
        name: string
    ): Promise<FolderItem> => {
        const res = await axiosInstance.put<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders/${folderId}`,
            { name }
        )
        return res.data.data
    },

    delete: async (workspaceId: string, folderId: string): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/folders/${folderId}`
        )
    },
}