import { NavLink, useNavigate } from 'react-router-dom'
import {
    LayoutDashboard,
    FolderOpen,
    Users,
    Settings,
    LogOut,
    Cloud,
} from 'lucide-react'
import { useAuthStore } from '@/store/auth.store'
import toast from 'react-hot-toast'
import { cn } from '@/lib/utils'

// (1) Định nghĩa các nav items
const navItems = [
    {
        label: 'Dashboard',
        href: '/dashboard',
        icon: LayoutDashboard,
    },
    {
        label: 'Workspaces',
        href: '/workspaces',
        icon: FolderOpen,
    },
    {
        label: 'Profile',
        href: '/profile',
        icon: Settings,
    },
]

export default function Sidebar() {
    const { user, logout } = useAuthStore()
    const navigate = useNavigate()

    const handleLogout = async () => {
        try {
            await logout()
            navigate('/login')
            toast.success('Đăng xuất thành công')
        } catch {
            // Logout đã clear local state dù có lỗi
            navigate('/login')
        }
    }

    return (
        <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">

            {/* Logo */}
            <div className="flex items-center gap-2 p-6 border-b border-gray-200">
                <Cloud className="h-6 w-6 text-primary" />
                <span className="font-bold text-gray-900">CloudStorage</span>
            </div>

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-1">
                {navItems.map((item) => (
                    <NavLink
                        key={item.href}
                        to={item.href}
                        className={({ isActive }) =>
                            cn(
                                // (2) Base styles
                                'flex items-center gap-3 px-3 py-2 rounded-lg',
                                'text-sm font-medium transition-colors',
                                // (3) Active vs inactive styles
                                isActive
                                    ? 'bg-primary text-white'
                                    : 'text-gray-600 hover:bg-gray-100'
                            )
                        }
                    >
                        <item.icon className="h-4 w-4" />
                        {item.label}
                    </NavLink>
                ))}
            </nav>

            {/* User info + Logout ở dưới cùng */}
            <div className="p-4 border-t border-gray-200">

                {/* Avatar + tên user */}
                <div className="flex items-center gap-3 mb-3 px-3">
                    <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center
                          justify-center text-primary font-semibold text-sm">
                        {/* (4) Hiển thị chữ cái đầu tên nếu không có avatar */}
                        {user?.avatarUrl ? (
                            <img
                                src={user.avatarUrl}
                                alt={user.fullName}
                                className="h-8 w-8 rounded-full object-cover"
                            />
                        ) : (
                            user?.fullName?.charAt(0).toUpperCase()
                        )}
                    </div>
                    <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                            {user?.fullName}
                        </p>
                        <p className="text-xs text-gray-500 truncate">
                            {user?.email}
                        </p>
                    </div>
                </div>

                {/* Logout button */}
                <button
                    onClick={handleLogout}
                    className="flex items-center gap-3 w-full px-3 py-2 rounded-lg
                     text-sm font-medium text-red-600 hover:bg-red-50
                     transition-colors"
                >
                    <LogOut className="h-4 w-4" />
                    Đăng xuất
                </button>
            </div>
        </aside>
    )
}