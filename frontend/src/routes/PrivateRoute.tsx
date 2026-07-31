import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

export default function PrivateRoute() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
    const location = useLocation()

    if (!isAuthenticated) {
        // (1) Lưu lại URL hiện tại để sau login redirect đúng chỗ
        return (
            <Navigate
                to="/login"
                state={{ from: location }}  // (2) truyền location vào state
                replace
            />
        )
    }

    return <Outlet />
}