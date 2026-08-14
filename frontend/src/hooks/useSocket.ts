// src/hooks/useSocket.ts

import { useEffect, useRef } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/auth.store'
import { wsService } from '@/services/websocket.service'
import { notifKeys } from './useNotification'
import toast from 'react-hot-toast'

export function useWebSocket() {
    const user = useAuthStore((state) => state.user)
    const queryClient = useQueryClient()
    const subscribedRef = useRef(false)

    useEffect(() => {
        // (1) Dùng email thay vì user.id
        if (!user?.email) return

        wsService.connect(user.email)   // truyền email

        if (!subscribedRef.current) {
            subscribedRef.current = true

            const unsubscribe = wsService.onNotification((notification) => {
                // Cập nhật badge count
                queryClient.invalidateQueries({
                    queryKey: notifKeys.unreadCount(),
                })
                // Cập nhật list
                queryClient.invalidateQueries({
                    queryKey: notifKeys.all,
                })
                // Toast
                toast(notification.title, {
                    icon: '🔔',
                    duration: 5000,
                    style: {
                        background: '#fff',
                        border: '1px solid #e5e7eb',
                        borderRadius: '12px',
                        fontSize: '14px',
                    },
                })
            })

            return () => {
                subscribedRef.current = false
                unsubscribe()
                wsService.disconnect()
            }
        }
    }, [user?.email])  // (2) depend on email
}