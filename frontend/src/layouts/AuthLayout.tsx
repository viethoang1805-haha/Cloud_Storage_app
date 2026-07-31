import { Outlet } from 'react-router-dom'
import { Cloud } from 'lucide-react'

export default function AuthLayout() {
    return (
        // (1) Full screen với gradient background
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100
                    flex items-center justify-center p-4">

            <div className="w-full max-w-md">

                {/* Logo */}
                <div className="flex items-center justify-center gap-2 mb-8">
                    <Cloud className="h-8 w-8 text-primary" />
                    <span className="text-2xl font-bold text-gray-900">
            CloudStorage
          </span>
                </div>

                {/* Card chứa form */}
                <div className="bg-white rounded-xl shadow-lg p-8">
                    {/* (2) Outlet render LoginPage hoặc RegisterPage */}
                    <Outlet />
                </div>

            </div>
        </div>
    )
}