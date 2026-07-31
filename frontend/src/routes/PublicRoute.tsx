import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

export default function PublicRoute() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated)

    // (1) Đã đăng nhập → redirect về dashboard
    if (isAuthenticated) {
        return <Navigate to="/dashboard" replace />
    }

    return <Outlet />
}