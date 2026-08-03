import { QueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { AxiosError } from 'axios'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        const status = (error as AxiosError)?.response?.status
        if (status && status >= 400 && status < 500) return false
        return failureCount < 1
      },
    },
    mutations: {
      onError: (error) => {
        const message =
          (error as AxiosError<{ message: string }>)?.response?.data?.message
          || 'Có lỗi xảy ra, vui lòng thử lại'
        toast.error(message)
      },
    },
  },
})