import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
    ArrowLeft, UserPlus, Crown, Shield,
    Eye, User, LogOut,
} from 'lucide-react'
import {
    useMembers,
    useInviteMember,
    useUpdateMemberRole,
    useRemoveMember,
    useLeaveWorkspace,
} from '@/hooks/useMember'
import { useWorkspace } from '@/hooks/useWorkspace'
import { useAuthStore } from '@/store/auth.store'
import Avatar from '@/components/common/Avatar'
import Button from '@/components/common/Button'
import Modal from '@/components/common/Modal'
import Input from '@/components/common/Input'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import Loading from '@/components/common/Loading'
import { RoleBadge } from '@/components/common/Badge'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { formatDate, cn } from '@/lib/utils'
// (1) Import từ types/ không phải api/
import { Member } from '@/types/member'

const inviteSchema = z.object({
    email: z.string().email('Email không hợp lệ'),
    role: z.enum(['ADMIN', 'MEMBER', 'VIEWER']),
})
type InviteForm = z.infer<typeof inviteSchema>

export default function WorkspaceMemberPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()
    const { user } = useAuthStore()

    const [showInvite, setShowInvite] = useState(false)
    // (2) Dùng Member từ types/ — không conflict
    const [removeTarget, setRemoveTarget] = useState<Member | null>(null)
    const [leaveConfirm, setLeaveConfirm] = useState(false)

    const { data: workspace } = useWorkspace(workspaceId!)
    const { data: members = [], isLoading } = useMembers(workspaceId!)

    const inviteMutation = useInviteMember(workspaceId!)
    const updateRoleMutation = useUpdateMemberRole(workspaceId!)
    const removeMutation = useRemoveMember(workspaceId!)
    const leaveMutation = useLeaveWorkspace(workspaceId!)

    const myMember = members.find((m) => m.user.email === user?.email)
    const canManage = myMember?.role === 'OWNER' || myMember?.role === 'ADMIN'
    const isOwner = myMember?.role === 'OWNER'

    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<InviteForm>({
            resolver: zodResolver(inviteSchema),
            defaultValues: { role: 'MEMBER' },
        })

    const onInvite = async (data: InviteForm) => {
        await inviteMutation.mutateAsync(data)
        reset()
        setShowInvite(false)
    }

    const getRoleIcon = (role: string) => {
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
                <div className="flex-1">
                    <h1 className="text-xl font-bold text-gray-900">Thành viên</h1>
                    <p className="text-sm text-gray-500">
                        {workspace?.name} · {members.length} thành viên
                    </p>
                </div>
                <div className="flex gap-2">
                    {myMember && !isOwner && (
                        <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => setLeaveConfirm(true)}
                        >
                            <LogOut className="h-4 w-4" />
                            Rời workspace
                        </Button>
                    )}
                    {canManage && (
                        <Button
                            variant="primary"
                            size="sm"
                            onClick={() => setShowInvite(true)}
                        >
                            <UserPlus className="h-4 w-4" />
                            Mời thành viên
                        </Button>
                    )}
                </div>
            </div>

            {/* Members list */}
            <div className="card overflow-hidden">
                <div className="px-5 py-3 border-b border-gray-100 bg-gray-50/50">
                    <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                        Danh sách thành viên
                    </p>
                </div>
                <ul className="divide-y divide-gray-50">
                    {members.map((member) => {
                        const isMe = member.user.email === user?.email
                        const canChangeRole =
                            canManage &&
                            !isMe &&
                            member.role !== 'OWNER' &&
                            !(myMember?.role === 'ADMIN' && member.role === 'ADMIN')
                        const canRemove =
                            canManage &&
                            !isMe &&
                            member.role !== 'OWNER' &&
                            !(myMember?.role === 'ADMIN' && member.role === 'ADMIN')

                        return (
                            <li
                                key={member.id}
                                className="flex items-center gap-4 px-5 py-4
                           hover:bg-gray-50 transition-colors"
                            >
                                <Avatar
                                    name={member.user.fullName}
                                    avatarUrl={member.user.avatarUrl}
                                    size="md"
                                />
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center gap-2">
                                        <p className="text-sm font-medium text-gray-900 truncate">
                                            {member.user.fullName}
                                        </p>
                                        {getRoleIcon(member.role)}
                                        {isMe && (
                                            <span className="text-xs text-gray-400">(Bạn)</span>
                                        )}
                                    </div>
                                    <p className="text-xs text-gray-400 truncate">
                                        {member.user.email}
                                    </p>
                                </div>

                                <div className="flex items-center gap-3 flex-shrink-0">
                                    {canChangeRole ? (
                                        <select
                                            value={member.role}
                                            onChange={(e) =>
                                                updateRoleMutation.mutate({
                                                    userId: member.user.id,
                                                    role: e.target.value,
                                                })
                                            }
                                            className="text-xs border border-gray-200 rounded-lg
                                 px-2 py-1 text-gray-700 bg-white
                                 focus:outline-none focus:ring-1
                                 focus:ring-primary-500"
                                        >
                                            <option value="VIEWER">VIEWER</option>
                                            <option value="MEMBER">MEMBER</option>
                                            <option value="ADMIN">ADMIN</option>
                                        </select>
                                    ) : (
                                        <RoleBadge role={member.role} />
                                    )}

                                    <p className="text-xs text-gray-400 hidden sm:block">
                                        {formatDate(member.joinedAt)}
                                    </p>

                                    {canRemove && (
                                        <button
                                            // (3) setRemoveTarget nhận Member từ types/ — đúng type
                                            onClick={() => setRemoveTarget(member)}
                                            className="text-xs text-red-400 hover:text-red-600
                                 transition-colors font-medium"
                                        >
                                            Xóa
                                        </button>
                                    )}
                                </div>
                            </li>
                        )
                    })}
                </ul>
            </div>

            {/* Invite modal */}
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
                            onClick={handleSubmit(onInvite)}
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
                        required
                        {...register('email')}
                    />
                    <div>
                        <label className="label">Vai trò</label>
                        <select {...register('role')} className="input">
                            <option value="VIEWER">Viewer — Chỉ xem</option>
                            <option value="MEMBER">Member — Xem và upload</option>
                            <option value="ADMIN">Admin — Quản lý thành viên</option>
                        </select>
                        <p className="text-xs text-gray-400 mt-1">
                            OWNER là vai trò cao nhất, không thể gán qua lời mời
                        </p>
                    </div>
                </div>
            </Modal>

            {/* Remove confirm */}
            <ConfirmDialog
                isOpen={!!removeTarget}
                onClose={() => setRemoveTarget(null)}
                onConfirm={async () => {
                    if (!removeTarget) return
                    await removeMutation.mutateAsync(removeTarget.user.id)
                    setRemoveTarget(null)
                }}
                title="Xóa thành viên"
                message={`Xóa "${removeTarget?.user.fullName}" khỏi workspace?`}
                confirmText="Xóa"
                isLoading={removeMutation.isPending}
            />

            {/* Leave confirm */}
            <ConfirmDialog
                isOpen={leaveConfirm}
                onClose={() => setLeaveConfirm(false)}
                onConfirm={() => leaveMutation.mutateAsync()}
                title="Rời workspace"
                message="Bạn sẽ không còn truy cập được workspace này nữa."
                confirmText="Rời workspace"
                isLoading={leaveMutation.isPending}
                variant="warning"
            />
        </div>
    )
}