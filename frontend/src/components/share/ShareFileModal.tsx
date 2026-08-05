import { useState } from 'react'
import {
    Copy, Link, Users, X, Check,
    Shield, Loader2, ExternalLink,
} from 'lucide-react'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import Avatar from '@/components/common/Avatar'
import {
    useShareLink,
    useCreateShareLink,
    useDeactivateShareLink,
    useFilePermissions,
    useShareWithUser,
    useRevokePermission,
} from '@/hooks/useShare'
import { FileItem } from '@/types/file'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { cn, formatDate } from '@/lib/utils'
import toast from 'react-hot-toast'

interface ShareFileModalProps {
    isOpen: boolean
    onClose: () => void
    file: FileItem
    workspaceId: string
}

const shareSchema = z.object({
    email: z.string().email('Email không hợp lệ'),
    permission: z.enum(['VIEW', 'DOWNLOAD', 'EDIT', 'DELETE']),
})
type ShareForm = z.infer<typeof shareSchema>

type Tab = 'link' | 'users'

const PERMISSION_INFO = {
    VIEW:     { label: 'Chỉ xem',    color: 'badge-gray'   },
    DOWNLOAD: { label: 'Tải xuống',  color: 'badge-blue'   },
    EDIT:     { label: 'Chỉnh sửa', color: 'badge-green'  },
    DELETE:   { label: 'Xóa',        color: 'badge-red'    },
}

