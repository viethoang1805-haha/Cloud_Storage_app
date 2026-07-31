import { Download, Trash2, FileText } from 'lucide-react'
import { FileItem } from '@/types/file'
import { formatFileSize, formatRelativeTime, getFileIcon } from '@/lib/utils'

interface FileCardProps {
    file: FileItem
    onDownload: () => void
    onDelete: () => void
}

export default function FileCard({ file, onDownload, onDelete }: FileCardProps) {
    return (
        <div className="bg-white border border-gray-200 rounded-xl p-4
                    hover:border-primary/40 hover:shadow-sm
                    transition-all group relative">

            {/* File icon / preview */}
            <div className="aspect-square rounded-lg bg-gray-50 flex items-center
                      justify-center mb-3 overflow-hidden">
                {file.isImage && file.downloadUrl ? (
                    <img
                        src={file.downloadUrl}
                        alt={file.originalName}
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <span className="text-3xl">
            {getFileIcon(file.contentType)}
          </span>
                )}
            </div>

            {/* File info */}
            <div className="space-y-1">
                <p className="text-sm font-medium text-gray-900 truncate"
                   title={file.originalName}>
                    {file.originalName}
                </p>
                <div className="flex items-center justify-between">
          <span className="text-xs text-gray-400">
            {formatFileSize(file.size)}
          </span>
                    <span className="text-xs text-gray-400">
            {formatRelativeTime(file.createdAt)}
          </span>
                </div>
            </div>

            {/* Action buttons — hiện khi hover */}
            <div className="absolute top-2 right-2 flex gap-1
                      opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                    onClick={(e) => { e.stopPropagation(); onDownload() }}
                    className="p-1.5 bg-white border border-gray-200 rounded-lg
                     hover:bg-blue-50 hover:border-blue-300
                     text-gray-500 hover:text-blue-600 transition-colors"
                    title="Tải xuống"
                >
                    <Download className="h-3.5 w-3.5" />
                </button>
                <button
                    onClick={(e) => { e.stopPropagation(); onDelete() }}
                    className="p-1.5 bg-white border border-gray-200 rounded-lg
                     hover:bg-red-50 hover:border-red-300
                     text-gray-500 hover:text-red-600 transition-colors"
                    title="Xóa"
                >
                    <Trash2 className="h-3.5 w-3.5" />
                </button>
            </div>
        </div>
    )
}