import { useQuery } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import { SystemDashboard } from '@/types/dashboard'
import Loading from '@/components/common/Loading'
import {
    Users, Files, Layers, HardDrive,
    TrendingUp, Activity,
} from 'lucide-react'
import { formatBytes } from '@/lib/utils'

export default function AdminDashboardPage() {
    const { data, isLoading } = useQuery({
        queryKey: ['admin', 'dashboard'],
        queryFn: async () => {
            const res = await axiosInstance.get<ApiResponse<SystemDashboard>>(
                '/admin/dashboard'
            )
            return res.data.data
        },
    })

    if (isLoading) return <Loading text="Đang tải..." />

    const stats = [
        {
            icon: Users,
            label: 'Tổng người dùng',
            value: data?.totalUsers ?? 0,
            sub: `+${data?.newUsersLast30Days ?? 0} trong 30 ngày`,
            color: 'text-blue-600',
            bg: 'bg-blue-50',
        },
        {
            icon: Layers,
            label: 'Tổng workspace',
            value: data?.totalWorkspaces ?? 0,
            sub: '',
            color: 'text-purple-600',
            bg: 'bg-purple-50',
        },
        {
            icon: Files,
            label: 'Tổng file',
            value: data?.totalFiles ?? 0,
            sub: `+${data?.newFilesLast30Days ?? 0} trong 30 ngày`,
            color: 'text-green-600',
            bg: 'bg-green-50',
        },
        {
            icon: HardDrive,
            label: 'Tổng dung lượng',
            value: data?.totalStorageFormatted ?? '0 B',
            sub: '',
            color: 'text-orange-600',
            bg: 'bg-orange-50',
            isText: true,
        },
    ]

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-3">
                <div className="h-10 w-10 bg-purple-100 rounded-xl flex
                        items-center justify-center">
                    <Activity className="h-5 w-5 text-purple-600" />
                </div>
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Admin Dashboard
                    </h1>
                    <p className="text-sm text-gray-500">
                        Tổng quan toàn hệ thống
                    </p>
                </div>
                <span className="ml-auto badge-purple">ADMIN</span>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                {stats.map(({ icon: Icon, label, value, sub, color, bg, isText }) => (
                    <div key={label} className="card p-5">
                        <div className={cn('inline-flex p-2 rounded-xl mb-3', bg)}>
                            <Icon className={cn('h-5 w-5', color)} />
                        </div>
                        <p className={cn(
                            'font-bold text-gray-900',
                            isText ? 'text-xl' : 'text-2xl'
                        )}>
                            {value}
                        </p>
                        <p className="text-sm text-gray-500 mt-0.5">{label}</p>
                        {sub && (
                            <p className="text-xs text-green-600 mt-1 font-medium">
                                ↑ {sub}
                            </p>
                        )}
                    </div>
                ))}
            </div>

            {/* Top workspaces */}
            {!!data?.topWorkspacesByStorage?.length && (
                <div className="card p-6">
                    <div className="flex items-center gap-2 mb-4">
                        <TrendingUp className="h-5 w-5 text-primary-600" />
                        <h2 className="section-title">Top Workspace theo dung lượng</h2>
                    </div>

                    <div className="overflow-hidden rounded-xl border border-gray-100">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="text-left px-4 py-3 text-xs font-medium
                                 text-gray-500 uppercase tracking-wider">
                                    Workspace
                                </th>
                                <th className="text-right px-4 py-3 text-xs font-medium
                                 text-gray-500 uppercase tracking-wider">
                                    File
                                </th>
                                <th className="text-right px-4 py-3 text-xs font-medium
                                 text-gray-500 uppercase tracking-wider">
                                    Dung lượng
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                            {data.topWorkspacesByStorage.map((ws, idx) => (
                                <tr key={ws.workspaceId}
                                    className="hover:bg-gray-50 transition-colors">
                                    <td className="px-4 py-3">
                                        <div className="flex items-center gap-3">
                        <span className="h-6 w-6 rounded-full bg-primary-100
                                         text-primary-700 text-xs font-bold
                                         flex items-center justify-center
                                         flex-shrink-0">
                          {idx + 1}
                        </span>
                                            <span className="font-medium text-gray-900">
                          {ws.workspaceName}
                        </span>
                                        </div>
                                    </td>
                                    <td className="px-4 py-3 text-right text-gray-600">
                                        {ws.fileCount}
                                    </td>
                                    <td className="px-4 py-3 text-right font-medium
                                   text-gray-900">
                                        {ws.storageFormatted}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    )
}

// Import cn
import { cn } from '@/lib/utils'