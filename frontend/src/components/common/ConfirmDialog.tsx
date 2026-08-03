import { AlertTriangle } from 'lucide-react'
import Modal from './Modal'
import Button from './Button'

interface ConfirmDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  isLoading?: boolean
  variant?: 'danger' | 'warning'
}

export default function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  isLoading = false,
  variant = 'danger',
}: ConfirmDialogProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      size="sm"
      footer={
        <div className="flex gap-3 justify-end">
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={isLoading}
          >
            {cancelText}
          </Button>
          <Button
            variant={variant === 'danger' ? 'danger' : 'primary'}
            onClick={onConfirm}
            isLoading={isLoading}
          >
            {confirmText}
          </Button>
        </div>
      }
    >
      <div className="flex gap-3">
        <div className={`flex-shrink-0 h-10 w-10 rounded-full flex
                         items-center justify-center
                         ${variant === 'danger'
                           ? 'bg-red-100'
                           : 'bg-yellow-100'}`}>
          <AlertTriangle className={`h-5 w-5 ${
            variant === 'danger' ? 'text-red-600' : 'text-yellow-600'
          }`} />
        </div>
        <p className="text-sm text-gray-600 leading-relaxed pt-2">
          {message}
        </p>
      </div>
    </Modal>
  )
}