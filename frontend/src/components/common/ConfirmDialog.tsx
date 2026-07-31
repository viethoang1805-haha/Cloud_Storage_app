import Modal from './Modal'
import { Loader2, AlertTriangle } from 'lucide-react'

interface ConfirmDialogProps {
    isOpen: boolean
    onClose: () => void
    onConfirm: () => void
    title: string
    message: string
    confirmText?: string
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
                                          isLoading = false,
                                          variant = 'danger',
                                      }: ConfirmDialogProps) {

    const variantClasses = {
        danger: 'bg-red-600 hover:bg-red-700 text-white',
        warning: 'bg-yellow-500 hover:bg-yellow-600 text-white',
    }

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="sm">
            <div className="space-y-4">
                <div className="flex gap-3">
                    <AlertTriangle className="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
                    <p className="text-sm text-gray-600">{message}</p>
                </div>

                <div className="flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        disabled={isLoading}
                        className="px-4 py-2 text-sm font-medium text-gray-700
                       bg-gray-100 rounded-lg hover:bg-gray-200
                       disabled:opacity-50 transition-colors"
                    >
                        Hủy
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={isLoading}
                        className={`px-4 py-2 text-sm font-medium rounded-lg
                       flex items-center gap-2 disabled:opacity-50
                       transition-colors ${variantClasses[variant]}`}
                    >
                        {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
                        {confirmText}
                    </button>
                </div>
            </div>
        </Modal>
    )
}