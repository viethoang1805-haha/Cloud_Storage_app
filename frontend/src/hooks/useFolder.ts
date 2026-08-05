import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { folderApi } from '@/api/folder.api'
import { FolderCreateRequest } from '@/types/folder'
import toast from 'react-hot-toast'

export const folderKeys = {
    all: ['folders'] as const,
    workspace: (wId: string) => [...folderKeys.all, wId] as const,
    root: (wId: string) => [...folderKeys.workspace(wId), 'root'] as const,
    detail: (wId: string, fId: string) =>
        [...folderKeys.workspace(wId), fId] as const,
}

export function useRootFolders(workspaceId: string) {
    return useQuery({
        queryKey: folderKeys.root(workspaceId),
        queryFn: () => folderApi.getRootFolders(workspaceId),
        enabled: !!workspaceId,
    })
}

export function useCreateFolder(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (data: FolderCreateRequest) =>
            folderApi.create(workspaceId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: folderKeys.workspace(workspaceId),
            })
            toast.success('Tạo thư mục thành công!')
        },
    })
}

export function useRenameFolder(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: ({ folderId, name }: { folderId: string; name: string }) =>
            folderApi.rename(workspaceId, folderId, { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: folderKeys.workspace(workspaceId),
            })
            toast.success('Đổi tên thành công!')
        },
    })
}

export function useDeleteFolder(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (folderId: string) =>
            folderApi.delete(workspaceId, folderId),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: folderKeys.workspace(workspaceId),
            })
            toast.success('Đã xóa thư mục!')
        },
    })
}