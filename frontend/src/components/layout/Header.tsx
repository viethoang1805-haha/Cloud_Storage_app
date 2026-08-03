import { Bell } from 'lucide-react'
import { useUnreadCount } from '@/hooks/useNotification'
import { useState } from 'react'
import NotificationPanel from '@/components/notification/NotificationPanel'

export default function Header() {
  const { data: unreadCount = 0 } = useUnreadCount()
  const [showNotif, setShowNotif] = useState(false)

  return (
    <header className="h-16 bg-white border-b border-gray-100
                       px-6 flex items-center justify-between
                       flex-shrink-0">

      <div />

      <div className="flex items-center gap-2">
        {/* Notification bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotif(!showNotif)}
            className="relative h-9 w-9 flex items-center justify-center
                       rounded-xl text-gray-500
                       hover:bg-gray-100 hover:text-gray-700
                       transition-all duration-150"
          >
            <Bell className="h-4 w-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 h-2 w-2
                               bg-red-500 rounded-full" />
            )}
          </button>

          {/* Notification dropdown */}
          {showNotif && (
            <>
              <div
                className="fixed inset-0 z-10"
                onClick={() => setShowNotif(false)}
              />
              <div className="absolute right-0 top-11 z-20
                              w-80 bg-white rounded-2xl shadow-xl
                              border border-gray-100 overflow-hidden">
                <NotificationPanel
                  onClose={() => setShowNotif(false)}
                />
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  )
}