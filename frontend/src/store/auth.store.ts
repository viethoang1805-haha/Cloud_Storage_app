import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest } from '@/types/auth'
import { authApi } from '@/api/auth.api'
import { tokenService } from '@/services/token.service'

// (1) Định nghĩa shape của store
interface AuthState {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean

    // Actions
    login: (data: LoginRequest) => Promise<void>
    register: (data: RegisterRequest) => Promise<void>
    logout: () => Promise<void>
    setUser: (user: User) => void
    initialize: () => void
}

export const useAuthStore = create<AuthState>()(
    // (2) devtools — hiển thị state trong Redux DevTools
    devtools(
        // (3) persist — lưu một phần state vào localStorage
        persist(
            (set, get) => ({
                user: null,
                isAuthenticated: false,
                isLoading: false,

                // =============================================
                // ĐĂNG NHẬP
                // =============================================
                login: async (data: LoginRequest) => {
                    set({ isLoading: true })
                    try {
                        const response = await authApi.login(data)

                        // (4) Lưu token và user
                        tokenService.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )
                        tokenService.setUser(response.user)

                        set({
                            user: response.user,
                            isAuthenticated: true,
                            isLoading: false,
                        })
                    } catch (error) {
                        set({ isLoading: false })
                        throw error  // (5) Rethrow để component xử lý
                    }
                },

                // =============================================
                // ĐĂNG KÝ
                // =============================================
                register: async (data: RegisterRequest) => {
                    set({ isLoading: true })
                    try {
                        const response = await authApi.register(data)

                        tokenService.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )
                        tokenService.setUser(response.user)

                        set({
                            user: response.user,
                            isAuthenticated: true,
                            isLoading: false,
                        })
                    } catch (error) {
                        set({ isLoading: false })
                        throw error
                    }
                },

                // =============================================
                // ĐĂNG XUẤT
                // =============================================
                logout: async () => {
                    try {
                        const refreshToken = tokenService.getRefreshToken()
                        if (refreshToken) {
                            // (6) Gọi API logout — có thể fail nếu token đã hết hạn
                            await authApi.logout(refreshToken).catch(() => {
                                // Bỏ qua lỗi — vẫn logout ở client
                            })
                        }
                    } finally {
                        // (7) Luôn clear state dù API có fail
                        tokenService.clearAll()
                        set({ user: null, isAuthenticated: false })
                    }
                },

                // Update user info (sau khi edit profile)
                setUser: (user: User) => {
                    tokenService.setUser(user)
                    set({ user })
                },

                // =============================================
                // INITIALIZE — gọi khi app khởi động
                // =============================================
                initialize: () => {
                    // (8) Đọc từ localStorage khi reload trang
                    const token = tokenService.getAccessToken()
                    const user = tokenService.getUser<User>()

                    if (token && user) {
                        set({ user, isAuthenticated: true })
                    } else {
                        set({ user: null, isAuthenticated: false })
                    }
                },
            }),
            {
                name: 'auth-storage',     // (9) key trong localStorage
                // (10) Chỉ persist user và isAuthenticated
                // Không persist isLoading
                partialize: (state) => ({
                    user: state.user,
                    isAuthenticated: state.isAuthenticated,
                }),
            }
        ),
        { name: 'AuthStore' }
    )
)