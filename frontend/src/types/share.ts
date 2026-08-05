export interface ShareLink {
    id: string
    token: string
    shareUrl: string
    hasPassword: boolean
    expiresAt: string | null
    isActive: boolean
    downloadCount: number
    maxDownloads: number | null
    createdAt: string
    file: {
        id: string
        originalName: string
        contentType: string
        size: number
        sizeFormatted: string
    }
}

export interface FilePermission {
    id: string
    permission: 'VIEW' | 'EDIT' | 'DOWNLOAD' | 'DELETE'
    isExpired: boolean
    expiresAt: string | null
    createdAt: string
    user: {
        id: string
        fullName: string
        email: string
        avatarUrl: string | null
    }
    sharedBy: {
        id: string
        fullName: string
        email: string
        avatarUrl: string | null
    }
}

export interface CreateShareLinkRequest {
    password?: string
    expiresAt?: string
    maxDownloads?: number
}

export interface ShareWithUserRequest {
    email: string
    permission: 'VIEW' | 'EDIT' | 'DOWNLOAD' | 'DELETE'
    expiresAt?: string
}