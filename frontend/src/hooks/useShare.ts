import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { shareApi } from '@/api/share.api'
import { CreateShareLinkRequest, ShareWithUserRequest } from '@/types/share'
import toast from 'react-hot-toast'

export const shareKeys = {
    link: (wId: string, fId: string) =>
        ['share', 'link', wId, fId] as const,
    permissions: (wId: string, fId: string) =>
        ['share', 'permissions', wId, fId] as const,
}

export function useShareLink(workspaceId: string, fileId: string) {
    return useQuery({
        queryKey: shareKeys.link(workspaceId, fileId),
        // (1) getLink đã xử lý 404 → trả null, không throw
        queryFn: () => shareApi.getLink(workspaceId, fileId),
        enabled: !!workspaceId && !!fileId,
        retry: false,
        staleTime: 0,
    })
}

export function useCreateShareLink(workspaceId: string, fileId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (data: CreateShareLinkRequest = {}) =>
            shareApi.createLink(workspaceId, fileId, data),
        onSuccess: (newLink) => {
            // (2) Set trực tiếp vào cache
            queryClient.setQueryData(
                shareKeys.link(workspaceId, fileId),
                newLink
            )
            toast.success('Tạo link chia sẻ thành công!')
        },
        onError: (err: any) => {
            const msg = err?.response?.data?.message || 'Tạo link thất bại'
            toast.error(msg)
        },
    })
}

export function useDeactivateShareLink(workspaceId: string, fileId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: () => shareApi.deactivateLink(workspaceId, fileId),
        onSuccess: () => {
            queryClient.setQueryData(
                shareKeys.link(workspaceId, fileId),
                null
            )
            toast.success('Đã thu hồi link!')
        },
    })
}

export function useFilePermissions(workspaceId: string, fileId: string) {
    return useQuery({
        queryKey: shareKeys.permissions(workspaceId, fileId),
        queryFn: () => shareApi.getPermissions(workspaceId, fileId),
        enabled: !!workspaceId && !!fileId,
        retry: false,
    })
}

export function useShareWithUser(workspaceId: string, fileId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (data: ShareWithUserRequest) =>
            shareApi.shareWithUser(workspaceId, fileId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: shareKeys.permissions(workspaceId, fileId),
            })
            toast.success('Đã chia sẻ file!')
        },
    })
}

export function useRevokePermission(workspaceId: string, fileId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (userId: string) =>
            shareApi.revokePermission(workspaceId, fileId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: shareKeys.permissions(workspaceId, fileId),
            })
            toast.success('Đã thu hồi quyền!')
        },
    })
}