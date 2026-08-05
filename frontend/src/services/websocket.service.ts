import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { tokenService } from './token.service'
import { Notification } from '@/types/notification'

type NotificationHandler = (notification: Notification) => void

class WebSocketService {
    private client: Client | null = null
    private handlers: NotificationHandler[] = []

    connect(userId: string): void {
        // Disconnect cũ nếu có
        if (this.client?.active) {
            this.client.deactivate()
        }

        const token = tokenService.getAccessToken()
        if (!token) return

        this.client = new Client({
            // (1) Dùng factory function để SockJS tạo mới mỗi lần reconnect
            webSocketFactory: () => {
                return new SockJS('http://localhost:8080/ws')
            },

            // (2) Token trong STOMP CONNECT header
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },

            onConnect: (frame) => {
                console.log('✅ WebSocket connected', frame)

                // (3) Subscribe channel riêng của user
                this.client?.subscribe(
                    `/user/${userId}/queue/notifications`,
                    (message: IMessage) => {
                        try {
                            const notification: Notification = JSON.parse(message.body)
                            this.handlers.forEach((h) => h(notification))
                        } catch (e) {
                            console.error('Parse notification error:', e)
                        }
                    }
                )
            },

            onDisconnect: () => {
                console.log('WebSocket disconnected')
            },

            onStompError: (frame) => {
                console.error('STOMP error:', frame.headers?.message)
            },

            // (4) Không reconnect tự động nếu bị 401
            reconnectDelay: 0,
        })

        this.client.activate()
    }

    disconnect(): void {
        this.client?.deactivate()
        this.client = null
        this.handlers = []
    }

    onNotification(handler: NotificationHandler): () => void {
        this.handlers.push(handler)
        return () => {
            this.handlers = this.handlers.filter((h) => h !== handler)
        }
    }
}

export const wsService = new WebSocketService()