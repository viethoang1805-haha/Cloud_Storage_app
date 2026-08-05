export interface UserProfile {
    id: string
    email: string
    fullName: string
    avatarUrl: string | null
    isEnabled: boolean
    roles: string[]
    createdAt: string
    storage: StorageInfo
}

export interface StorageInfo {
    usedBytes: number
    limitBytes: number
    usedFormatted: string
    limitFormatted: string
    usedPercent: number
}

export interface UpdateProfileRequest {
    fullName: string
}

export interface ChangePasswordRequest {
    currentPassword: string
    newPassword: string
    confirmPassword: string
}