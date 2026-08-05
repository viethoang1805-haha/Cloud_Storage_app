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

    setTokens(accessToken: string, refreshToken: string): void {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    },

    setUser(user: object): void {
        localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    getUser<T>(): T | null {
        const str = localStorage.getItem(USER_KEY)
        if (!str) return null
        try {
            return JSON.parse(str) as T
        } catch {
            return null
        }
    },

    clearAll(): void {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
    },

    isAuthenticated(): boolean {
        return !!this.getAccessToken()
    },
}