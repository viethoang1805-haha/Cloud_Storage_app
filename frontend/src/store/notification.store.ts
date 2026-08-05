import { create } from 'zustand'
import { Notification } from '@/types/notification'

interface NotificationState {
    notifications: Notification[]
    unreadCount: number
    addNotification: (notification: Notification) => void
    setUnreadCount: (count: number) => void
    markRead: (id: string) => void
    markAllRead: () => void
}

export const useNotificationStore = create<NotificationState>((set) => ({
    notifications: [],
    unreadCount: 0,

    addNotification: (notification) =>
        set((state) => ({
            notifications: [notification, ...state.notifications].slice(0, 50),
            unreadCount: state.unreadCount + 1,
        })),

    setUnreadCount: (count) => set({ unreadCount: count }),

    markRead: (id) =>
        set((state) => ({
            notifications: state.notifications.map((n) =>
                n.id === id ? { ...n, isRead: true } : n
            ),
            unreadCount: Math.max(0, state.unreadCount - 1),
        })),

    markAllRead: () =>
        set((state) => ({
            notifications: state.notifications.map((n) => ({
                ...n,
                isRead: true,
            })),
            unreadCount: 0,
        })),
}))