export interface Notification {
    id: string
    title: string
    message: string
    type: string
    isRead: boolean
    readAt: string | null
    refType: string | null
    refId: string | null
    createdAt: string
}

export interface NotificationPageResponse {
    notifications: Notification[]
    unreadCount: number
    currentPage: number
    totalPages: number
    totalElements: number
    hasNext: boolean
}