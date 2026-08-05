import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { memberApi } from '@/api/member.api'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import toast from 'react-hot-toast'

const schema = z.object({
    email: z.string().email('Email không hợp lệ'),
    role: z.enum(['ADMIN', 'MEMBER', 'VIEWER']),
})
type Form = z.infer<typeof schema>

interface Props {
    isOpen: boolean
    onClose: () => void
    workspaceId: string
}

export default function InviteMemberModal({ isOpen, onClose, workspaceId }: Props) {
    const queryClient = useQueryClient()

    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<Form>({
            resolver: zodResolver(schema),
            defaultValues: { role: 'MEMBER' },
        })

    const inviteMutation = useMutation({
        mutationFn: (data: Form) => memberApi.invite(workspaceId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['members', workspaceId] })
            toast.success('Đã mời thành viên!')
            reset()
            onClose()
        },
    })

    return (
        <Modal
            isOpen={isOpen}
            onClose={() => { onClose(); reset() }}
            title="Mời thành viên"
            description="Nhập email để mời người dùng vào workspace này"
            footer={
                <div className="flex gap-3 justify-end">
                    <Button variant="secondary" onClick={() => { onClose(); reset() }}>
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
                    required
                    {...register('email')}
                />

                <div>
                    <label className="label">Vai trò</label>
                    <select {...register('role')} className="input">
                        <option value="VIEWER">Viewer — Chỉ xem</option>
                        <option value="MEMBER">Member — Xem và upload file</option>
                        <option value="ADMIN">Admin — Quản lý thành viên</option>
                    </select>
                    <p className="text-xs text-gray-400 mt-1.5">
                        OWNER là vai trò cao nhất, không thể gán qua lời mời
                    </p>
                </div>
            </div>
        </Modal>
    )
}