import { Link } from 'react-router-dom'
import { Cloud, Home } from 'lucide-react'

export default function NotFoundPage() {
    return (
        <div className="min-h-screen bg-gray-50 flex items-center
                    justify-center p-4">
            <div className="text-center">
                <Cloud className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h1 className="text-6xl font-bold text-gray-200 mb-2">404</h1>
                <h2 className="text-xl font-semibold text-gray-700 mb-2">
                    Trang không tồn tại
                </h2>
                <p className="text-gray-500 mb-6">
                    Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
                </p>
                <Link
                    to="/dashboard"
                    className="inline-flex items-center gap-2 bg-primary text-white
                     px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors"
                >
                    <Home className="h-4 w-4" />
                    Về trang chủ
                </Link>
            </div>
        </div>
    )
}