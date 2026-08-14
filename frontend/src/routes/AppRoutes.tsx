// src/routes/AppRoutes.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AuthLayout from '@/layouts/AuthLayout'
import MainLayout from '@/layouts/MainLayout'

// Auth
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage'

// Main
import DashboardPage from '@/pages/dashboard/DashboardPage'

// Workspace
import WorkspaceListPage from '@/pages/workspace/WorkspaceListPage'
import WorkspaceDetailPage from '@/pages/workspace/WorkspaceDetailPage'
import WorkspaceMemberPage from '@/pages/workspace/WorkspaceMemberPage'
import WorkspaceDashboardPage from '@/pages/workspace/WorkspaceDashboardPage'

// File
import FileListPage from '@/pages/file/FileListPage'
import UploadPage from '@/pages/file/UploadPage'

// Activity
import ActivityPage from '@/pages/activity/ActivityPage'

// Profile
import ProfilePage from '@/pages/profile/ProfilePage'

// Admin
import AdminDashboardPage from '@/pages/admin/AdminDashboardPage'
import AdminUsersPage from '@/pages/admin/AdminUsersPage'
import AdminActivitiesPage from '@/pages/admin/AdminActivitiesPage'

// Not found
import NotFoundPage from '@/pages/NotFoundPage'

// Guards
import PrivateRoute from './PrivateRoute'
import PublicRoute from './PublicRoute'
import AdminRoute from './AdminRoute'

import PublicSharePage from '@/pages/share/PublicSharePage'

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                {/* ===== PUBLIC ===== */}
                <Route element={<PublicRoute />}>
                    <Route element={<AuthLayout />}>
                        <Route path="/login"           element={<LoginPage />} />
                        <Route path="/register"        element={<RegisterPage />} />
                        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                        <Route path="/share/:token" element={<PublicSharePage />} />
                    </Route>
                </Route>

                {/* ===== PRIVATE ===== */}
                <Route element={<PrivateRoute />}>
                    <Route element={<MainLayout />}>

                        {/* Redirect root */}
                        <Route index element={<Navigate to="/dashboard" replace />} />

                        {/* Dashboard */}
                        <Route path="/dashboard" element={<DashboardPage />} />

                        {/* Workspaces */}
                        <Route path="/workspaces" element={<WorkspaceListPage />} />
                        <Route path="/workspaces/:workspaceId"
                               element={<WorkspaceDetailPage />} />
                        <Route path="/workspaces/:workspaceId/members"
                               element={<WorkspaceMemberPage />} />
                        <Route path="/workspaces/:workspaceId/dashboard"
                               element={<WorkspaceDashboardPage />} />

                        {/* Files */}
                        <Route path="/workspaces/:workspaceId/files"
                               element={<FileListPage />} />
                        <Route path="/workspaces/:workspaceId/upload"
                               element={<UploadPage />} />

                        {/* Activity */}
                        <Route path="/workspaces/:workspaceId/activities"
                               element={<ActivityPage />} />

                        {/* Profile */}
                        <Route path="/profile" element={<ProfilePage />} />

                    </Route>

                    {/* ===== ADMIN ===== */}
                    <Route element={<MainLayout />}>
                        <Route element={<AdminRoute />}>
                            <Route path="/admin"
                                   element={<AdminDashboardPage />} />
                            <Route path="/admin/users"
                                   element={<AdminUsersPage />} />
                            <Route path="/admin/activities"
                                   element={<AdminActivitiesPage />} />
                        </Route>
                    </Route>

                </Route>

                {/* 404 */}
                <Route path="*" element={<NotFoundPage />} />

            </Routes>
        </BrowserRouter>
    )
}