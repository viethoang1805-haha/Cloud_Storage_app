import axios, {
    AxiosError,
    AxiosResponse,
    InternalAxiosRequestConfig,
} from 'axios'
import { tokenService } from '@/services/token.service'
import { ApiError } from '@/types/common'

// (1) Tạo axios instance với base config
const axiosInstance = axios.create({
    baseURL: '/api/v1',         // tất cả request đều có prefix /api/v1
    timeout: 30000,             // 30 giây timeout
    headers: {
        'Content-Type': 'application/json',
    },
})

// =============================================
// (2) REQUEST INTERCEPTOR — thêm token vào mọi request
// =============================================
axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = tokenService.getAccessToken()

        if (token) {
            // Thêm Authorization header
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => Promise.reject(error)
)

// =============================================
// (3) RESPONSE INTERCEPTOR — xử lý refresh token tự động
// =============================================

// Flag tránh nhiều request cùng lúc đều trigger refresh
let isRefreshing = false

// Queue các request đang chờ refresh token
let failedQueue: Array<{
    resolve: (token: string) => void
    reject: (error: unknown) => void
}> = []

// (4) Xử lý queue sau khi refresh xong
const processQueue = (
    error: unknown,
    token: string | null = null
): void => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error)
        } else {
            prom.resolve(token!)
        }
    })
    failedQueue = []
}

axiosInstance.interceptors.response.use(
    // Response thành công — trả về data trực tiếp
    (response: AxiosResponse) => response,

    // Response lỗi
    async (error: AxiosError<ApiError>) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
            _retry?: boolean
        }

        // (5) Chỉ xử lý 401 và request chưa retry
        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = tokenService.getRefreshToken()

            // Nếu không có refresh token → logout luôn
            if (!refreshToken) {
                tokenService.clearAll()
                window.location.href = '/login'
                return Promise.reject(error)
            }

            // (6) Nếu đang refresh → add vào queue chờ
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject })
                }).then((token) => {
                    originalRequest.headers.Authorization = `Bearer ${token}`
                    return axiosInstance(originalRequest)
                }).catch((err) => Promise.reject(err))
            }

            // (7) Bắt đầu refresh
            originalRequest._retry = true
            isRefreshing = true

            try {
                // Gọi trực tiếp axios (không phải instance) để tránh loop interceptor
                const response = await axios.post('/api/v1/auth/refresh', {
                    refreshToken,
                })

                const { accessToken, refreshToken: newRefreshToken } =
                    response.data.data

                // (8) Lưu token mới
                tokenService.setTokens(accessToken, newRefreshToken)

                // Retry tất cả request đang chờ với token mới
                processQueue(null, accessToken)

                // Retry request gốc
                originalRequest.headers.Authorization = `Bearer ${accessToken}`
                return axiosInstance(originalRequest)

            } catch (refreshError) {
                // (9) Refresh thất bại → logout
                processQueue(refreshError, null)
                tokenService.clearAll()
                window.location.href = '/login'
                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        }

        return Promise.reject(error)
    }
)

export default axiosInstance