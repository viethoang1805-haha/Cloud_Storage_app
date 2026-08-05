import { useQuery } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import Loading from '@/components/common/Loading'
import Avatar from '@/components/common/Avatar'
import { RoleBadge } from '@/components/common/Badge'
import { Users, Search } from 'lucide-react'
import { useState } from 'react'
import { formatDate, formatBytes } from '@/lib/utils'

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
    const [search, setSearch] = useState('')

    const { data: users = [], isLoading } = useQuery({
        queryKey: ['admin', 'users'],
        queryFn: async () => {
            const res = await axiosInstance.get<ApiResponse<AdminUser[]>>(
                '/admin/users'
            )
            return res.data.data
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
                            {users.length} người dùng
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
                        <th className="text-left px-4 py-3 text-xs font-medium
                             text-gray-500 uppercase tracking-wider">
                            Trạng thái
                        </th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                    {filtered.map((user) => (
                        <tr key={user.id}
                            className="hover:bg-gray-50 transition-colors">
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
                                        <p className="text-xs text-gray-400">
                                            {user.email}
                                        </p>
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
                                <div>
                                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-gray-600">
                        {user.storage.usedFormatted}
                      </span>
                                        <span className="text-gray-400">
                        {user.storage.usedPercent.toFixed(0)}%
                      </span>
                                    </div>
                                    <div className="h-1.5 bg-gray-100 rounded-full w-24">
                                        <div
                                            className="h-full bg-primary-500 rounded-full"
                                            style={{
                                                width: `${Math.min(
                                                    user.storage.usedPercent, 100
                                                )}%`,
                                            }}
                                        />
                                    </div>
                                </div>
                            </td>

                            {/* Created */}
                            <td className="px-4 py-3 text-xs text-gray-400
                               hidden lg:table-cell">
                                {formatDate(user.createdAt)}
                            </td>

                            {/* Status */}
                            <td className="px-4 py-3">
                  <span className={user.isEnabled
                      ? 'badge-green' : 'badge-red'}>
                    {user.isEnabled ? 'Hoạt động' : 'Đã khóa'}
                  </span>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                {filtered.length === 0 && (
                    <div className="text-center py-10 text-gray-400 text-sm">
                        Không tìm thấy người dùng
                    </div>
                )}
            </div>
        </div>
    )
}