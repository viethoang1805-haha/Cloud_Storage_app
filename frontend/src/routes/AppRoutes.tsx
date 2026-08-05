import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AuthLayout from '@/layouts/AuthLayout'
import MainLayout from '@/layouts/MainLayout'
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import WorkspaceListPage from '@/pages/workspace/WorkspaceListPage'
import WorkspaceDetailPage from '@/pages/workspace/WorkspaceDetailPage'
import WorkspaceMemberPage from '@/pages/workspace/WorkspaceMemberPage'
import FolderListPage from '@/pages/folder/FolderListPage'
import FileListPage from '@/pages/file/FileListPage'
import UploadPage from '@/pages/file/UploadPage'
import SharePage from '@/pages/file/SharePage'
import ProfilePage from '@/pages/profile/ProfilePage'
import NotFoundPage from '@/pages/NotFoundPage'
import PrivateRoute from './PrivateRoute'
import PublicRoute from './PublicRoute'
import AdminDashboardPage from '@/pages/admin/AdminDashboardPage'
import AdminUsersPage from '@/pages/admin/AdminUsersPage'
import AdminRoute from './AdminRoute'

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Public */}
                <Route element={<PublicRoute />}>
                    <Route element={<AuthLayout />}>
                        <Route path="/login"           element={<LoginPage />} />
                        <Route path="/register"        element={<RegisterPage />} />
                        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    </Route>
                </Route>

                {/* Private */}
                <Route element={<PrivateRoute />}>
                    <Route element={<MainLayout />}>
                        <Route index element={<Navigate to="/dashboard" replace />} />
                        <Route path="/dashboard" element={<DashboardPage />} />

                        <Route element={<AdminRoute />}>
                            <Route path="/admin"          element={<AdminDashboardPage />} />
                            <Route path="/admin/users"    element={<AdminUsersPage />} />
                        </Route>
                        {/* Workspace */}
                        <Route path="/workspaces" element={<WorkspaceListPage />} />
                        <Route path="/workspaces/:workspaceId"
                               element={<WorkspaceDetailPage />} />
                        <Route path="/workspaces/:workspaceId/members"
                               element={<WorkspaceMemberPage />} />

                        {/* Folder */}
                        <Route path="/workspaces/:workspaceId/folders"
                               element={<FolderListPage />} />

                        {/* File */}
                        <Route path="/workspaces/:workspaceId/files"
                               element={<FileListPage />} />
                        <Route path="/workspaces/:workspaceId/upload"
                               element={<UploadPage />} />
                        <Route path="/workspaces/:workspaceId/share"
                               element={<SharePage />} />

                        {/* Profile */}
                        <Route path="/profile" element={<ProfilePage />} />
                    </Route>
                </Route>

                {/* 404 */}
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </BrowserRouter>
    )
}