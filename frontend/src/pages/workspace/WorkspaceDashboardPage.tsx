import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { dashboardApi } from '@/api/dashboard.api'
import Loading from '@/components/common/Loading'
import Avatar from '@/components/common/Avatar'
import {
    Files, FolderOpen, Users, HardDrive,
    TrendingUp, Activity, ArrowLeft,
    PieChart,
} from 'lucide-react'
import { formatRelativeTime, cn } from '@/lib/utils'
import { ACTION_LABELS } from '@/utils/constants'

const FILE_TYPE_COLORS: Record<string, string> = {
    PDF:        'bg-red-500',
    Image:      'bg-blue-500',
    Video:      'bg-purple-500',
    Word:       'bg-blue-700',
    Excel:      'bg-green-600',
    PowerPoint: 'bg-orange-500',
    Archive:    'bg-yellow-600',
    Audio:      'bg-pink-500',
    Text:       'bg-gray-500',
    Other:      'bg-gray-400',
}

export default function WorkspaceDashboardPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()

    const { data, isLoading } = useQuery({
        queryKey: ['dashboard', 'workspace', workspaceId],
        queryFn: () => dashboardApi.getWorkspace(workspaceId!),
        enabled: !!workspaceId,
    })

    if (isLoading) return <Loading text="Đang tải dashboard..." />

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <button
                    onClick={() => navigate(-1)}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors"
                >
                    <ArrowLeft className="h-4 w-4" />
                </button>
                <div>
                    <h1 className="text-xl font-bold text-gray-900">
                        Dashboard: {data?.workspaceName}
                    </h1>
                    <p className="text-sm text-gray-500">
                        Thống kê workspace
                    </p>
                </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                    {
                        icon: Files,
                        label: 'Tổng file',
                        value: data?.totalFiles ?? 0,
                        color: 'text-blue-600',
                        bg: 'bg-blue-50',
                    },
                    {
                        icon: FolderOpen,
                        label: 'Thư mục',
                        value: data?.totalFolders ?? 0,
                        color: 'text-green-600',
                        bg: 'bg-green-50',
                    },
                    {
                        icon: Users,
                        label: 'Thành viên',
                        value: data?.totalMembers ?? 0,
                        color: 'text-purple-600',
                        bg: 'bg-purple-50',
                    },
                    {
                        icon: HardDrive,
                        label: 'Dung lượng',
                        value: data?.totalStorageFormatted ?? '0 B',
                        color: 'text-orange-600',
                        bg: 'bg-orange-50',
                        isText: true,
                    },
                ].map(({ icon: Icon, label, value, color, bg, isText }) => (
                    <div key={label} className="card p-5">
                        <div className={cn('inline-flex p-2 rounded-xl mb-3', bg)}>
                            <Icon className={cn('h-5 w-5', color)} />
                        </div>
                        <p className={cn(
                            'font-bold text-gray-900',
                            isText ? 'text-lg' : 'text-2xl'
                        )}>
                            {value}
                        </p>
                        <p className="text-sm text-gray-500 mt-0.5">{label}</p>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* File type stats */}
                {!!data?.fileTypeStats?.length && (
                    <div className="card p-6">
                        <div className="flex items-center gap-2 mb-4">
                            <PieChart className="h-5 w-5 text-primary-600" />
                            <h2 className="section-title">Phân loại file</h2>
                        </div>
                        <ul className="space-y-3">
                            {data.fileTypeStats.map((stat) => (
                                <li key={stat.type}>
                                    <div className="flex items-center justify-between
                                  text-sm mb-1">
                    <span className="font-medium text-gray-700">
                      {stat.type}
                    </span>
                                        <div className="flex items-center gap-3">
                      <span className="text-gray-500 text-xs">
                        {stat.count} file
                      </span>
                                            <span className="text-gray-400 text-xs">
                        {stat.totalSizeFormatted}
                      </span>
                                            <span className="font-semibold text-gray-900 w-10
                                       text-right">
                        {stat.percentage.toFixed(0)}%
                      </span>
                                        </div>
                                    </div>
                                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                                        <div
                                            className={cn(
                                                'h-full rounded-full transition-all',
                                                FILE_TYPE_COLORS[stat.type] ?? 'bg-gray-400'
                                            )}
                                            style={{ width: `${Math.min(stat.percentage, 100)}%` }}
                                        />
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}

                {/* Top contributors */}
                {!!data?.topContributors?.length && (
                    <div className="card p-6">
                        <div className="flex items-center gap-2 mb-4">
                            <TrendingUp className="h-5 w-5 text-primary-600" />
                            <h2 className="section-title">Top Contributors</h2>
                        </div>
                        <ul className="space-y-3">
                            {data.topContributors.map((contributor, idx) => (
                                <li
                                    key={contributor.userId}
                                    className="flex items-center gap-3"
                                >
                  <span className={cn(
                      'h-6 w-6 rounded-full flex items-center justify-center',
                      'text-xs font-bold flex-shrink-0',
                      idx === 0 ? 'bg-yellow-100 text-yellow-700' :
                          idx === 1 ? 'bg-gray-100 text-gray-600' :
                              idx === 2 ? 'bg-orange-100 text-orange-700' :
                                  'bg-gray-50 text-gray-500'
                  )}>
                    {idx + 1}
                  </span>
                                    <Avatar
                                        name={contributor.userName}
                                        avatarUrl={contributor.avatarUrl}
                                        size="sm"
                                    />
                                    <div className="flex-1 min-w-0">
                                        <p className="text-sm font-medium text-gray-900 truncate">
                                            {contributor.userName}
                                        </p>
                                        <p className="text-xs text-gray-400">
                                            {contributor.fileCount} file ·{' '}
                                            {contributor.totalSizeFormatted}
                                        </p>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            {/* Recent activities */}
            {!!data?.recentActivities?.length && (
                <div className="card p-6">
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-2">
                            <Activity className="h-5 w-5 text-primary-600" />
                            <h2 className="section-title">Hoạt động gần đây</h2>
                        </div>
                        <button
                            onClick={() =>
                                navigate(`/workspaces/${workspaceId}/activities`)
                            }
                            className="text-xs text-primary-600 hover:text-primary-700
                         font-medium hover:underline"
                        >
                            Xem tất cả →
                        </button>
                    </div>

                    <ul className="divide-y divide-gray-50">
                        {data.recentActivities.map((activity, idx) => (
                            <li
                                key={idx}
                                className="flex items-center gap-3 py-3
                           first:pt-0 last:pb-0"
                            >
                                <Avatar name={activity.actorName} size="xs" />
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm text-gray-700 truncate">
                    <span className="font-medium text-gray-900">
                      {activity.actorName}
                    </span>{' '}
                                        {activity.actionDisplay?.toLowerCase()}
                                        {activity.targetName && (
                                            <span className="text-gray-500">
                        {' '}"
                        <span className="font-medium text-gray-700">
                          {activity.targetName}
                        </span>
                        "
                      </span>
                                        )}
                                    </p>
                                </div>
                                <span className="text-xs text-gray-400 flex-shrink-0">
                  {formatRelativeTime(activity.createdAt)}
                </span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    )
}