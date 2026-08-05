import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

export default function AdminRoute() {
    const user = useAuthStore((state) => state.user)
    const isAdmin = user?.roles?.includes('ROLE_ADMIN')

    if (!isAdmin) {
        return <Navigate to="/dashboard" replace />
    }

    return <Outlet />
}