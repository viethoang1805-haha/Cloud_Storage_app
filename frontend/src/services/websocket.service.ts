// src/services/websocket.service.ts

import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { tokenService } from './token.service'
import { Notification } from '@/types/notification'

type NotificationHandler = (notification: Notification) => void

class WebSocketService {
    private client: Client | null = null
    private handlers: NotificationHandler[] = []
    private currentEmail: string | null = null

    // (1) Nhận email thay vì userId
    connect(email: string): void {
        if (this.currentEmail === email && this.client?.active) return

        if (this.client) {
            this.client.deactivate()
            this.client = null
        }

        const token = tokenService.getAccessToken()
        if (!token || !email) return

        this.currentEmail = email

        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },

            onConnect: () => {
                console.log('✅ WebSocket connected')

                // (2) Subscribe bằng email — khớp với Spring principal
                this.client?.subscribe(
                    `/user/${email}/queue/notifications`,
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
                this.currentEmail = null
            },

            onStompError: (frame) => {
                console.error('STOMP error:', frame.headers?.message)
            },

            reconnectDelay: 0,
        })

        this.client.activate()
    }

    disconnect(): void {
        if (this.client) {
            this.client.deactivate()
            this.client = null
        }
        this.currentEmail = null
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