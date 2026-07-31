import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import {
    FileItem,
    FilePageResponse,
    DownloadUrlResponse,
} from '@/types/file'

export const fileApi = {

    // (1) Upload — dùng FormData, không phải JSON
    upload: async (
        workspaceId: string,
        file: File,
        folderId?: string
    ): Promise<FileItem> => {
        const formData = new FormData()
        formData.append('file', file)
        if (folderId) formData.append('folderId', folderId)

        const res = await axiosInstance.post<ApiResponse<FileItem>>(
            `/workspaces/${workspaceId}/files`,
            formData,
            {
                headers: { 'Content-Type': 'multipart/form-data' },
                // (2) Track upload progress
                onUploadProgress: (progressEvent) => {
                    const percent = Math.round(
                        (progressEvent.loaded * 100) / (progressEvent.total || 1)
                    )
                    // Sẽ dùng sau trong UI
                    console.log(`Upload: ${percent}%`)
                },
            }
        )
        return res.data.data
    },

    getInFolder: async (
        workspaceId: string,
        folderId: string,
        page = 0,
        size = 20
    ): Promise<FilePageResponse> => {
        const res = await axiosInstance.get<ApiResponse<FilePageResponse>>(
            `/workspaces/${workspaceId}/files/folder/${folderId}`,
            { params: { page, size } }
        )
        return res.data.data
    },

    getInRoot: async (
        workspaceId: string,
        page = 0,
        size = 20
    ): Promise<FilePageResponse> => {
        const res = await axiosInstance.get<ApiResponse<FilePageResponse>>(
            `/workspaces/${workspaceId}/files/root`,
            { params: { page, size } }
        )
        return res.data.data
    },

    search: async (
        workspaceId: string,
        keyword: string,
        page = 0
    ): Promise<FilePageResponse> => {
        const res = await axiosInstance.get<ApiResponse<FilePageResponse>>(
            `/workspaces/${workspaceId}/files/search`,
            { params: { keyword, page } }
        )
        return res.data.data
    },

    getDownloadUrl: async (
        workspaceId: string,
        fileId: string
    ): Promise<DownloadUrlResponse> => {
        const res = await axiosInstance.get<ApiResponse<DownloadUrlResponse>>(
            `/workspaces/${workspaceId}/files/${fileId}/download-url`
        )
        return res.data.data
    },

    delete: async (workspaceId: string, fileId: string): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/files/${fileId}`
        )
    },

    move: async (
        workspaceId: string,
        fileId: string,
        targetFolderId?: string
    ): Promise<FileItem> => {
        const res = await axiosInstance.patch<ApiResponse<FileItem>>(
            `/workspaces/${workspaceId}/files/${fileId}/move`,
            null,
            { params: { targetFolderId } }
        )
        return res.data.data
    },
}