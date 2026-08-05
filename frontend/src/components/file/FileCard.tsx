import { Download, Trash2, Share2 } from 'lucide-react'
import { FileItem } from '@/types/file'
import { formatBytes, formatRelativeTime, getFileIcon } from '@/lib/utils'

interface FileCardProps {
    file: FileItem
    onDownload: () => void
    onDelete: () => void
    onShare?: () => void
}

export default function FileCard({
                                     file,
                                     onDownload,
                                     onDelete,
                                     onShare,
                                 }: FileCardProps) {
    return (
        <div className="card group relative hover:shadow-md
                    transition-all duration-200 overflow-hidden">
            {/* Thumbnail */}
            <div className="aspect-square bg-gray-50 flex items-center
                      justify-center border-b border-gray-100">
                {file.isImage && file.downloadUrl ? (
                    <img
                        src={file.downloadUrl}
                        alt={file.originalName}
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <span className="text-4xl select-none">
            {getFileIcon(file.contentType)}
          </span>
                )}
            </div>

            {/* Info */}
            <div className="p-3">
                <p
                    className="text-xs font-medium text-gray-900 truncate"
                    title={file.originalName}
                >
                    {file.originalName}
                </p>
                <div className="flex items-center justify-between mt-1">
          <span className="text-xs text-gray-400">
            {formatBytes(file.size)}
          </span>
                    <span className="text-xs text-gray-400">
            {formatRelativeTime(file.createdAt)}
          </span>
                </div>
            </div>

            {/* Actions */}
            <div className="absolute top-2 right-2 flex gap-1
                      opacity-0 group-hover:opacity-100
                      transition-opacity duration-150">
                {onShare && (
                    <button
                        onClick={(e) => { e.stopPropagation(); onShare() }}
                        className="h-7 w-7 flex items-center justify-center
                       bg-white rounded-lg shadow-sm border border-gray-200
                       text-gray-500 hover:text-purple-600
                       hover:border-purple-300 transition-colors"
                        title="Chia sẻ"
                    >
                        <Share2 className="h-3.5 w-3.5" />
                    </button>
                )}
                <button
                    onClick={(e) => { e.stopPropagation(); onDownload() }}
                    className="h-7 w-7 flex items-center justify-center
                     bg-white rounded-lg shadow-sm border border-gray-200
                     text-gray-500 hover:text-blue-600
                     hover:border-blue-300 transition-colors"
                    title="Tải xuống"
                >
                    <Download className="h-3.5 w-3.5" />
                </button>
                <button
                    onClick={(e) => { e.stopPropagation(); onDelete() }}
                    className="h-7 w-7 flex items-center justify-center
                     bg-white rounded-lg shadow-sm border border-gray-200
                     text-gray-500 hover:text-red-600
                     hover:border-red-300 transition-colors"
                    title="Xóa"
                >
                    <Trash2 className="h-3.5 w-3.5" />
                </button>
            </div>
        </div>
    )
}