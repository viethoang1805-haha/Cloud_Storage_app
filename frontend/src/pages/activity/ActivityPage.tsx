import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Activity, Filter, User, RefreshCw } from 'lucide-react'
import { useWorkspaceActivities, useMyActivities } from '@/hooks/useActivity'
import Loading from '@/components/common/Loading'
import EmptyState from '@/components/common/EmptyState'
import Avatar from '@/components/common/Avatar'
import { formatRelativeTime, cn } from '@/lib/utils'
import { ACTION_LABELS } from '@/utils/constants'

type TabType = 'all' | 'mine'

const ACTION_TYPES = [
    { value: '', label: 'Tất cả' },
    { value: 'FILE_UPLOADED', label: 'Upload file' },
    { value: 'FILE_DOWNLOADED', label: 'Tải file' },
    { value: 'FILE_DELETED', label: 'Xóa file' },
    { value: 'FILE_SHARED', label: 'Chia sẻ file' },
    { value: 'FOLDER_CREATED', label: 'Tạo thư mục' },
    { value: 'MEMBER_INVITED', label: 'Mời thành viên' },
    { value: 'MEMBER_REMOVED', label: 'Xóa thành viên' },
]

const ACTION_COLORS: Record<string, string> = {
    FILE_UPLOADED:   'bg-blue-50 text-blue-700',
    FILE_DOWNLOADED: 'bg-green-50 text-green-700',
    FILE_DELETED:    'bg-red-50 text-red-700',
    FILE_SHARED:     'bg-purple-50 text-purple-700',
    FOLDER_CREATED:  'bg-yellow-50 text-yellow-700',
    FOLDER_DELETED:  'bg-red-50 text-red-700',
    MEMBER_INVITED:  'bg-indigo-50 text-indigo-700',
    MEMBER_REMOVED:  'bg-orange-50 text-orange-700',
    WORKSPACE_CREATED: 'bg-teal-50 text-teal-700',
    USER_LOGIN:      'bg-gray-100 text-gray-600',
}

export default function ActivityPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const [tab, setTab] = useState<TabType>('all')
    const [actionFilter, setActionFilter] = useState('')
    const [page, setPage] = useState(0)

    const allQuery = useWorkspaceActivities(workspaceId!, {
        page,
        action: actionFilter || undefined,
    })

    const mineQuery = useMyActivities(workspaceId!, page)

    const activeQuery = tab === 'all' ? allQuery : mineQuery
    const activities = activeQuery.data?.activities ?? []
    const totalPages = activeQuery.data?.totalPages ?? 0

    const isLoading = activeQuery.isLoading

    return (
        <div>
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="h-10 w-10 bg-blue-50 rounded-xl flex
                          items-center justify-center">
                        <Activity className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">
                            Nhật ký hoạt động
                        </h1>
                        <p className="text-sm text-gray-500">
                            {activeQuery.data?.totalElements ?? 0} hoạt động
                        </p>
                    </div>
                </div>
                <button
                    onClick={() => activeQuery.refetch()}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors"
                    title="Làm mới"
                >
                    <RefreshCw className={cn(
                        'h-4 w-4',
                        activeQuery.isFetching && 'animate-spin'
                    )} />
                </button>
            </div>

            {/* Tabs + Filter */}
            <div className="flex flex-col sm:flex-row items-start sm:items-center
                      gap-3 mb-5">
                {/* Tabs */}
                <div className="flex border border-gray-200 rounded-xl
                        overflow-hidden flex-shrink-0">
                    {[
                        { key: 'all' as TabType, label: 'Tất cả', icon: Activity },
                        { key: 'mine' as TabType, label: 'Của tôi', icon: User },
                    ].map(({ key, label, icon: Icon }) => (
                        <button
                            key={key}
                            onClick={() => { setTab(key); setPage(0) }}
                            className={cn(
                                'flex items-center gap-2 px-4 py-2 text-sm font-medium',
                                'transition-colors',
                                tab === key
                                    ? 'bg-primary-600 text-white'
                                    : 'text-gray-600 hover:bg-gray-50'
                            )}
                        >
                            <Icon className="h-3.5 w-3.5" />
                            {label}
                        </button>
                    ))}
                </div>

                {/* Action filter — chỉ hiện khi tab all */}
                {tab === 'all' && (
                    <div className="flex items-center gap-2">
                        <Filter className="h-4 w-4 text-gray-400 flex-shrink-0" />
                        <select
                            value={actionFilter}
                            onChange={(e) => {
                                setActionFilter(e.target.value)
                                setPage(0)
                            }}
                            className="text-sm border border-gray-200 rounded-xl
                         px-3 py-2 text-gray-700 focus:outline-none
                         focus:ring-2 focus:ring-primary-500/30
                         bg-white"
                        >
                            {ACTION_TYPES.map((a) => (
                                <option key={a.value} value={a.value}>
                                    {a.label}
                                </option>
                            ))}
                        </select>
                    </div>
                )}
            </div>

            {/* Content */}
            {isLoading ? (
                <Loading text="Đang tải nhật ký..." />
            ) : activities.length === 0 ? (
                <EmptyState
                    icon={Activity}
                    title="Chưa có hoạt động nào"
                    description="Các hoạt động trong workspace sẽ xuất hiện ở đây"
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
                                        {/* Actor avatar */}
                                        <div className="flex-shrink-0 mt-0.5">
                                            <Avatar
                                                name={log.actor?.name ?? 'Unknown'}
                                                size="sm"
                                            />
                                        </div>

                                        {/* Content */}
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-sm font-medium text-gray-900">
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
                                                {log.ipAddress && (
                                                    <span className="text-xs text-gray-300">
                            IP: {log.ipAddress}
                          </span>
                                                )}
                                            </div>
                                        </div>

                                        {/* Time */}
                                        <span className="text-xs text-gray-400 flex-shrink-0
                                     mt-0.5">
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
                           rounded-lg disabled:opacity-40 hover:bg-gray-50
                           transition-colors"
                            >
                                ← Trước
                            </button>
                            <span className="text-sm text-gray-500">
                Trang {page + 1} / {totalPages}
              </span>
                            <button
                                onClick={() => setPage((p) => p + 1)}
                                disabled={page >= totalPages - 1}
                                className="px-3 py-1.5 text-sm border border-gray-200
                           rounded-lg disabled:opacity-40 hover:bg-gray-50
                           transition-colors"
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