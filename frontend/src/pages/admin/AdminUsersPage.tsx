import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import Loading from '@/components/common/Loading'
import Avatar from '@/components/common/Avatar'
import { RoleBadge } from '@/components/common/Badge'
import { Users, Search, Lock, Unlock } from 'lucide-react'
import { formatDate, cn } from '@/lib/utils'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import toast from 'react-hot-toast'

interface AdminUser {
    id: string
    email: string
    fullName: string
    avatarUrl: string | null
    isEnabled: boolean
    roles: string[]
    createdAt: string
    storage: {
        usedBytes: number
        limitBytes: number
        usedFormatted: string
        usedPercent: number
    }
}

export default function AdminUsersPage() {
    const queryClient = useQueryClient()
    const [search, setSearch] = useState('')
    const [toggleTarget, setToggleTarget] = useState<AdminUser | null>(null)

    const { data: users = [], isLoading } = useQuery({
        queryKey: ['admin', 'users'],
        queryFn: async () => {
            const res = await axiosInstance.get<ApiResponse<AdminUser[]>>(
                '/admin/users'
            )
            return res.data.data
        },
    })

    // Toggle khóa/mở tài khoản
    const toggleMutation = useMutation({
        mutationFn: async ({
                               userId,
                               enable,
                           }: {
            userId: string
            enable: boolean
        }) => {
            await axiosInstance.patch(`/admin/users/${userId}/toggle`, {
                enabled: enable,
            })
        },
        onSuccess: (_, { enable }) => {
            queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
            toast.success(enable ? 'Đã mở khóa tài khoản!' : 'Đã khóa tài khoản!')
            setToggleTarget(null)
        },
    })

    const filtered = search
        ? users.filter(
            (u) =>
                u.fullName.toLowerCase().includes(search.toLowerCase()) ||
                u.email.toLowerCase().includes(search.toLowerCase())
        )
        : users

    if (isLoading) return <Loading text="Đang tải danh sách..." />

    return (
        <div>
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="h-10 w-10 bg-blue-100 rounded-xl flex
                          items-center justify-center">
                        <Users className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">
                            Quản lý người dùng
                        </h1>
                        <p className="text-sm text-gray-500">
                            {users.length} người dùng ·{' '}
                            {users.filter((u) => !u.isEnabled).length} đã khóa
                        </p>
                    </div>
                </div>
            </div>

            {/* Search */}
            <div className="relative mb-5">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2
                           h-4 w-4 text-gray-400 pointer-events-none" />
                <input
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Tìm theo tên hoặc email..."
                    className="input pl-10"
                />
            </div>

            {/* Table */}
            <div className="card overflow-hidden">
                <table className="w-full text-sm">
                    <thead>
                    <tr className="border-b border-gray-100 bg-gray-50/50">
                        <th className="text-left px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider">
                            Người dùng
                        </th>
                        <th className="text-left px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider
                             hidden md:table-cell">
                            Vai trò
                        </th>
                        <th className="text-left px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider
                             hidden lg:table-cell">
                            Dung lượng
                        </th>
                        <th className="text-left px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider
                             hidden lg:table-cell">
                            Ngày tham gia
                        </th>
                        <th className="text-center px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider">
                            Trạng thái
                        </th>
                        <th className="text-right px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider">
                            Hành động
                        </th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                    {filtered.map((user) => (
                        <tr
                            key={user.id}
                            className={cn(
                                'hover:bg-gray-50 transition-colors',
                                !user.isEnabled && 'opacity-60'
                            )}
                        >
                            {/* User info */}
                            <td className="px-4 py-3">
                                <div className="flex items-center gap-3">
                                    <Avatar
                                        name={user.fullName}
                                        avatarUrl={user.avatarUrl}
                                        size="sm"
                                    />
                                    <div>
                                        <p className="font-medium text-gray-900">
                                            {user.fullName}
                                        </p>
                                        <p className="text-xs text-gray-400">{user.email}</p>
                                    </div>
                                </div>
                            </td>

                            {/* Roles */}
                            <td className="px-4 py-3 hidden md:table-cell">
                                <div className="flex gap-1 flex-wrap">
                                    {user.roles.map((role) => (
                                        <RoleBadge
                                            key={role}
                                            role={role.replace('ROLE_', '')}
                                        />
                                    ))}
                                </div>
                            </td>

                            {/* Storage */}
                            <td className="px-4 py-3 hidden lg:table-cell">
                                <div className="min-w-[100px]">
                                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-gray-600">
                        {user.storage?.usedFormatted ?? '0 B'}
                      </span>
                                        <span className="text-gray-400">
                        {user.storage?.usedPercent?.toFixed(0) ?? 0}%
                      </span>
                                    </div>
                                    <div className="h-1.5 bg-gray-100 rounded-full w-24">
                                        <div
                                            className={cn(
                                                'h-full rounded-full',
                                                (user.storage?.usedPercent ?? 0) > 80
                                                    ? 'bg-red-500'
                                                    : 'bg-primary-500'
                                            )}
                                            style={{
                                                width: `${Math.min(
                                                    user.storage?.usedPercent ?? 0, 100
                                                )}%`,
                                            }}
                                        />
                                    </div>
                                </div>
                            </td>

                            {/* Created at */}
                            <td className="px-4 py-3 text-xs text-gray-400
                               hidden lg:table-cell">
                                {formatDate(user.createdAt)}
                            </td>

                            {/* Status */}
                            <td className="px-4 py-3 text-center">
                  <span className={
                      user.isEnabled ? 'badge-green' : 'badge-red'
                  }>
                    {user.isEnabled ? 'Hoạt động' : 'Đã khóa'}
                  </span>
                            </td>

                            {/* Actions */}
                            <td className="px-4 py-3 text-right">
                                <button
                                    onClick={() => setToggleTarget(user)}
                                    className={cn(
                                        'inline-flex items-center gap-1.5 px-3 py-1.5',
                                        'text-xs font-medium rounded-lg transition-colors',
                                        user.isEnabled
                                            ? 'text-red-600 hover:bg-red-50'
                                            : 'text-green-600 hover:bg-green-50'
                                    )}
                                >
                                    {user.isEnabled ? (
                                        <>
                                            <Lock className="h-3.5 w-3.5" />
                                            Khóa
                                        </>
                                    ) : (
                                        <>
                                            <Unlock className="h-3.5 w-3.5" />
                                            Mở khóa
                                        </>
                                    )}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                {filtered.length === 0 && (
                    <div className="text-center py-10 text-gray-400 text-sm">
                        Không tìm thấy người dùng nào
                    </div>
                )}
            </div>

            {/* Toggle confirm */}
            <ConfirmDialog
                isOpen={!!toggleTarget}
                onClose={() => setToggleTarget(null)}
                onConfirm={() => {
                    if (!toggleTarget) return
                    toggleMutation.mutate({
                        userId: toggleTarget.id,
                        enable: !toggleTarget.isEnabled,
                    })
                }}
                title={
                    toggleTarget?.isEnabled ? 'Khóa tài khoản' : 'Mở khóa tài khoản'
                }
                message={
                    toggleTarget?.isEnabled
                        ? `Khóa tài khoản "${toggleTarget?.fullName}"? Người dùng sẽ không thể đăng nhập.`
                        : `Mở khóa tài khoản "${toggleTarget?.fullName}"?`
                }
                confirmText={toggleTarget?.isEnabled ? 'Khóa' : 'Mở khóa'}
                variant={toggleTarget?.isEnabled ? 'danger' : 'warning'}
                isLoading={toggleMutation.isPending}
            />
        </div>
    )
}