import { QueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { AxiosError } from 'axios'
import { ApiError } from '@/types/common'

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            // (1) Cache 5 phút trước khi refetch
            staleTime: 5 * 60 * 1000,

            // (2) Tự động refetch khi focus lại tab
            refetchOnWindowFocus: false,

            // (3) Retry 1 lần nếu fail (trừ 4xx errors)
            retry: (failureCount, error) => {
                const axiosError = error as AxiosError<ApiError>
                // Không retry nếu là lỗi client (4xx)
                if (axiosError.response?.status &&
                    axiosError.response.status >= 400 &&
                    axiosError.response.status < 500) {
                    return false
                }
                return failureCount < 1
            },
        },
        mutations: {
            // (4) Hiển thị lỗi từ mutation tự động
            onError: (error) => {
                const axiosError = error as AxiosError<ApiError>
                const message = axiosError.response?.data?.message
                    || 'Có lỗi xảy ra, vui lòng thử lại'
                toast.error(message)
            },
        },
    },
})