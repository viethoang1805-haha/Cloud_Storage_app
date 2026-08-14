import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import {
    LayoutDashboard,
    FolderOpen,
    User,
    LogOut,
    Cloud,
    ChevronDown,
    ChevronRight, Users, Activity,
} from 'lucide-react'
import { useAuthStore } from '@/store/auth.store'
import { useWorkspaces } from '@/hooks/useWorkspace'
import Avatar from '@/components/common/Avatar'
import { cn } from '@/lib/utils'
import toast from 'react-hot-toast'
import { useState } from 'react'

export default function Sidebar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const { data: workspaces = [] } = useWorkspaces()
  const [wsExpanded, setWsExpanded] = useState(true)

  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

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
                      flex flex-col flex-shrink-0 overflow-hidden">

        {/* Logo */}
        <div className="h-16 flex items-center gap-2.5 px-5
                      border-b border-gray-100 flex-shrink-0">
          <div className="h-8 w-8 bg-primary-600 rounded-xl
                        flex items-center justify-center">
            <Cloud className="h-4 w-4 text-white" />
          </div>
          <span className="font-bold text-gray-900 text-sm tracking-tight">
          CloudStorage
        </span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 overflow-y-auto space-y-0.5">

          {/* Dashboard */}
          <NavLink
              to="/dashboard"
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
                  <LayoutDashboard className={cn(
                      'h-4 w-4 flex-shrink-0',
                      isActive ? 'text-primary-600' : 'text-gray-400'
                  )} />
                  Dashboard
                </>
            )}
          </NavLink>

          {/* Workspaces section */}
          <div className="pt-2">
            <button
                onClick={() => setWsExpanded(!wsExpanded)}
                className="w-full flex items-center justify-between
                       px-3 py-1.5 text-xs font-semibold text-gray-400
                       uppercase tracking-wide hover:text-gray-600
                       transition-colors"
            >
              Workspaces
              {wsExpanded
                  ? <ChevronDown className="h-3 w-3" />
                  : <ChevronRight className="h-3 w-3" />}
            </button>

            {wsExpanded && (
                <div className="mt-1 space-y-0.5">
                  {/* All workspaces link */}
                  <NavLink
                      to="/workspaces"
                      end
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
                          <FolderOpen className={cn(
                              'h-4 w-4 flex-shrink-0',
                              isActive ? 'text-primary-600' : 'text-gray-400'
                          )} />
                          Tất cả workspace
                        </>
                    )}
                  </NavLink>
                  {/* Individual workspaces */}
                  {workspaces.slice(0, 5).map((ws) => {
                    const isActive = location.pathname.startsWith(
                        `/workspaces/${ws.id}`
                    )
                    return (
                        <button
                            key={ws.id}
                            onClick={() =>
                                navigate(`/workspaces/${ws.id}/files`)
                            }
                            className={cn(
                                'w-full flex items-center gap-2.5 px-3 py-2 rounded-xl',
                                'text-sm transition-all duration-150 text-left',
                                isActive
                                    ? 'bg-primary-50 text-primary-700 font-medium'
                                    : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'
                            )}
                        >
                          <div className={cn(
                              'h-5 w-5 rounded flex items-center justify-center',
                              'text-xs font-bold flex-shrink-0',
                              isActive
                                  ? 'bg-primary-100 text-primary-700'
                                  : 'bg-gray-100 text-gray-500'
                          )}>
                            {ws.name.charAt(0).toUpperCase()}
                          </div>
                          <span className="truncate">{ws.name}</span>
                        </button>

                    )
                  })}
                </div>
            )}
          </div>

          {/* Profile */}
          <div className="pt-2">
            <NavLink
                to="/profile"
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
                    <User className={cn(
                        'h-4 w-4 flex-shrink-0',
                        isActive ? 'text-primary-600' : 'text-gray-400'
                    )} />
                    Hồ sơ
                  </>
              )}
            </NavLink>
          </div>
        </nav>
          {isAdmin && (
              <div className="pt-2">
                  <p className="px-3 py-1.5 text-xs font-semibold text-gray-400
                  uppercase tracking-wide">
                      Quản trị
                  </p>
                  <NavLink
                      to="/admin"
                      end
                      className={({ isActive }) =>
                          cn(
                              'flex items-center gap-3 px-3 py-2.5 rounded-xl',
                              'text-sm font-medium transition-all duration-150',
                              isActive
                                  ? 'bg-purple-50 text-purple-700'
                                  : 'text-gray-600 hover:bg-gray-50'
                          )
                      }
                  >
                      {({ isActive }) => (
                          <>
                              <Activity className={cn(
                                  'h-4 w-4 flex-shrink-0',
                                  isActive ? 'text-purple-600' : 'text-gray-400'
                              )} />
                              Admin Dashboard
                          </>
                      )}
                  </NavLink>
                  <NavLink
                      to="/admin/users"
                      className={({ isActive }) =>
                          cn(
                              'flex items-center gap-3 px-3 py-2.5 rounded-xl',
                              'text-sm font-medium transition-all duration-150',
                              isActive
                                  ? 'bg-purple-50 text-purple-700'
                                  : 'text-gray-600 hover:bg-gray-50'
                          )
                      }
                  >
                      {({ isActive }) => (
                          <>
                              <Users className={cn(
                                  'h-4 w-4 flex-shrink-0',
                                  isActive ? 'text-purple-600' : 'text-gray-400'
                              )} />
                              Người dùng
                          </>
                      )}
                  </NavLink>
              </div>
          )}

        {/* User info + Logout */}
        <div className="px-3 py-4 border-t border-gray-100 flex-shrink-0">
          {user && (
              <div className="flex items-center gap-3 px-3 py-2.5 mb-1
                          rounded-xl">
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
                {isAdmin && (
                    <span className="badge-purple text-xs">Admin</span>
                )}
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