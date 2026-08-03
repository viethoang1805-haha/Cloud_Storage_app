import { useEffect } from 'react'
import { useAuthStore } from '@/store/auth.store'
import AppRoutes from '@/routes/AppRoutes'

export default function App() {
  const initialize = useAuthStore((state) => state.initialize)

  useEffect(() => {
    initialize()
  }, [initialize])

  return <AppRoutes />
}