import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard,
  FolderOpen,
  User,
  LogOut,
  Cloud,
} from 'lucide-react'
import { useAuthStore } from '@/store/auth.store'
import Avatar from '@/components/common/Avatar'
import { cn } from '@/lib/utils'
import toast from 'react-hot-toast'

const navItems = [
  { to: '/dashboard',   label: 'Dashboard',   icon: LayoutDashboard },
  { to: '/workspaces',  label: 'Workspaces',  icon: FolderOpen },
  { to: '/profile',     label: 'Hồ sơ',      icon: User },
]

export default function Sidebar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await logout()
      navigate('/login')
      toast.success('Đã đăng xuất')
    } catch {
      navigate('/login')
    }
  }

  return (
    <aside className="w-60 h-screen bg-white border-r border-gray-100
                      flex flex-col flex-shrink-0">

      {/* Logo */}
      <div className="h-16 flex items-center gap-2.5 px-5
                      border-b border-gray-100">
        <div className="h-8 w-8 bg-primary-600 rounded-xl
                        flex items-center justify-center">
          <Cloud className="h-4 w-4 text-white" />
        </div>
        <span className="font-bold text-gray-900 text-sm tracking-tight">
          CloudStorage
        </span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 overflow-y-auto">
        <ul className="space-y-0.5">
          {navItems.map(({ to, label, icon: Icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3 py-2.5 rounded-xl',
                    'text-sm font-medium transition-all duration-150',
                    isActive
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon className={cn(
                      'h-4 w-4 flex-shrink-0',
                      isActive ? 'text-primary-600' : 'text-gray-400'
                    )} />
                    {label}
                  </>
                )}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      {/* User section */}
      <div className="px-3 py-4 border-t border-gray-100">
        {user && (
          <div className="flex items-center gap-3 px-3 py-2.5 mb-1
                          rounded-xl hover:bg-gray-50 transition-colors">
            <Avatar
              name={user.fullName}
              avatarUrl={user.avatarUrl}
              size="sm"
            />
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-gray-900 truncate">
                {user.fullName}
              </p>
              <p className="text-xs text-gray-400 truncate">
                {user.email}
              </p>
            </div>
          </div>
        )}

        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5
                     rounded-xl text-sm font-medium text-gray-500
                     hover:bg-red-50 hover:text-red-600
                     transition-all duration-150"
        >
          <LogOut className="h-4 w-4" />
          Đăng xuất
        </button>
      </div>
    </aside>
  )
}