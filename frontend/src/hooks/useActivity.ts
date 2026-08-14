import { useQuery } from '@tanstack/react-query'
import { activityApi } from '@/api/activity.api'

export const activityKeys = {
    workspace: (wId: string, params: object) =>
        ['activities', 'workspace', wId, params] as const,
    mine: (wId: string) => ['activities', 'mine', wId] as const,
    admin: (params: object) => ['activities', 'admin', params] as const,
}

export function useWorkspaceActivities(
    workspaceId: string,
    params: { page?: number; action?: string } = {}
) {
    return useQuery({
        queryKey: activityKeys.workspace(workspaceId, params),
        queryFn: () => activityApi.getWorkspaceActivities(workspaceId, params),
        enabled: !!workspaceId,
    })
}

export function useMyActivities(workspaceId: string, page = 0) {
    return useQuery({
        queryKey: activityKeys.mine(workspaceId),
        queryFn: () => activityApi.getMyActivities(workspaceId, { page }),
        enabled: !!workspaceId,
    })
}

export function useAdminActivities(
    params: { page?: number; action?: string } = {}
) {
    return useQuery({
        queryKey: activityKeys.admin(params),
        queryFn: () => activityApi.getAllActivities(params),
    })
}