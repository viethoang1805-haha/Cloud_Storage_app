import { useEffect } from 'react'
import { useAuthStore } from '@/store/auth.store'
import AppRoutes from '@/routes/AppRoutes'

function App() {
  const initialize = useAuthStore((state) => state.initialize)

  // (1) Khởi tạo auth state từ localStorage khi app mount
  useEffect(() => {
    initialize()
  }, [initialize])

  return <AppRoutes />
}

export default App