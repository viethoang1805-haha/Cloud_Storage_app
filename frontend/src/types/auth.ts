// (1) Các type dùng cho Authentication
export interface User {
    id: string
    email: string
    fullName: string
    avatarUrl: string | null
    roles: string[]
}

export interface TokenResponse {
    accessToken: string
    refreshToken: string
    tokenType: string
    expiresIn: number   // giây
    user: User
}

export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    email: string
    password: string
    fullName: string
}

export interface RefreshTokenRequest {
    refreshToken: string
}