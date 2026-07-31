import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/auth.store'
import { wsService } from '@/services/websocket.service'
import { notifKeys } from './useNotification'
import toast from 'react-hot-toast'
import { Bell } from 'lucide-react'

// (1) Hook kết nối WebSocket và sync với TanStack Query
export function useWebSocket() {
    const user = useAuthStore((state) => state.user)
    const queryClient = useQueryClient()

    useEffect(() => {
        if (!user) return

        // Kết nối
        wsService.connect(user.id)

        // (2) Đăng ký handler — cập nhật cache khi có notification mới
        const unsubscribe = wsService.onNotification((notification) => {
            // Invalidate unread count → Header badge tự cập nhật
            queryClient.invalidateQueries({
                queryKey: notifKeys.unreadCount(),
            })

            // Invalidate notification list
            queryClient.invalidateQueries({
                queryKey: notifKeys.all,
            })

            // (3) Hiển thị toast notification
            toast(notification.title, {
                icon: '🔔',
                duration: 5000,
            })
        })

        // (4) Cleanup khi unmount hoặc user thay đổi
        return () => {
            unsubscribe()
            wsService.disconnect()
        }
    }, [user, queryClient])
}