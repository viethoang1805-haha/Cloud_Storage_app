import { useState } from 'react'
import { Activity, Filter } from 'lucide-react'
import { useAdminActivities } from '@/hooks/useActivity'
import Loading from '@/components/common/Loading'
import EmptyState from '@/components/common/EmptyState'
import Avatar from '@/components/common/Avatar'
import { formatRelativeTime, cn } from '@/lib/utils'
import { ACTION_LABELS } from '@/utils/constants'

const ACTION_COLORS: Record<string, string> = {
    FILE_UPLOADED:    'bg-blue-50 text-blue-700',
    FILE_DOWNLOADED:  'bg-green-50 text-green-700',
    FILE_DELETED:     'bg-red-50 text-red-700',
    FILE_SHARED:      'bg-purple-50 text-purple-700',
    FOLDER_CREATED:   'bg-yellow-50 text-yellow-700',
    MEMBER_INVITED:   'bg-indigo-50 text-indigo-700',
    WORKSPACE_CREATED:'bg-teal-50 text-teal-700',
    USER_LOGIN:       'bg-gray-100 text-gray-600',
}

export default function AdminActivitiesPage() {
    const [page, setPage] = useState(0)
    const [actionFilter, setActionFilter] = useState('')

    const { data, isLoading } = useAdminActivities({
        page,
        action: actionFilter || undefined,
    })

    const activities = data?.activities ?? []
    const totalPages = data?.totalPages ?? 0

    return (
        <div>
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="h-10 w-10 bg-indigo-100 rounded-xl flex
                          items-center justify-center">
                        <Activity className="h-5 w-5 text-indigo-600" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">
                            Nhật ký hệ thống
                        </h1>
                        <p className="text-sm text-gray-500">
                            {data?.totalElements ?? 0} hoạt động
                        </p>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4 text-gray-400" />
                    <select
                        value={actionFilter}
                        onChange={(e) => {
                            setActionFilter(e.target.value)
                            setPage(0)
                        }}
                        className="text-sm border border-gray-200 rounded-xl
                       px-3 py-2 text-gray-700 focus:outline-none
                       focus:ring-2 focus:ring-primary-500/30 bg-white"
                    >
                        <option value="">Tất cả hành động</option>
                        {Object.entries(ACTION_LABELS).map(([key, label]) => (
                            <option key={key} value={key}>{label}</option>
                        ))}
                    </select>
                </div>
            </div>

            {isLoading ? (
                <Loading text="Đang tải..." />
            ) : activities.length === 0 ? (
                <EmptyState
                    icon={Activity}
                    title="Chưa có hoạt động"
                    description="Các hoạt động toàn hệ thống sẽ xuất hiện ở đây"
                />
            ) : (
                <>
                    <div className="card overflow-hidden">
                        <ul className="divide-y divide-gray-50">
                            {activities.map((log) => {
                                const colorClass =
                                    ACTION_COLORS[log.action] ?? 'bg-gray-100 text-gray-600'
                                const displayText =
                                    ACTION_LABELS[log.action] ?? log.actionDisplay ?? log.action

                                return (
                                    <li
                                        key={log.id}
                                        className="flex items-start gap-4 px-5 py-4
                               hover:bg-gray-50 transition-colors"
                                    >
                                        <Avatar
                                            name={log.actor?.name ?? 'Unknown'}
                                            size="sm"
                                        />
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-sm font-semibold text-gray-900">
                          {log.actor?.name ?? 'Unknown'}
                        </span>
                                                <span className={cn(
                                                    'text-xs px-2 py-0.5 rounded-full font-medium',
                                                    colorClass
                                                )}>
                          {displayText}
                        </span>
                                                {log.targetName && (
                                                    <span className="text-sm text-gray-600 truncate">
                            "{log.targetName}"
                          </span>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-3 mt-1">
                        <span className="text-xs text-gray-400">
                          {log.actor?.email}
                        </span>
                                                {log.workspaceName && (
                                                    <span className="text-xs text-gray-400">
                            📁 {log.workspaceName}
                          </span>
                                                )}
                                                {log.ipAddress && (
                                                    <span className="text-xs text-gray-300">
                            {log.ipAddress}
                          </span>
                                                )}
                                            </div>
                                        </div>
                                        <span className="text-xs text-gray-400 flex-shrink-0">
                      {formatRelativeTime(log.createdAt)}
                    </span>
                                    </li>
                                )
                            })}
                        </ul>
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center justify-center gap-2 mt-5">
                            <button
                                onClick={() => setPage((p) => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-3 py-1.5 text-sm border border-gray-200
                           rounded-lg disabled:opacity-40 hover:bg-gray-50"
                            >
                                ← Trước
                            </button>
                            <span className="text-sm text-gray-500">
                {page + 1} / {totalPages}
              </span>
                            <button
                                onClick={() => setPage((p) => p + 1)}
                                disabled={page >= totalPages - 1}
                                className="px-3 py-1.5 text-sm border border-gray-200
                           rounded-lg disabled:opacity-40 hover:bg-gray-50"
                            >
                                Sau →
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    )
}