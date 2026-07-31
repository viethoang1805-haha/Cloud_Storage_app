import {
    useQuery,
    useMutation,
    useQueryClient,
    useInfiniteQuery,
} from '@tanstack/react-query'
import { fileApi } from '@/api/file.api'
import toast from 'react-hot-toast'
import { useState } from 'react'

export const fileKeys = {
    all: ['files'] as const,
    workspace: (wId: string) => [...fileKeys.all, wId] as const,
    root: (wId: string) => [...fileKeys.workspace(wId), 'root'] as const,
    folder: (wId: string, fId: string) =>
        [...fileKeys.workspace(wId), 'folder', fId] as const,
    search: (wId: string, kw: string) =>
        [...fileKeys.workspace(wId), 'search', kw] as const,
}

// (1) File ở root workspace với infinite scroll
export function useRootFiles(workspaceId: string) {
    return useInfiniteQuery({
        queryKey: fileKeys.root(workspaceId),
        queryFn: ({ pageParam = 0 }) =>
            fileApi.getInRoot(workspaceId, pageParam as number),
        // (2) Lấy page tiếp theo
        getNextPageParam: (lastPage) =>
            lastPage.hasNext ? lastPage.currentPage + 1 : undefined,
        initialPageParam: 0,
        enabled: !!workspaceId,
    })
}

// File trong folder với infinite scroll
export function useFolderFiles(workspaceId: string, folderId: string) {
    return useInfiniteQuery({
        queryKey: fileKeys.folder(workspaceId, folderId),
        queryFn: ({ pageParam = 0 }) =>
            fileApi.getInFolder(workspaceId, folderId, pageParam as number),
        getNextPageParam: (lastPage) =>
            lastPage.hasNext ? lastPage.currentPage + 1 : undefined,
        initialPageParam: 0,
        enabled: !!workspaceId && !!folderId,
    })
}

// Tìm kiếm file
export function useSearchFiles(workspaceId: string, keyword: string) {
    return useQuery({
        queryKey: fileKeys.search(workspaceId, keyword),
        queryFn: () => fileApi.search(workspaceId, keyword),
        // (3) Debounce: chỉ fetch khi keyword đủ dài
        enabled: !!workspaceId && keyword.length > 1,
        staleTime: 30 * 1000,  // Cache 30 giây cho search
    })
}

// Upload file với progress tracking
export function useUploadFile(workspaceId: string) {
    const queryClient = useQueryClient()
    const [progress, setProgress] = useState(0)

    const mutation = useMutation({
        mutationFn: ({
                         file,
                         folderId,
                     }: {
            file: File
            folderId?: string
        }) => {
            // (4) Override upload để track progress
            return fileApi.upload(workspaceId, file, folderId)
        },

        onSuccess: (_, variables) => {
            // (5) Invalidate cache để refetch danh sách file
            if (variables.folderId) {
                queryClient.invalidateQueries({
                    queryKey: fileKeys.folder(workspaceId, variables.folderId),
                })
            } else {
                queryClient.invalidateQueries({
                    queryKey: fileKeys.root(workspaceId),
                })
            }
            toast.success('Upload thành công!')
            setProgress(0)
        },

        onError: () => {
            setProgress(0)
        },
    })

    return { ...mutation, progress }
}

// Download file
export function useDownloadFile(workspaceId: string) {
    return useMutation({
        mutationFn: (fileId: string) =>
            fileApi.getDownloadUrl(workspaceId, fileId),

        onSuccess: (data) => {
            // (6) Mở URL trong tab mới để download
            window.open(data.downloadUrl, '_blank')
        },
    })
}

// Xóa file
export function useDeleteFile(workspaceId: string) {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (fileId: string) =>
            fileApi.delete(workspaceId, fileId),

        onSuccess: () => {
            // Invalidate tất cả file queries trong workspace
            queryClient.invalidateQueries({
                queryKey: fileKeys.workspace(workspaceId),
            })
            toast.success('Đã xóa file!')
        },
    })
}