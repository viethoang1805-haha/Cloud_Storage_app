import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { folderApi } from '@/api/folder.api'
import { FolderCreateRequest } from '@/types/folder'
import toast from 'react-hot-toast'

export const folderKeys = {
    all: ['folders'] as const,
    workspace: (wId: string) => [...folderKeys.all, wId] as const,
    root: (wId: string) => [...folderKeys.workspace(wId), 'root'] as const,
    children: (wId: string, fId: string) =>
        [...folderKeys.workspace(wId), fId, 'children'] as const,
    detail: (wId: string, fId: string) =>
        [...folderKeys.workspace(wId), fId] as const,
}

export function useRootFolders(workspaceId: string) {
    return useQuery({
        queryKey: folderKeys.root(workspaceId),
        queryFn: () => folderApi.getRootFolders(workspaceId),
        enabled: !!workspaceId,
        staleTime: 30 * 1000,
    })
}

// (1) Hook riêng cho folder children
export function useFolderChildren(workspaceId: string, folderId: string, enabled: boolean) {
    return useQuery({
        queryKey: folderKeys.children(workspaceId, folderId),
        queryFn: () => folderApi.getChildren(workspaceId, folderId),
        enabled: enabled && !!workspaceId && !!folderId,
        staleTime: 30 * 1000,
    })
}

export function useCreateFolder(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (data: FolderCreateRequest) =>
            folderApi.create(workspaceId, data),
        onSuccess: (newFolder) => {
            // (2) Invalidate đúng query key
            if (newFolder.parent) {
                queryClient.invalidateQueries({
                    queryKey: folderKeys.children(workspaceId, newFolder.parent.id),
                })
            } else {
                queryClient.invalidateQueries({
                    queryKey: folderKeys.root(workspaceId),
                })
            }
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