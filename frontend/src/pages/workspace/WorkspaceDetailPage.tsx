import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
    Users, FolderOpen, ArrowLeft,
    Settings, Trash2, BarChart2,
    Edit2, Activity,
} from 'lucide-react'
import {
    useWorkspace,
    useUpdateWorkspace,
    useDeleteWorkspace,
} from '@/hooks/useWorkspace'
import { useAuthStore } from '@/store/auth.store'
import Loading from '@/components/common/Loading'
import Button from '@/components/common/Button'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import Avatar from '@/components/common/Avatar'
import { RoleBadge } from '@/components/common/Badge'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { cn } from '@/lib/utils'

const editSchema = z.object({
    name: z.string().min(1, 'Tên không được để trống').max(255),
    description: z.string().max(1000).optional(),
})
type EditForm = z.infer<typeof editSchema>

export default function WorkspaceDetailPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()
    const { user } = useAuthStore()

    const [showEdit, setShowEdit] = useState(false)
    const [showDelete, setShowDelete] = useState(false)

    const { data: workspace, isLoading } = useWorkspace(workspaceId!)
    const updateMutation = useUpdateWorkspace(workspaceId!)
    const deleteMutation = useDeleteWorkspace()

    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<EditForm>({
            resolver: zodResolver(editSchema),
            values: {
                name: workspace?.name ?? '',
                description: workspace?.description ?? '',
            },
        })

    const isOwner = workspace?.myRole === 'OWNER'
    const isAdmin = workspace?.myRole === 'ADMIN' || isOwner

    const onEdit = async (data: EditForm) => {
        await updateMutation.mutateAsync(data)
        setShowEdit(false)
    }

    const onDelete = async () => {
        await deleteMutation.mutateAsync(workspaceId!)
        navigate('/workspaces')
    }

    if (isLoading) return <Loading text="Đang tải..." />

    if (!workspace) return (
        <div className="text-center py-20 text-gray-500">
            Không tìm thấy workspace
        </div>
    )

    const quickActions = [
        {
            icon: FolderOpen,
            label: 'Quản lý file',
            desc: 'Upload và xem tài liệu',
            color: 'bg-blue-50 text-blue-600',
            onClick: () => navigate(`/workspaces/${workspaceId}/files`),
        },
        {
            icon: Users,
            label: 'Thành viên',
            desc: `${workspace.memberCount} thành viên`,
            color: 'bg-green-50 text-green-600',
            onClick: () => navigate(`/workspaces/${workspaceId}/members`),
        },
        {
            icon: Activity,
            label: 'Hoạt động',
            desc: 'Nhật ký workspace',
            color: 'bg-purple-50 text-purple-600',
            onClick: () => navigate(`/workspaces/${workspaceId}/activities`),
        },
        {
            icon: BarChart2,
            label: 'Thống kê',
            desc: 'Dashboard workspace',
            color: 'bg-orange-50 text-orange-600',
            onClick: () => navigate(`/workspaces/${workspaceId}/dashboard`),
        },
    ]

    return (
        <div className="max-w-3xl space-y-6">
            {/* Header */}
            <div className="flex items-start gap-4">
                <button
                    onClick={() => navigate('/workspaces')}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors mt-0.5"
                >
                    <ArrowLeft className="h-4 w-4" />
                </button>

                <div className="flex-1">
                    <div className="flex items-start justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">
                                {workspace.name}
                            </h1>
                            {workspace.description && (
                                <p className="text-gray-500 mt-1 text-sm">
                                    {workspace.description}
                                </p>
                            )}
                        </div>
                        <RoleBadge role={workspace.myRole} />
                    </div>
                </div>
            </div>

            {/* Quick actions */}
            <div className="grid grid-cols-2 gap-3">
                {quickActions.map(({ icon: Icon, label, desc, color, onClick }) => (
                    <button
                        key={label}
                        onClick={onClick}
                        className="card-hover p-5 flex items-center gap-4 text-left"
                    >
                        <div className={cn(
                            'h-11 w-11 rounded-xl flex items-center justify-center',
                            'flex-shrink-0',
                            color
                        )}>
                            <Icon className="h-5 w-5" />
                        </div>
                        <div>
                            <p className="font-semibold text-gray-900 text-sm">{label}</p>
                            <p className="text-xs text-gray-400 mt-0.5">{desc}</p>
                        </div>
                    </button>
                ))}
            </div>

            {/* Info card */}
            <div className="card p-6">
                <div className="flex items-center justify-between mb-5">
                    <h2 className="section-title">Thông tin workspace</h2>
                    {isAdmin && (
                        <button
                            onClick={() => setShowEdit(true)}
                            className="flex items-center gap-1.5 text-sm text-primary-600
                         hover:text-primary-700 font-medium transition-colors"
                        >
                            <Edit2 className="h-3.5 w-3.5" />
                            Chỉnh sửa
                        </button>
                    )}
                </div>

                <div className="space-y-4">
                    {/* Owner */}
                    <div className="flex items-center gap-3">
                        <Avatar
                            name={workspace.owner.fullName}
                            avatarUrl={workspace.owner.avatarUrl}
                            size="md"
                        />
                        <div>
                            <p className="text-xs text-gray-400">Chủ sở hữu</p>
                            <p className="text-sm font-medium text-gray-900">
                                {workspace.owner.fullName}
                            </p>
                            <p className="text-xs text-gray-400">
                                {workspace.owner.email}
                            </p>
                        </div>
                    </div>

                    <div className="divider" />

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <p className="text-xs text-gray-400 mb-1">Thành viên</p>
                            <p className="text-sm font-semibold text-gray-900">
                                {workspace.memberCount} người
                            </p>
                        </div>
                        <div>
                            <p className="text-xs text-gray-400 mb-1">Loại</p>
                            <p className="text-sm font-semibold text-gray-900">
                                {workspace.isPersonal ? 'Cá nhân' : 'Nhóm'}
                            </p>
                        </div>
                    </div>

                    {workspace.isPersonal && (
                        <div className="flex items-center gap-2 text-xs
                            text-primary-600 bg-primary-50
                            rounded-xl px-3 py-2">
                            <span>🔒</span>
                            Workspace cá nhân — không thể xóa
                        </div>
                    )}
                </div>
            </div>

            {/* Danger zone — chỉ OWNER */}
            {isOwner && !workspace.isPersonal && (
                <div className="card p-6 border-red-100">
                    <h2 className="section-title text-red-600 mb-1">Vùng nguy hiểm</h2>
                    <p className="text-sm text-gray-500 mb-4">
                        Xóa workspace sẽ xóa toàn bộ file, thư mục và thành viên.
                        Hành động này không thể hoàn tác.
                    </p>
                    <Button
                        variant="danger"
                        onClick={() => setShowDelete(true)}
                    >
                        <Trash2 className="h-4 w-4" />
                        Xóa workspace
                    </Button>
                </div>
            )}

            {/* Edit modal */}
            <Modal
                isOpen={showEdit}
                onClose={() => { setShowEdit(false); reset() }}
                title="Chỉnh sửa workspace"
                footer={
                    <div className="flex gap-3 justify-end">
                        <Button
                            variant="secondary"
                            onClick={() => { setShowEdit(false); reset() }}
                        >
                            Hủy
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleSubmit(onEdit)}
                            isLoading={updateMutation.isPending}
                        >
                            Lưu thay đổi
                        </Button>
                    </div>
                }
            >
                <div className="space-y-4">
                    <Input
                        label="Tên workspace"
                        error={errors.name?.message}
                        required
                        {...register('name')}
                    />
                    <div>
                        <label className="label">Mô tả</label>
                        <textarea
                            {...register('description')}
                            rows={3}
                            className="input resize-none"
                        />
                    </div>
                </div>
            </Modal>

            {/* Delete confirm */}
            <ConfirmDialog
                isOpen={showDelete}
                onClose={() => setShowDelete(false)}
                onConfirm={onDelete}
                title="Xóa workspace"
                message={`Xóa workspace "${workspace.name}"? Toàn bộ file và dữ liệu sẽ bị xóa vĩnh viễn.`}
                confirmText="Xóa vĩnh viễn"
                isLoading={deleteMutation.isPending}
            />
        </div>
    )
}