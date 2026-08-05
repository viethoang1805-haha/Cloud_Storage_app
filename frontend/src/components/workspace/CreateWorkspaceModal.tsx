import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import { useCreateWorkspace } from '@/hooks/useWorkspace'

const schema = z.object({
    name: z.string().min(1, 'Tên không được để trống').max(255),
    description: z.string().max(1000).optional(),
})
type Form = z.infer<typeof schema>

interface Props {
    isOpen: boolean
    onClose: () => void
}

export default function CreateWorkspaceModal({ isOpen, onClose }: Props) {
    const createMutation = useCreateWorkspace()
    const { register, handleSubmit, reset, formState: { errors } } =
        useForm<Form>({ resolver: zodResolver(schema) })

    const onSubmit = async (data: Form) => {
        await createMutation.mutateAsync(data)
        reset()
        onClose()
    }

    return (
        <Modal
            isOpen={isOpen}
            onClose={() => { onClose(); reset() }}
            title="Tạo workspace mới"
            footer={
                <div className="flex gap-3 justify-end">
                    <Button variant="secondary" onClick={() => { onClose(); reset() }}>
                        Hủy
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleSubmit(onSubmit)}
                        isLoading={createMutation.isPending}
                    >
                        Tạo workspace
                    </Button>
                </div>
            }
        >
            <div className="space-y-4">
                <Input
                    label="Tên workspace"
                    placeholder="VD: Team Project 2024"
                    error={errors.name?.message}
                    required
                    {...register('name')}
                />
                <div>
                    <label className="label">Mô tả</label>
                    <textarea
                        {...register('description')}
                        rows={3}
                        placeholder="Mô tả ngắn về workspace (không bắt buộc)"
                        className="input resize-none"
                    />
                </div>
            </div>
        </Modal>
    )
}