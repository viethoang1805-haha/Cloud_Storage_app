import { useEffect } from 'react'
import { useAuthStore } from '@/store/auth.store'
import AppRoutes from '@/routes/AppRoutes'
import { useWebSocket } from '@/hooks/useSocket'

export default function App() {
  const initialize = useAuthStore(state => state.initialize)

  useEffect(() => {
    initialize()
  }, [initialize])

  // Gọi hook WebSocket
  useWebSocket()

  return <AppRoutes />
}