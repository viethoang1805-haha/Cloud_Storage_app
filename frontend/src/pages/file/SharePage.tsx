import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'

export default function SharePage() {
    const { workspaceId } = useParams()
    const navigate = useNavigate()

    return (
        <div className="max-w-2xl">
            <div className="flex items-center gap-3 mb-6">
                <button
                    onClick={() => navigate(-1)}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors"
                >
                    <ArrowLeft className="h-4 w-4" />
                </button>
                <h1 className="text-xl font-bold text-gray-900">Chia sẻ file</h1>
            </div>

            <div className="card p-8 text-center">
                <p className="text-gray-500">
                    Chọn file từ trang{' '}
                    <button
                        onClick={() => navigate(`/workspaces/${workspaceId}/files`)}
                        className="text-primary-600 font-medium hover:underline"
                    >
                        Quản lý file
                    </button>{' '}
                    để chia sẻ.
                </p>
            </div>
        </div>
    )
}