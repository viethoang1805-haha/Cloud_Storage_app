// (1) Quản lý token trong localStorage
const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_KEY = 'user_info'

export const tokenService = {

    getAccessToken(): string | null {
        return localStorage.getItem(ACCESS_TOKEN_KEY)
    },

    getRefreshToken(): string | null {
        return localStorage.getItem(REFRESH_TOKEN_KEY)
    },

    // (2) Lưu cả cặp token sau khi login/register
    setTokens(accessToken: string, refreshToken: string): void {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    },

    // (3) Lưu thông tin user để không phải call API mỗi lần reload
    setUser(user: object): void {
        localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    getUser<T>(): T | null {
        const userStr = localStorage.getItem(USER_KEY)
        if (!userStr) return null
        try {
            return JSON.parse(userStr) as T
        } catch {
            return null
        }
    },

    // (4) Xóa tất cả khi logout
    clearAll(): void {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
    },

    // (5) Kiểm tra đã đăng nhập chưa (có token không)
    isAuthenticated(): boolean {
        return !!this.getAccessToken()
    },
}