import { useQuery } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import Loading from '@/components/common/Loading'
import {
  HardDrive,
  Files,
  FolderOpen,
  Layers,
  Bell,
  TrendingUp,
} from 'lucide-react'
import { formatFileSize, formatRelativeTime } from '@/lib/utils'

interface PersonalDashboard {
  storage: {
    usedBytes: number
    limitBytes: number
    availableBytes: number
    usedFormatted: string
    limitFormatted: string
    usedPercent: number
  }
  totalFiles: number
  totalFolders: number
  totalWorkspaces: number
  unreadNotifications: number
  recentFiles: {
    id: string
    originalName: string
    contentType: string
    sizeFormatted: string
    workspaceName: string
    uploadedAt: string
  }[]
}

export default function DashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard', 'personal'],
    queryFn: async () => {
      const res = await axiosInstance.get<ApiResponse<PersonalDashboard>>(
        '/dashboard'
      )
      return res.data.data
    },
  })

  if (isLoading) return <Loading text="Đang tải dashboard..." />

  return (
    <div className="space-y-6">
      {/* Page title */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 mt-1">Tổng quan hoạt động của bạn</p>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={Files}
          label="Tổng file"
          value={data?.totalFiles ?? 0}
          color="blue"
        />
        <StatCard
          icon={FolderOpen}
          label="Thư mục"
          value={data?.totalFolders ?? 0}
          color="green"
        />
        <StatCard
          icon={Layers}
          label="Workspaces"
          value={data?.totalWorkspaces ?? 0}
          color="purple"
        />
        <StatCard
          icon={Bell}
          label="Thông báo mới"
          value={data?.unreadNotifications ?? 0}
          color="orange"
        />
      </div>

      {/* Storage usage */}
      {data?.storage && (
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center gap-2 mb-4">
            <HardDrive className="h-5 w-5 text-primary" />
            <h2 className="font-semibold text-gray-900">Dung lượng lưu trữ</h2>
          </div>

          {/* Progress bar */}
          <div className="mb-3">
            <div className="flex justify-between text-sm mb-2">
              <span className="text-gray-600">
                Đã dùng: <strong>{data.storage.usedFormatted}</strong>
              </span>
              <span className="text-gray-500">
                Giới hạn: {data.storage.limitFormatted}
              </span>
            </div>
            <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all ${
                  data.storage.usedPercent > 80
                    ? 'bg-red-500'
                    : data.storage.usedPercent > 60
                    ? 'bg-yellow-500'
                    : 'bg-primary'
                }`}
                style={{ width: `${Math.min(data.storage.usedPercent, 100)}%` }}
              />
            </div>
          </div>

          <p className="text-xs text-gray-400">
            {data.storage.usedPercent.toFixed(1)}% đã sử dụng •
            Còn trống: {data.storage.availableFormatted ?? ''}
          </p>
        </div>
      )}

      {/* Recent files */}
      {data?.recentFiles && data.recentFiles.length > 0 && (
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="h-5 w-5 text-primary" />
            <h2 className="font-semibold text-gray-900">File gần đây</h2>
          </div>

          <div className="space-y-3">
            {data.recentFiles.map((file) => (
              <div
                key={file.id}
                className="flex items-center gap-3 py-2 border-b
                           border-gray-50 last:border-0"
              >
                <span className="text-xl">
                  {file.contentType.startsWith('image/')
                    ? '🖼️'
                    : file.contentType === 'application/pdf'
                    ? '📄'
                    : '📁'}
                </span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {file.originalName}
                  </p>
                  <p className="text-xs text-gray-400">
                    {file.workspaceName} • {file.sizeFormatted}
                  </p>
                </div>
                <span className="text-xs text-gray-400 flex-shrink-0">
                  {formatRelativeTime(file.uploadedAt)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

// Stat Card component
function StatCard({
  icon: Icon,
  label,
  value,
  color,
}: {
  icon: React.ElementType
  label: string
  value: number
  color: 'blue' | 'green' | 'purple' | 'orange'
}) {
  const colorClasses = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    purple: 'bg-purple-50 text-purple-600',
    orange: 'bg-orange-50 text-orange-600',
  }

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-5">
      <div className={`inline-flex p-2 rounded-lg mb-3 ${colorClasses[color]}`}>
        <Icon className="h-5 w-5" />
      </div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      <p className="text-sm text-gray-500 mt-0.5">{label}</p>
    </div>
  )
}