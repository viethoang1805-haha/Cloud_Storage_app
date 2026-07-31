import { Loader2 } from 'lucide-react'

interface LoadingProps {
    text?: string
    fullScreen?: boolean
}

export default function Loading({
                                    text = 'Đang tải...',
                                    fullScreen = false,
                                }: LoadingProps) {
    if (fullScreen) {
        return (
            <div className="fixed inset-0 bg-white flex items-center
                      justify-center z-50">
                <div className="text-center">
                    <Loader2 className="h-8 w-8 animate-spin text-primary mx-auto mb-3" />
                    <p className="text-sm text-gray-500">{text}</p>
                </div>
            </div>
        )
    }

    return (
        <div className="flex items-center justify-center py-12">
            <div className="text-center">
                <Loader2 className="h-6 w-6 animate-spin text-primary mx-auto mb-2" />
                <p className="text-sm text-gray-500">{text}</p>
            </div>
        </div>
    )
}