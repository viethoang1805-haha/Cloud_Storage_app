import { Bell } from 'lucide-react'
import { useAuthStore } from '@/store/auth.store'
import { useQuery } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'

export default function Header() {
    const user = useAuthStore((state) => state.user)

    // (1) Lấy số thông báo chưa đọc
    const { data: unreadCount } = useQuery({
        queryKey: ['notifications', 'unread-count'],
        queryFn: async () => {
            const response = await axiosInstance.get<ApiResponse<number>>(
                '/notifications/unread-count'
            )
            return response.data.data
        },
        // (2) Refetch mỗi 60 giây
        refetchInterval: 60 * 1000,
    })

    return (
        <header className="bg-white border-b border-gray-200 px-6 py-4
                       flex items-center justify-between">

            {/* Breadcrumb hoặc page title — sẽ thêm sau */}
            <div />

            {/* Right side: notifications + user */}
            <div className="flex items-center gap-4">

                {/* Notification bell */}
                <button className="relative p-2 rounded-lg hover:bg-gray-100
                           transition-colors">
                    <Bell className="h-5 w-5 text-gray-600" />
                    {/* (3) Badge số chưa đọc */}
                    {unreadCount && unreadCount > 0 && (
                        <span className="absolute -top-1 -right-1 h-5 w-5 rounded-full
                             bg-red-500 text-white text-xs flex items-center
                             justify-center font-medium">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
                    )}
                </button>

            </div>
        </header>
    )
}