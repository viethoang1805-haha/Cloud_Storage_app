import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AuthLayout from '@/layouts/AuthLayout'
import MainLayout from '@/layouts/MainLayout'
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import WorkspaceListPage from '@/pages/workspace/WorkspaceListPage'
import WorkspaceDetailPage from '@/pages/workspace/WorkspaceDetailPage'
import WorkspaceMemberPage from '@/pages/workspace/WorkspaceMemberPage'
import FileListPage from '@/pages/file/FileManagerPage'
import ProfilePage from '@/pages/profile/ProfilePage'
import NotFoundPage from '@/pages/NotFoundPage'
import PrivateRoute from './PrivateRoute'
import PublicRoute from './PublicRoute'

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Public — chỉ khi chưa đăng nhập */}
                <Route element={<PublicRoute />}>
                    <Route element={<AuthLayout />}>
                        <Route path="/login"    element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                    </Route>
                </Route>

                {/* Private — cần đăng nhập */}
                <Route element={<PrivateRoute />}>
                    <Route element={<MainLayout />}>
                        <Route index element={<Navigate to="/dashboard" replace />} />
                        <Route path="/dashboard"  element={<DashboardPage />} />
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

                {/* 404 */}
                <Route path="*" element={<NotFoundPage />} />

            </Routes>
        </BrowserRouter>
    )
}