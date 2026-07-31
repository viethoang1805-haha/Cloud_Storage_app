import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

// (1) Utility function kết hợp Tailwind classes
// Dùng để merge classes có điều kiện
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

// (2) Format file size
export function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

// (3) Format date tiếng Việt
export function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}

// (4) Format relative time ("3 phút trước")
export function formatRelativeTime(dateStr: string): string {
    const now = new Date()
    const date = new Date(dateStr)
    const diffMs = now.getTime() - date.getTime()
    const diffSeconds = Math.floor(diffMs / 1000)
    const diffMinutes = Math.floor(diffSeconds / 60)
    const diffHours = Math.floor(diffMinutes / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffSeconds < 60) return 'Vừa xong'
    if (diffMinutes < 60) return `${diffMinutes} phút trước`
    if (diffHours < 24) return `${diffHours} giờ trước`
    if (diffDays < 7) return `${diffDays} ngày trước`

    return formatDate(dateStr)
}

// (5) Lấy icon cho file type
export function getFileIcon(contentType: string): string {
    if (contentType.startsWith('image/')) return '🖼️'
    if (contentType.startsWith('video/')) return '🎬'
    if (contentType.startsWith('audio/')) return '🎵'
    if (contentType === 'application/pdf') return '📄'
    if (contentType.includes('word')) return '📝'
    if (contentType.includes('excel') || contentType.includes('spreadsheet')) return '📊'
    if (contentType.includes('powerpoint') || contentType.includes('presentation')) return '📊'
    if (contentType.includes('zip') || contentType.includes('rar')) return '📦'
    return '📁'
}