import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { tokenService } from './token.service'
import { Notification } from '@/types/notification'

type NotificationHandler = (notification: Notification) => void

class WebSocketService {
    private client: Client | null = null
    private handlers: NotificationHandler[] = []

    // (1) Kết nối WebSocket
    connect(userId: string): void {
        if (this.client?.active) return  // Đã kết nối rồi

        const token = tokenService.getAccessToken()
        if (!token) return

        this.client = new Client({
            webSocketFactory: () =>
                new SockJS('http://localhost:8080/ws'),

            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },

            onConnect: () => {
                console.log('✅ WebSocket connected')

                // (2) Subscribe nhận notification cá nhân
                this.client?.subscribe(
                    `/user/${userId}/queue/notifications`,
                    (message: IMessage) => {
                        try {
                            const notification: Notification = JSON.parse(message.body)
                            // (3) Gọi tất cả handler đã đăng ký
                            this.handlers.forEach((handler) => handler(notification))
                        } catch (error) {
                            console.error('Parse notification error:', error)
                        }
                    }
                )
            },

            onDisconnect: () => {
                console.log('❌ WebSocket disconnected')
            },

            onStompError: (frame) => {
                console.error('STOMP error:', frame)
            },

            reconnectDelay: 5000,    // Retry sau 5 giây
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        })

        this.client.activate()
    }

    // (4) Ngắt kết nối
    disconnect(): void {
        this.client?.deactivate()
        this.client = null
        this.handlers = []
    }

    // (5) Đăng ký handler nhận notification
    onNotification(handler: NotificationHandler): () => void {
        this.handlers.push(handler)

        // (6) Trả về unsubscribe function
        return () => {
            this.handlers = this.handlers.filter((h) => h !== handler)
        }
    }
}

// (7) Singleton — chỉ 1 instance cho toàn app
export const wsService = new WebSocketService()