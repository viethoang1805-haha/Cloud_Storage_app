import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { memberApi, MemberInviteRequest } from '@/api/member.api'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

export const memberKeys = {
    all: ['members'] as const,
    workspace: (wId: string) => [...memberKeys.all, wId] as const,
}

export function useMembers(workspaceId: string) {
    return useQuery({
        queryKey: memberKeys.workspace(workspaceId),
        queryFn: () => memberApi.getMembers(workspaceId),
        enabled: !!workspaceId,
    })
}

export function useInviteMember(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (data: MemberInviteRequest) =>
            memberApi.invite(workspaceId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: memberKeys.workspace(workspaceId),
            })
            toast.success('Đã mời thành viên!')
        },
    })
}

export function useUpdateMemberRole(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: ({ userId, role }: { userId: string; role: string }) =>
            memberApi.updateRole(workspaceId, userId, role),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: memberKeys.workspace(workspaceId),
            })
            toast.success('Đã cập nhật vai trò!')
        },
    })
}

export function useRemoveMember(workspaceId: string) {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: (userId: string) => memberApi.remove(workspaceId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: memberKeys.workspace(workspaceId),
            })
            toast.success('Đã xóa thành viên!')
        },
    })
}

export function useLeaveWorkspace(workspaceId: string) {
    const queryClient = useQueryClient()
    const navigate = useNavigate()
    return useMutation({
        mutationFn: () => memberApi.leave(workspaceId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['workspaces'] })
            toast.success('Đã rời workspace!')
            navigate('/workspaces')
        },
    })
}