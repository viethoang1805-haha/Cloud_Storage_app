import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

// Layouts
import AuthLayout from '@/layouts/AuthLayout'
import MainLayout from '@/layouts/MainLayout'

// Auth pages
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'

// Main pages
import DashboardPage from '@/pages/dashboard/DashboardPage'
import WorkspaceListPage from '@/pages/workspace/WorkspaceListPage'
import WorkspaceDetailPage from '@/pages/workspace/WorkspaceDetailPage'
import WorkspaceMemberPage from '@/pages/workspace/WorkspaceMemberPage'
import FileListPage from '@/pages/file/FileListPage'
import ProfilePage from '@/pages/profile/ProfilePage'
import NotFoundPage from '@/pages/NotFoundPage'

// Route guards
import PrivateRoute from './PrivateRoute'
import PublicRoute from './PublicRoute'

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                {/* (1) Public routes — chỉ truy cập khi CHƯA đăng nhập */}
                <Route element={<PublicRoute />}>
                    <Route element={<AuthLayout />}>
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                    </Route>
                </Route>

                {/* (2) Private routes — cần đăng nhập */}
                <Route element={<PrivateRoute />}>
                    <Route element={<MainLayout />}>
                        <Route path="/" element={<Navigate to="/dashboard" replace />} />
                        <Route path="/dashboard" element={<DashboardPage />} />
                        <Route path="/workspaces" element={<WorkspaceListPage />} />
                        <Route path="/workspaces/:workspaceId"
                               element={<WorkspaceDetailPage />} />
                        <Route path="/workspaces/:workspaceId/members"
                               element={<WorkspaceMemberPage />} />
                        <Route path="/workspaces/:workspaceId/files"
                               element={<FileListPage />} />
                        <Route path="/profile" element={<ProfilePage />} />
                    </Route>
                </Route>

                {/* (3) Share link public — không cần đăng nhập */}
                <Route path="/share/:token" element={<div>SharePage (TODO)</div>} />

                {/* 404 */}
                <Route path="*" element={<NotFoundPage />} />

            </Routes>
        </BrowserRouter>
    )
}