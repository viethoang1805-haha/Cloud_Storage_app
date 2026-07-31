import { Outlet } from 'react-router-dom'
import Sidebar from '@/components/layout/Sidebar'
import Header from '@/components/layout/Header'

export default function MainLayout() {
    return (
        // (1) Layout chính: sidebar bên trái, content bên phải
        <div className="flex h-screen bg-gray-50">

            {/* Sidebar cố định bên trái */}
            <Sidebar />

            {/* Content area */}
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">

                {/* Header trên cùng */}
                <Header />

                {/* Main content — scrollable */}
                <main className="flex-1 overflow-y-auto p-6">
                    <Outlet />
                </main>

            </div>
        </div>
    )
}