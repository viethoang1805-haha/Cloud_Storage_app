import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import { useRenameFolder } from '@/hooks/useFolder'
import { FolderItem } from '@/types/folder'

const schema = z.object({
    name: z.string().min(1, 'Tên không được để trống').max(255),
})
type Form = z.infer<typeof schema>

interface RenameFolderModalProps {
    isOpen: boolean
    onClose: () => void
    folder: FolderItem
    workspaceId: string
}

export default function RenameFolderModal({
                                              isOpen,
                                              onClose,
                                              folder,
                                              workspaceId,
                                          }: RenameFolderModalProps) {
    const renameMutation = useRenameFolder(workspaceId)

    const { register, handleSubmit, reset, setFocus, formState: { errors } } =
        useForm<Form>({
            resolver: zodResolver(schema),
            defaultValues: { name: folder.name },
        })

    // Reset khi folder thay đổi
    useEffect(() => {
        reset({ name: folder.name })
    }, [folder.id, reset])

    // Focus input khi mở
    useEffect(() => {
        if (isOpen) {
            setTimeout(() => setFocus('name'), 100)
        }
    }, [isOpen, setFocus])

    const onSubmit = async (data: Form) => {
        if (data.name === folder.name) {
            onClose()
            return
        }
        await renameMutation.mutateAsync({
            folderId: folder.id,
            name: data.name,
        })
        onClose()
    }

    return (
        <Modal
            isOpen={isOpen}
            onClose={() => { onClose(); reset({ name: folder.name }) }}
            title="Đổi tên thư mục"
            size="sm"
            footer={
                <div className="flex gap-3 justify-end">
                    <Button
                        variant="secondary"
                        onClick={() => { onClose(); reset({ name: folder.name }) }}
                    >
                        Hủy
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleSubmit(onSubmit)}
                        isLoading={renameMutation.isPending}
                    >
                        Lưu
                    </Button>
                </div>
            }
        >
            <Input
                label="Tên thư mục"
                error={errors.name?.message}
                {...register('name')}
                onKeyDown={(e) => {
                    if (e.key === 'Enter') handleSubmit(onSubmit)()
                }}
            />
        </Modal>
    )
}