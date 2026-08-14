import { useEffect } from 'react'
import { useAuthStore } from '@/store/auth.store'
import AppRoutes from '@/routes/AppRoutes'
import { useWebSocket } from '@/hooks/useSocket'

// (1) Component riêng để dùng hook
function WebSocketProvider() {
  useWebSocket()
  return null
}

export default function App() {
  const initialize = useAuthStore((state) => state.initialize)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)

  useEffect(() => {
    initialize()
  }, [initialize])

  return (
      <>
        {/* (2) Chỉ mount WebSocket khi đã đăng nhập */}
        {isAuthenticated && <WebSocketProvider />}
        <AppRoutes />
      </>
  )
}