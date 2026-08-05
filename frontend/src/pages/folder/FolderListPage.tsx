import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, FolderOpen } from 'lucide-react'
import { useRootFolders } from '@/hooks/useFolder'
import { useWorkspace } from '@/hooks/useWorkspace'
import Loading from '@/components/common/Loading'
import EmptyState from '@/components/common/EmptyState'
import Button from '@/components/common/Button'
import { formatDate } from '@/lib/utils'

export default function FolderListPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()

    const { data: workspace } = useWorkspace(workspaceId!)
    const { data: folders = [], isLoading } = useRootFolders(workspaceId!)

    if (isLoading) return <Loading text="Đang tải thư mục..." />

    return (
        <div>
            <div className="flex items-center gap-3 mb-6">
                <button
                    onClick={() => navigate(-1)}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors"
                >
                    <ArrowLeft className="h-4 w-4" />
                </button>
                <div>
                    <h1 className="text-xl font-bold text-gray-900">Thư mục</h1>
                    <p className="text-sm text-gray-500">{workspace?.name}</p>
                </div>
            </div>

            {folders.length === 0 ? (
                <EmptyState
                    icon={FolderOpen}
                    title="Chưa có thư mục nào"
                    description="Tạo thư mục để tổ chức file của bạn"
                    action={{
                        label: 'Đến trang quản lý file',
                        onClick: () => navigate(`/workspaces/${workspaceId}/files`),
                    }}
                />
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {folders.map((folder) => (
                        <button
                            key={folder.id}
                            onClick={() =>
                                navigate(`/workspaces/${workspaceId}/files`)
                            }
                            className="card-hover p-5 text-left"
                        >
                            <FolderOpen className="h-8 w-8 text-primary-500 mb-3" />
                            <p className="font-medium text-gray-900 truncate">
                                {folder.name}
                            </p>
                            <p className="text-xs text-gray-400 mt-1">
                                {folder.childCount} thư mục con
                            </p>
                            <p className="text-xs text-gray-300 mt-0.5">
                                {formatDate(folder.createdAt)}
                            </p>
                        </button>
                    ))}
                </div>
            )}
        </div>
    )
}