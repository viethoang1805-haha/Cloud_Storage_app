import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import { FolderItem, FolderTree, FolderCreateRequest, FolderRenameRequest } from '@/types/folder'

export const folderApi = {
    getRootFolders: async (workspaceId: string): Promise<FolderItem[]> => {
        const res = await axiosInstance.get<ApiResponse<FolderItem[]>>(
            `/workspaces/${workspaceId}/folders`
        )
        return res.data.data
    },

    getById: async (workspaceId: string, folderId: string): Promise<FolderItem> => {
        const res = await axiosInstance.get<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders/${folderId}`
        )
        return res.data.data
    },

    getTree: async (workspaceId: string, folderId: string): Promise<FolderTree> => {
        const res = await axiosInstance.get<ApiResponse<FolderTree>>(
            `/workspaces/${workspaceId}/folders/${folderId}/tree`
        )
        return res.data.data
    },

    create: async (
        workspaceId: string,
        data: FolderCreateRequest
    ): Promise<FolderItem> => {
        const res = await axiosInstance.post<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders`,
            data
        )
        return res.data.data
    },

    rename: async (
        workspaceId: string,
        folderId: string,
        data: FolderRenameRequest
    ): Promise<FolderItem> => {
        const res = await axiosInstance.put<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders/${folderId}`,
            data
        )
        return res.data.data
    },

    move: async (
        workspaceId: string,
        folderId: string,
        targetParentId?: string
    ): Promise<FolderItem> => {
        const res = await axiosInstance.patch<ApiResponse<FolderItem>>(
            `/workspaces/${workspaceId}/folders/${folderId}/move`,
            { targetParentId }
        )
        return res.data.data
    },

    delete: async (workspaceId: string, folderId: string): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/folders/${folderId}`
        )
    },
    // Thêm method này vào folderApi object:
    getChildren: async (workspaceId: string, parentId: string): Promise<FolderItem[]> => {
        const res = await axiosInstance.get<ApiResponse<FolderItem[]>>(
            `/workspaces/${workspaceId}/folders`,
            { params: { parentId } }
        )
        return res.data.data
    },

}