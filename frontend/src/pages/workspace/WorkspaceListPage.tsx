import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, FolderOpen, Users, Loader2 } from 'lucide-react'
import { useWorkspaces, useCreateWorkspace } from '@/hooks/useWorkspace'
import { useWebSocket } from '@/hooks/useSocket'
import Modal from '@/components/common/Modal'
import Loading from '@/components/common/Loading'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Workspace } from '@/types/workspace'

const createSchema = z.object({
    name: z.string().min(1, 'Tên không được để trống').max(255),
    description: z.string().max(1000).optional(),
})

type CreateForm = z.infer<typeof createSchema>

export default function WorkspaceListPage() {
    // (1) Kết nối WebSocket ở trang chính
    useWebSocket()

    const navigate = useNavigate()
    const [showCreate, setShowCreate] = useState(false)

    const { data: workspaces, isLoading } = useWorkspaces()
    const createMutation = useCreateWorkspace()

    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<CreateForm>({ resolver: zodResolver(createSchema) })

    const onSubmit = async (data: CreateForm) => {
        await createMutation.mutateAsync(data)
        reset()
        setShowCreate(false)
    }

    if (isLoading) return <Loading text="Đang tải workspace..." />

    return (
        <div>
            {/* Page header */}
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Workspaces</h1>
                    <p className="text-gray-500 mt-1">
                        Quản lý không gian làm việc của bạn
                    </p>
                </div>
                <button
                    onClick={() => setShowCreate(true)}
                    className="flex items-center gap-2 bg-primary text-white
                     px-4 py-2 rounded-lg hover:bg-primary/90
                     transition-colors text-sm font-medium"
                >
                    <Plus className="h-4 w-4" />
                    Tạo workspace
                </button>
            </div>

            {/* Workspace grid */}
            {workspaces?.length === 0 ? (
                // (2) Empty state
                <div className="text-center py-16">
                    <FolderOpen className="h-12 w-12 text-gray-300 mx-auto mb-3" />
                    <h3 className="text-gray-600 font-medium mb-1">
                        Chưa có workspace nào
                    </h3>
                    <p className="text-gray-400 text-sm mb-4">
                        Tạo workspace đầu tiên để bắt đầu
                    </p>
                    <button
                        onClick={() => setShowCreate(true)}
                        className="text-primary hover:underline text-sm font-medium"
                    >
                        Tạo ngay
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {workspaces?.map((ws) => (
                        <WorkspaceCard
                            key={ws.id}
                            workspace={ws}
                            onClick={() => navigate(`/workspaces/${ws.id}/files`)}
                        />
                    ))}
                </div>
            )}

            {/* Create Modal */}
            <Modal
                isOpen={showCreate}
                onClose={() => { setShowCreate(false); reset() }}
                title="Tạo workspace mới"
            >
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Tên workspace *
                        </label>
                        <input
                            {...register('name')}
                            placeholder="VD: Team Project Q1 2024"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary/30"
                        />
                        {errors.name && (
                            <p className="mt-1 text-xs text-red-500">
                                {errors.name.message}
                            </p>
                        )}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Mô tả
                        </label>
                        <textarea
                            {...register('description')}
                            rows={3}
                            placeholder="Mô tả ngắn về workspace (không bắt buộc)"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary/30 resize-none"
                        />
                    </div>

                    <div className="flex gap-3 justify-end pt-2">
                        <button
                            type="button"
                            onClick={() => { setShowCreate(false); reset() }}
                            className="px-4 py-2 text-sm font-medium text-gray-700
                         bg-gray-100 rounded-lg hover:bg-gray-200"
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            disabled={createMutation.isPending}
                            className="flex items-center gap-2 px-4 py-2 text-sm
                         font-medium text-white bg-primary rounded-lg
                         hover:bg-primary/90 disabled:opacity-50"
                        >
                            {createMutation.isPending && (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            )}
                            Tạo workspace
                        </button>
                    </div>
                </form>
            </Modal>
        </div>
    )
}

// (3) Workspace Card component
function WorkspaceCard({
                           workspace,
                           onClick,
                       }: {
    workspace: Workspace
    onClick: () => void
}) {
    const roleColors = {
        OWNER: 'bg-purple-100 text-purple-700',
        ADMIN: 'bg-blue-100 text-blue-700',
        MEMBER: 'bg-green-100 text-green-700',
        VIEWER: 'bg-gray-100 text-gray-700',
    }

    return (
        <div
            onClick={onClick}
            className="bg-white border border-gray-200 rounded-xl p-5
                 hover:border-primary/50 hover:shadow-md
                 transition-all cursor-pointer group"
        >
            {/* Header */}
            <div className="flex items-start justify-between mb-3">
                <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-gray-900 truncate
                         group-hover:text-primary transition-colors">
                        {workspace.name}
                    </h3>
                    {workspace.description && (
                        <p className="text-xs text-gray-500 mt-0.5 truncate">
                            {workspace.description}
                        </p>
                    )}
                </div>
                {/* Role badge */}
                <span className={`text-xs font-medium px-2 py-0.5 rounded-full
                         ml-2 flex-shrink-0 ${roleColors[workspace.myRole]}`}>
          {workspace.myRole}
        </span>
            </div>

            {/* Stats */}
            <div className="flex items-center gap-4 text-xs text-gray-500">
                <div className="flex items-center gap-1">
                    <Users className="h-3.5 w-3.5" />
                    <span>{workspace.memberCount} thành viên</span>
                </div>
                {workspace.isPersonal && (
                    <span className="text-primary font-medium">Cá nhân</span>
                )}
            </div>
        </div>
    )
}