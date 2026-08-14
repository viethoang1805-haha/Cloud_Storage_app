// src/components/layout/Header.tsx
import { Bell } from 'lucide-react'
import { useUnreadCount } from '@/hooks/useNotification'
import { useState } from 'react'
import NotificationPanel from '@/components/notification/NotificationPanel'

export default function Header() {
    const { data: unreadCount = 0 } = useUnreadCount()
    const [showNotif, setShowNotif] = useState(false)

    return (
        <header className="h-16 bg-white border-b border-gray-100
                       px-6 flex items-center justify-end
                       flex-shrink-0">
            <div className="relative">
                <button
                    onClick={() => setShowNotif(!showNotif)}
                    className="relative h-9 w-9 flex items-center justify-center
                     rounded-xl text-gray-500
                     hover:bg-gray-100 hover:text-gray-700
                     transition-all duration-150"
                >
                    <Bell className="h-5 w-5" />
                    {/* (1) Badge hiển thị số chưa đọc */}
                    {unreadCount > 0 && (
                        <span className="absolute -top-0.5 -right-0.5 h-5 min-w-5
                             px-1 rounded-full bg-red-500 text-white
                             text-xs font-bold flex items-center
                             justify-center leading-none">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
                    )}
                </button>

                {showNotif && (
                    <>
                        <div
                            className="fixed inset-0 z-10"
                            onClick={() => setShowNotif(false)}
                        />
                        <div className="absolute right-0 top-11 z-20 w-80
                            bg-white rounded-2xl shadow-xl
                            border border-gray-100 overflow-hidden">
                            <NotificationPanel onClose={() => setShowNotif(false)} />
                        </div>
                    </>
                )}
            </div>
        </header>
    )
}