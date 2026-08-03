import { Outlet } from 'react-router-dom'
import { Cloud } from 'lucide-react'

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-br
                    from-primary-50 via-white to-blue-50
                    flex items-center justify-center p-4">
      <div className="w-full max-w-[400px]">

        {/* Logo */}
        <div className="flex items-center justify-center gap-3 mb-8">
          <div className="h-10 w-10 bg-primary-600 rounded-2xl
                          flex items-center justify-center shadow-lg">
            <Cloud className="h-5 w-5 text-white" />
          </div>
          <span className="text-2xl font-bold text-gray-900">
            CloudStorage
          </span>
        </div>

        {/* Card */}
        <div className="card p-8">
          <Outlet />
        </div>

        <p className="text-center text-xs text-gray-400 mt-6">
          © 2024 CloudStorage. Bảo mật & An toàn.
        </p>
      </div>
    </div>
  )
}