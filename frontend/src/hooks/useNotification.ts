import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationApi } from '@/api/notification.api'

export const notifKeys = {
    all: ['notifications'] as const,
    list: (page: number, unreadOnly: boolean) =>
        [...notifKeys.all, 'list', page, unreadOnly] as const,
    unreadCount: () => [...notifKeys.all, 'unread-count'] as const,
}

export function useNotifications(page = 0, unreadOnly = false) {
    return useQuery({
        queryKey: notifKeys.list(page, unreadOnly),
        queryFn: () => notificationApi.getAll(page, 20, unreadOnly),
    })
}

export function useUnreadCount() {
    return useQuery({
        queryKey: notifKeys.unreadCount(),
        queryFn: notificationApi.getUnreadCount,
        refetchInterval: 60 * 1000,  // Poll mỗi 60 giây
    })
}

export function useMarkAsRead() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (id: string) => notificationApi.markAsRead(id),
        onSuccess: () => {
            // Refetch count và list
            queryClient.invalidateQueries({ queryKey: notifKeys.all })
        },
    })
}

export function useMarkAllAsRead() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: notificationApi.markAllAsRead,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: notifKeys.all })
        },
    })
}