import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'

export default function ForgotPasswordPage() {
    return (
        <div>
            <div className="mb-7">
                <h1 className="text-2xl font-bold text-gray-900">Quên mật khẩu</h1>
                <p className="text-sm text-gray-500 mt-1.5">
                    Tính năng này đang được phát triển
                </p>
            </div>

            <div className="bg-blue-50 rounded-xl p-4 text-sm text-blue-700 mb-6">
                Vui lòng liên hệ quản trị viên để đặt lại mật khẩu.
            </div>

            <Link
                to="/login"
                className="flex items-center gap-2 text-sm text-primary-600
                   hover:text-primary-700 font-medium"
            >
                <ArrowLeft className="h-4 w-4" />
                Quay lại đăng nhập
            </Link>
        </div>
    )
}