import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, UserPlus, Crown, Shield, Eye, User } from 'lucide-react'
import { memberApi, Member, MemberInviteRequest } from '@/api/member.api'
import { useWorkspace } from '@/hooks/useWorkspace'
import Avatar from '@/components/common/Avatar'
import Button from '@/components/common/Button'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import Loading from '@/components/common/Loading'
import { RoleBadge } from '@/components/common/Badge'
import { useAuthStore } from '@/store/auth.store'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { formatDate } from '@/lib/utils'

const inviteSchema = z.object({
    email: z.string().min(1, 'Nhập email').email('Email không hợp lệ'),
    role: z.enum(['ADMIN', 'MEMBER', 'VIEWER']),
})

type InviteForm = z.infer<typeof inviteSchema>

export default function WorkspaceMemberPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { user } = useAuthStore()

    const [showInvite, setShowInvite] = useState(false)
    const [removeTarget, setRemoveTarget] = useState<Member | null>(null)

    const { data: workspace } = useWorkspace(workspaceId!)

    const { data: members = [], isLoading } = useQuery({
        queryKey: ['members', workspaceId],
        queryFn: () => memberApi.getMembers(workspaceId!),
        enabled: !!workspaceId,
    })

    const inviteMutation = useMutation({
        mutationFn: (data: MemberInviteRequest) =>
            memberApi.invite(workspaceId!, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['members', workspaceId] })
            toast.success('Đã mời thành viên!')
            setShowInvite(false)
            reset()
        },
    })

    const removeMutation = useMutation({
        mutationFn: (userId: string) =>
            memberApi.remove(workspaceId!, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['members', workspaceId] })
            toast.success('Đã xóa thành viên!')
            setRemoveTarget(null)
        },
    })

    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<InviteForm>({
            resolver: zodResolver(inviteSchema),
            defaultValues: { role: 'MEMBER' },
        })

    const myMember = members.find((m) => m.user.email === user?.email)
    const canManage = myMember?.role === 'OWNER' || myMember?.role === 'ADMIN'

    const roleIcon = (role: string) => {
        switch (role) {
            case 'OWNER':  return <Crown className="h-3.5 w-3.5 text-purple-500" />
            case 'ADMIN':  return <Shield className="h-3.5 w-3.5 text-blue-500" />
            case 'VIEWER': return <Eye className="h-3.5 w-3.5 text-gray-400" />
            default:       return <User className="h-3.5 w-3.5 text-green-500" />
        }
    }

    if (isLoading) return <Loading text="Đang tải thành viên..." />

    return (
        <div className="max-w-3xl">
            {/* Header */}
            <div className="flex items-center gap-4 mb-6">
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
                        Thành viên
                    </h1>
                    <p className="text-sm text-gray-500">
                        {workspace?.name} · {members.length} thành viên
                    </p>
                </div>
                {canManage && (
                    <Button
                        variant="primary"
                        size="sm"
                        className="ml-auto"
                        onClick={() => setShowInvite(true)}
                    >
                        <UserPlus className="h-4 w-4" />
                        Mời thành viên
                    </Button>
                )}
            </div>

            {/* Members list */}
            <div className="card overflow-hidden">
                <ul className="divide-y divide-gray-50">
                    {members.map((member) => (
                        <li key={member.id}
                            className="flex items-center gap-4 px-5 py-4
                           hover:bg-gray-50 transition-colors">
                            <Avatar
                                name={member.user.fullName}
                                avatarUrl={member.user.avatarUrl}
                                size="md"
                            />
                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2">
                                    <p className="text-sm font-medium text-gray-900 truncate">
                                        {member.user.fullName}
                                        {member.user.email === user?.email && (
                                            <span className="ml-1.5 text-xs text-gray-400">(Bạn)</span>
                                        )}
                                    </p>
                                    {roleIcon(member.role)}
                                </div>
                                <p className="text-xs text-gray-400 truncate">
                                    {member.user.email}
                                </p>
                            </div>

                            <div className="flex items-center gap-3 flex-shrink-0">
                                <RoleBadge role={member.role} />
                                <p className="text-xs text-gray-400 hidden sm:block">
                                    {formatDate(member.joinedAt)}
                                </p>
                                {canManage
                                    && member.role !== 'OWNER'
                                    && member.user.email !== user?.email && (
                                        <button
                                            onClick={() => setRemoveTarget(member)}
                                            className="text-xs text-red-500 hover:text-red-700
                               hover:underline transition-colors"
                                        >
                                            Xóa
                                        </button>
                                    )}
                            </div>
                        </li>
                    ))}
                </ul>
            </div>

            {/* Invite Modal */}
            <Modal
                isOpen={showInvite}
                onClose={() => { setShowInvite(false); reset() }}
                title="Mời thành viên"
                description="Nhập email để mời người dùng vào workspace"
                footer={
                    <div className="flex gap-3 justify-end">
                        <Button
                            variant="secondary"
                            onClick={() => { setShowInvite(false); reset() }}
                        >
                            Hủy
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleSubmit((d) => inviteMutation.mutateAsync(d))}
                            isLoading={inviteMutation.isPending}
                        >
                            Gửi lời mời
                        </Button>
                    </div>
                }
            >
                <div className="space-y-4">
                    <Input
                        label="Email"
                        type="email"
                        placeholder="colleague@example.com"
                        error={errors.email?.message}
                        {...register('email')}
                    />

                    <div>
                        <label className="label">Vai trò</label>
                        <select
                            {...register('role')}
                            className="input"
                        >
                            <option value="MEMBER">Member — Xem và upload file</option>
                            <option value="ADMIN">Admin — Quản lý thành viên</option>
                            <option value="VIEWER">Viewer — Chỉ xem</option>
                        </select>
                    </div>
                </div>
            </Modal>

            {/* Remove confirm */}
            <ConfirmDialog
                isOpen={!!removeTarget}
                onClose={() => setRemoveTarget(null)}
                onConfirm={() =>
                    removeTarget && removeMutation.mutateAsync(removeTarget.user.id)
                }
                title="Xóa thành viên"
                message={`Bạn có chắc muốn xóa "${removeTarget?.user.fullName}" khỏi workspace?`}
                confirmText="Xóa"
                isLoading={removeMutation.isPending}
            />
        </div>
    )
}