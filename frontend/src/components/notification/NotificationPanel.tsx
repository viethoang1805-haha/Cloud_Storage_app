import { Bell, Check, CheckCheck, X } from 'lucide-react'
import {
    useNotifications,
    useMarkAsRead,
    useMarkAllAsRead,
} from '@/hooks/useNotification'
import { formatRelativeTime } from '@/lib/utils'
import Loading from '@/components/common/Loading'

interface NotificationPanelProps {
    onClose: () => void
}

export default function NotificationPanel({ onClose }: NotificationPanelProps) {
    // (1) Gọi đúng — chỉ 2 args hoặc không cần arg vì có default
    const { data, isLoading } = useNotifications(0, false)
    const markAsRead = useMarkAsRead()
    const markAllAsRead = useMarkAllAsRead()

    const notifications = data?.notifications ?? []
    const unreadCount = data?.unreadCount ?? 0

    return (
        <div className="flex flex-col max-h-[480px]">

            {/* Header */}
            <div className="flex items-center justify-between
                      px-4 py-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                    <Bell className="h-4 w-4 text-gray-500" />
                    <span className="text-sm font-semibold text-gray-900">
            Thông báo
          </span>
                    {unreadCount > 0 && (
                        <span className="badge-blue px-1.5 py-0.5 text-xs rounded-full">
              {unreadCount}
            </span>
                    )}
                </div>

                <div className="flex items-center gap-1">
                    {unreadCount > 0 && (
                        <button
                            onClick={() => markAllAsRead.mutate()}
                            className="p-1.5 rounded-lg text-gray-400
                         hover:text-gray-600 hover:bg-gray-100
                         transition-colors"
                            title="Đánh dấu tất cả đã đọc"
                        >
                            <CheckCheck className="h-4 w-4" />
                        </button>
                    )}
                    <button
                        onClick={onClose}
                        className="p-1.5 rounded-lg text-gray-400
                       hover:text-gray-600 hover:bg-gray-100
                       transition-colors"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>
            </div>

            {/* Body */}
            <div className="overflow-y-auto flex-1">
                {isLoading ? (
                    <Loading size="sm" text="Đang tải..." className="py-8" />
                ) : notifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center
                          py-10">
                        <Bell className="h-8 w-8 text-gray-200 mb-2" />
                        <p className="text-sm text-gray-400">
                            Chưa có thông báo nào
                        </p>
                    </div>
                ) : (
                    <ul className="divide-y divide-gray-50">
                        {notifications.map((notif) => (
                            <li
                                key={notif.id}
                                className={`px-4 py-3 hover:bg-gray-50
                            transition-colors cursor-pointer
                            ${!notif.isRead ? 'bg-blue-50/40' : ''}`}
                                onClick={() => {
                                    if (!notif.isRead) markAsRead.mutate(notif.id)
                                }}
                            >
                                <div className="flex items-start gap-3">
                                    {/* Dot */}
                                    <div className={`mt-1.5 h-2 w-2 rounded-full flex-shrink-0 ${
                                        !notif.isRead ? 'bg-primary-500' : 'bg-gray-200'
                                    }`} />

                                    <div className="flex-1 min-w-0">
                                        <p className={`text-xs font-medium truncate ${
                                            !notif.isRead ? 'text-gray-900' : 'text-gray-600'
                                        }`}>
                                            {notif.title}
                                        </p>
                                        <p className="text-xs text-gray-400 mt-0.5 line-clamp-2">
                                            {notif.message}
                                        </p>
                                        <p className="text-xs text-gray-300 mt-1">
                                            {formatRelativeTime(notif.createdAt)}
                                        </p>
                                    </div>

                                    {!notif.isRead && (
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation()
                                                markAsRead.mutate(notif.id)
                                            }}
                                            className="flex-shrink-0 p-1 rounded
                                 text-gray-300 hover:text-green-500
                                 transition-colors"
                                            title="Đánh dấu đã đọc"
                                        >
                                            <Check className="h-3.5 w-3.5" />
                                        </button>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    )
}