export default function ShareFileModal({
                                           isOpen,
                                           onClose,
                                           file,
                                           workspaceId,
                                       }: ShareFileModalProps) {
    const [tab, setTab] = useState<Tab>('link')
    const [copied, setCopied] = useState(false)

    // (1) data có thể là ShareLink | null | undefined
    const { data: shareLink, isLoading: linkLoading } =
        useShareLink(workspaceId, file.id)

    const { data: permissions = [], isLoading: permLoading } =
        useFilePermissions(workspaceId, file.id)

    const createLink = useCreateShareLink(workspaceId, file.id)
    const deactivateLink = useDeactivateShareLink(workspaceId, file.id)
    const shareWithUser = useShareWithUser(workspaceId, file.id)
    const revokePermission = useRevokePermission(workspaceId, file.id)

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ShareForm>({
        resolver: zodResolver(shareSchema),
        defaultValues: { permission: 'VIEW' },
    })

    const handleCopyLink = async () => {
        if (!shareLink?.shareUrl) return
        try {
            await navigator.clipboard.writeText(shareLink.shareUrl)
            setCopied(true)
            toast.success('Đã copy link!')
            setTimeout(() => setCopied(false), 2000)
        } catch {
            toast.error('Không copy được, hãy copy thủ công')
        }
    }

    const onShareSubmit = async (data: ShareForm) => {
        await shareWithUser.mutateAsync(data)
        reset()
    }

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Chia sẻ file"
            description={file.originalName}
            size="lg"
        >
            {/* Tabs */}
            <div className="flex border-b border-gray-100 mb-5 -mt-2">
                {[
                    { key: 'link' as Tab, label: 'Link công khai', icon: Link },
                    { key: 'users' as Tab, label: 'Chia sẻ riêng', icon: Users },
                ].map(({ key, label, icon: Icon }) => (
                    <button
                        key={key}
                        onClick={() => setTab(key)}
                        className={cn(
                            'flex items-center gap-2 px-4 py-2.5 text-sm font-medium',
                            'border-b-2 transition-colors -mb-px',
                            tab === key
                                ? 'border-primary-600 text-primary-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        )}
                    >
                        <Icon className="h-4 w-4" />
                        {label}
                    </button>
                ))}
            </div>
            {/* ===== TAB: LINK ===== */}
            {tab === 'link' && (
                <div className="space-y-4">
                    {linkLoading ? (
                        <div className="flex items-center justify-center py-8">
                            <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                        </div>
                    ) : shareLink?.isActive ? (
                        <>
                            {/* Active status */}
                            <div className="flex items-center gap-2 rounded-xl bg-green-50 px-4 py-3 text-sm text-green-700">
                                <Check className="h-4 w-4 flex-shrink-0" />
                                Link đang hoạt động
                            </div>

                            {/* Link */}
                            <div className="flex gap-2">
                                <input
                                    value={shareLink.shareUrl}
                                    readOnly
                                    onClick={(e) => e.currentTarget.select()}
                                    className="input flex-1 cursor-text bg-gray-50 font-mono text-xs"
                                />

                                <Button
                                    variant="secondary"
                                    onClick={handleCopyLink}
                                    className="flex-shrink-0 gap-1.5"
                                >
                                    {copied ? (
                                        <Check className="h-4 w-4 text-green-500" />
                                    ) : (
                                        <Copy className="h-4 w-4" />
                                    )}
                                    {copied ? 'Đã copy' : 'Copy'}
                                </Button>

                                <a
                                    href={shareLink.shareUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="btn-secondary flex flex-shrink-0 items-center justify-center"
                                    title="Mở link"
                                >
                                    <ExternalLink className="h-4 w-4" />
                                </a>
                            </div>

                            {/* Stats */}
                            <div className="grid grid-cols-2 gap-3">
                                <div className="rounded-xl bg-gray-50 p-3 text-center">
                                    <p className="text-2xl font-bold text-gray-900">
                                        {shareLink.downloadCount}
                                    </p>
                                    <p className="mt-0.5 text-xs text-gray-400">
                                        Lượt truy cập
                                    </p>
                                </div>

                                <div className="rounded-xl bg-gray-50 p-3 text-center">
                                    <p className="text-2xl font-bold text-gray-900">
                                        {shareLink.maxDownloads ?? '∞'}
                                    </p>
                                    <p className="mt-0.5 text-xs text-gray-400">
                                        Giới hạn
                                    </p>
                                </div>
                            </div>

                            {/* Badges */}
                            <div className="flex flex-wrap gap-2">
                                {shareLink.hasPassword && (
                                    <span className="badge-yellow flex items-center gap-1 text-xs">
                            <Shield className="h-3 w-3" />
                            Có mật khẩu
                        </span>
                                )}

                                {shareLink.expiresAt && (
                                    <span className="badge-gray text-xs">
                            ⏰ Hết hạn: {formatDate(shareLink.expiresAt)}
                        </span>
                                )}
                            </div>

                            {/* Revoke */}
                            <div className="border-t border-gray-100 pt-2">
                                <Button
                                    variant="danger"
                                    size="sm"
                                    onClick={() => deactivateLink.mutate()}
                                    isLoading={deactivateLink.isPending}
                                >
                                    <X className="h-3.5 w-3.5" />
                                    Thu hồi link
                                </Button>
                            </div>
                        </>
                    ) : (
                        <div className="py-10 text-center">
                            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gray-100">
                                <Link className="h-7 w-7 text-gray-400" />
                            </div>

                            <h3 className="mb-1 font-semibold text-gray-900">
                                Chưa có link chia sẻ
                            </h3>

                            <p className="mb-6 text-sm text-gray-400">
                                Tạo link để chia sẻ file với bất kỳ ai
                            </p>

                            <Button
                                variant="primary"
                                onClick={() => createLink.mutate({})}
                                isLoading={createLink.isPending}
                            >
                                <Link className="h-4 w-4" />
                                Tạo link chia sẻ
                            </Button>
                        </div>
                    )}
                </div>
            )}

            {/* ===== TAB: USERS ===== */}
            {tab === 'users' && (
                <div className="space-y-5">
                    {/* Invite form */}
                    <div className="rounded-xl bg-gray-50 p-4">
                        <p className="mb-3 text-xs font-semibold text-gray-600">
                            Mời người dùng cụ thể
                        </p>

                        <form
                            onSubmit={handleSubmit(onShareSubmit)}
                            className="space-y-3"
                        >
                            <Input
                                label="Email"
                                type="email"
                                placeholder="colleague@example.com"
                                error={errors.email?.message}
                                {...register('email')}
                            />

                            <div className="flex gap-3">
                                <div className="flex-1">
                                    <label className="label">Quyền truy cập</label>

                                    <select
                                        {...register('permission')}
                                        className="input"
                                    >
                                        <option value="VIEW">👁 Chỉ xem</option>
                                        <option value="DOWNLOAD">⬇️ Tải xuống</option>
                                        <option value="EDIT">✏️ Chỉnh sửa</option>
                                        <option value="DELETE">🗑 Xóa</option>
                                    </select>
                                </div>

                                <div className="flex items-end">
                                    <Button
                                        type="submit"
                                        variant="primary"
                                        isLoading={shareWithUser.isPending}
                                    >
                                        Chia sẻ
                                    </Button>
                                </div>
                            </div>
                        </form>
                    </div>

                    {/* Permissions */}
                    {permLoading ? (
                        <div className="flex justify-center py-4">
                            <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
                        </div>
                    ) : permissions.length > 0 ? (
                        <div>
                            <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-gray-500">
                                Đã chia sẻ với {permissions.length} người
                            </p>

                            <ul className="divide-y divide-gray-50">
                                {permissions.map((perm) => {
                                    const info = PERMISSION_INFO[perm.permission]

                                    return (
                                        <li
                                            key={perm.id}
                                            className="flex items-center gap-3 py-3 first:pt-0 last:pb-0"
                                        >
                                            <Avatar
                                                name={perm.user.fullName}
                                                avatarUrl={perm.user.avatarUrl}
                                                size="sm"
                                            />

                                            <div className="min-w-0 flex-1">
                                                <p className="truncate text-sm font-medium text-gray-900">
                                                    {perm.user.fullName}
                                                </p>

                                                <p className="truncate text-xs text-gray-400">
                                                    {perm.user.email}
                                                </p>
                                            </div>

                                            <div className="flex flex-shrink-0 items-center gap-2">
                                    <span className={cn('badge', info.color)}>
                                        {info.label}
                                    </span>

                                                {perm.isExpired && (
                                                    <span className="badge-red text-xs">
                                            Hết hạn
                                        </span>
                                                )}

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        revokePermission.mutate(perm.user.id)
                                                    }
                                                    className="rounded p-1 text-gray-300 transition-colors hover:text-red-500"
                                                    title="Thu hồi quyền"
                                                >
                                                    <X className="h-3.5 w-3.5" />
                                                </button>
                                            </div>
                                        </li>
                                    )
                                })}
                            </ul>
                        </div>
                    ) : (
                        <div className="py-6 text-center text-gray-400">
                            <Users className="mx-auto mb-2 h-8 w-8 text-gray-200" />
                            <p className="text-sm">Chưa chia sẻ với ai</p>
                        </div>
                    )}
                </div>
            )}
        </Modal>
    )
}