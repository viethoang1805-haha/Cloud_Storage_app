import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import {
    Download, Lock, FileText, Shield,
    ExternalLink, Cloud, Loader2,
} from 'lucide-react'
import Button from '@/components/common/Button'
import { formatBytes } from '@/lib/utils'

interface PublicFileResponse {
    fileName: string
    contentType: string
    size: number
    sizeFormatted: string
    requiresPassword: boolean
    passwordVerified: boolean
    downloadUrl: string | null
    workspaceName: string | null
    uploaderName: string | null
    createdAt: string | null
}

export default function PublicSharePage() {
    const { token } = useParams<{ token: string }>()
    const [password, setPassword] = useState('')
    const [fileInfo, setFileInfo] = useState<PublicFileResponse | null>(null)
    const [error, setError] = useState<string | null>(null)

    const accessMutation = useMutation({
        mutationFn: async (pwd?: string) => {
            const res = await axiosInstance.post<ApiResponse<PublicFileResponse>>(
                `/share/public/${token}`,
                pwd ? { password: pwd } : {}
            )
            return res.data.data
        },
        onSuccess: (data) => {
            setFileInfo(data)
            setError(null)
        },
        onError: (err: any) => {
            const msg = err?.response?.data?.message
            if (msg?.includes('Mật khẩu')) {
                setError('Mật khẩu không đúng, vui lòng thử lại')
            } else if (err?.response?.status === 404) {
                setError('Link không tồn tại hoặc đã bị thu hồi')
            } else if (err?.response?.status === 410) {
                setError('Link đã hết hạn hoặc đã đạt giới hạn tải xuống')
            } else {
                setError(msg || 'Có lỗi xảy ra, vui lòng thử lại')
            }
        },
    })

    // Auto access khi load (không cần password)
    const handleAccess = () => {
        setError(null)
        accessMutation.mutate(password || undefined)
    }

    const handleDownload = () => {
        if (fileInfo?.downloadUrl) {
            window.open(fileInfo.downloadUrl, '_blank')
        }
    }

    const getFileIcon = (contentType: string) => {
        if (contentType?.startsWith('image/')) return '🖼️'
        if (contentType === 'application/pdf') return '📕'
        if (contentType?.includes('word')) return '📝'
        if (contentType?.includes('excel') || contentType?.includes('spreadsheet')) return '📊'
        if (contentType?.includes('powerpoint')) return '📈'
        if (contentType?.startsWith('video/')) return '🎬'
        if (contentType?.startsWith('audio/')) return '🎵'
        if (contentType?.includes('zip') || contentType?.includes('rar')) return '📦'
        return '📄'
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-primary-50
                    via-white to-blue-50 flex items-center
                    justify-center p-4">
            <div className="w-full max-w-md">

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

                    {/* Chưa access → hiển thị form */}
                    {!fileInfo ? (
                        <div className="text-center">
                            <div className="h-16 w-16 bg-primary-50 rounded-2xl
                              flex items-center justify-center mx-auto mb-5">
                                <FileText className="h-8 w-8 text-primary-600" />
                            </div>

                            <h1 className="text-xl font-bold text-gray-900 mb-2">
                                File được chia sẻ
                            </h1>
                            <p className="text-sm text-gray-500 mb-6">
                                Nhập mật khẩu (nếu có) để truy cập file
                            </p>

                            {/* Error */}
                            {error && (
                                <div className="bg-red-50 border border-red-200 rounded-xl
                                p-3 mb-4 text-sm text-red-700 text-left">
                                    {error}
                                </div>
                            )}

                            {/* Password input */}
                            <div className="mb-5">
                                <div className="relative">
                                    <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2
                                   h-4 w-4 text-gray-400" />
                                    <input
                                        type="password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleAccess()}
                                        placeholder="Mật khẩu (nếu có)"
                                        className="input pl-10"
                                    />
                                </div>
                            </div>

                            <Button
                                variant="primary"
                                className="w-full"
                                onClick={handleAccess}
                                isLoading={accessMutation.isPending}
                            >
                                Truy cập file
                            </Button>
                        </div>

                    ) : fileInfo.requiresPassword && !fileInfo.passwordVerified ? (
                        /* Cần password */
                        <div className="text-center">
                            <div className="h-16 w-16 bg-yellow-50 rounded-2xl
                              flex items-center justify-center mx-auto mb-5">
                                <Shield className="h-8 w-8 text-yellow-500" />
                            </div>
                            <h2 className="text-lg font-bold text-gray-900 mb-2">
                                File được bảo vệ
                            </h2>
                            <p className="text-sm text-gray-500 mb-5">
                                File này yêu cầu mật khẩu để truy cập
                            </p>

                            {error && (
                                <div className="bg-red-50 border border-red-200 rounded-xl
                                p-3 mb-4 text-sm text-red-700">
                                    {error}
                                </div>
                            )}

                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleAccess()}
                                placeholder="Nhập mật khẩu"
                                className="input mb-3"
                                autoFocus
                            />
                            <Button
                                variant="primary"
                                className="w-full"
                                onClick={handleAccess}
                                isLoading={accessMutation.isPending}
                            >
                                Xác nhận
                            </Button>
                        </div>

                    ) : (
                        /* Đã access thành công */
                        <div>
                            {/* File info */}
                            <div className="flex items-center gap-4 p-4 bg-gray-50
                              rounded-xl mb-6">
                <span className="text-4xl flex-shrink-0">
                  {getFileIcon(fileInfo.contentType)}
                </span>
                                <div className="min-w-0 flex-1">
                                    <p className="font-semibold text-gray-900 truncate">
                                        {fileInfo.fileName}
                                    </p>
                                    <p className="text-sm text-gray-400 mt-0.5">
                                        {fileInfo.sizeFormatted}
                                    </p>
                                    {fileInfo.workspaceName && (
                                        <p className="text-xs text-gray-300 mt-0.5">
                                            📁 {fileInfo.workspaceName}
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Uploader info */}
                            {fileInfo.uploaderName && (
                                <p className="text-sm text-gray-500 mb-5 text-center">
                                    Chia sẻ bởi{' '}
                                    <span className="font-medium text-gray-700">
                    {fileInfo.uploaderName}
                  </span>
                                </p>
                            )}

                            {/* Download button */}
                            {fileInfo.downloadUrl ? (
                                <Button
                                    variant="primary"
                                    className="w-full"
                                    onClick={handleDownload}
                                >
                                    <Download className="h-4 w-4" />
                                    Tải xuống
                                </Button>
                            ) : (
                                <div className="text-center text-sm text-gray-400 py-4">
                                    <Lock className="h-5 w-5 mx-auto mb-2 text-gray-300" />
                                    Không có quyền tải xuống
                                </div>
                            )}

                            {/* Copy link */}
                            <button
                                onClick={() => {
                                    navigator.clipboard.writeText(window.location.href)
                                }}
                                className="w-full mt-3 flex items-center justify-center
                           gap-2 text-sm text-gray-400 hover:text-gray-600
                           transition-colors py-2"
                            >
                                <ExternalLink className="h-3.5 w-3.5" />
                                Copy link chia sẻ
                            </button>
                        </div>
                    )}
                </div>

                <p className="text-center text-xs text-gray-400 mt-6">
                    Được chia sẻ qua CloudStorage · Bảo mật & An toàn
                </p>
            </div>
        </div>
    )
}