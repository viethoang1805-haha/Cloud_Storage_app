import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatBytes(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatRelativeTime(dateStr: string): string {
  const now = new Date()
  const date = new Date(dateStr)
  const diff = now.getTime() - date.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  if (hours < 24) return `${hours} giờ trước`
  if (days < 7) return `${days} ngày trước`
  return formatDate(dateStr)
}

export function getFileIcon(contentType: string): string {
  if (!contentType) return '📄'
  if (contentType.startsWith('image/'))     return '🖼️'
  if (contentType.startsWith('video/'))     return '🎬'
  if (contentType.startsWith('audio/'))     return '🎵'
  if (contentType === 'application/pdf')    return '📕'
  if (contentType.includes('word'))         return '📝'
  if (contentType.includes('excel') || contentType.includes('spreadsheet')) return '📊'
  if (contentType.includes('powerpoint') || contentType.includes('presentation')) return '📈'
  if (contentType.includes('zip') || contentType.includes('rar') || contentType.includes('7z')) return '📦'
  if (contentType.startsWith('text/'))      return '📃'
  return '📄'
}

export function getRoleBadgeClass(role: string): string {
  const classes: Record<string, string> = {
    OWNER:  'badge-purple',
    ADMIN:  'badge-blue',
    MEMBER: 'badge-green',
    VIEWER: 'badge-gray',
  }
  return classes[role] ?? 'badge-gray'
}