import { Download, Trash2, Share2 } from 'lucide-react'
import { FileItem } from '@/types/file'
import { formatBytes, formatRelativeTime, getFileIcon } from '@/lib/utils'
import Avatar from '@/components/common/Avatar'

interface FileTableProps {
    files: FileItem[]
    onDownload: (file: FileItem) => void
    onDelete: (file: FileItem) => void
    onShare?: (file: FileItem) => void
}

export default function FileTable({
                                      files,
                                      onDownload,
                                      onDelete,
                                      onShare,
                                  }: FileTableProps) {
    return (
        <div className="card overflow-hidden">
            <table className="w-full text-sm">
                <thead>
                <tr className="border-b border-gray-100 bg-gray-50/50">
                    <th className="text-left px-4 py-3 text-xs font-medium
                           text-gray-500 uppercase tracking-wider">
                        Tên file
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-medium
                           text-gray-500 uppercase tracking-wider
                           hidden md:table-cell">
                        Kích thước
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-medium
                           text-gray-500 uppercase tracking-wider
                           hidden lg:table-cell">
                        Người upload
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-medium
                           text-gray-500 uppercase tracking-wider
                           hidden lg:table-cell">
                        Ngày tạo
                    </th>
                    <th className="px-4 py-3" />
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                {files.map((file) => (
                    <tr key={file.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-4 py-3">
                            <div className="flex items-center gap-3">
                  <span className="text-xl flex-shrink-0">
                    {getFileIcon(file.contentType)}
                  </span>
                                <div className="min-w-0">
                                    <p className="font-medium text-gray-900 truncate max-w-[200px]">
                                        {file.originalName}
                                    </p>
                                    {file.folder && (
                                        <p className="text-xs text-gray-400 truncate">
                                            📁 {file.folder.name}
                                        </p>
                                    )}
                                </div>
                            </div>
                        </td>
                        <td className="px-4 py-3 text-xs text-gray-500
                             hidden md:table-cell whitespace-nowrap">
                            {formatBytes(file.size)}
                        </td>
                        <td className="px-4 py-3 hidden lg:table-cell">
                            {file.uploadedBy ? (
                                <div className="flex items-center gap-2">
                                    <Avatar
                                        name={file.uploadedBy.fullName}
                                        avatarUrl={file.uploadedBy.avatarUrl}
                                        size="xs"
                                    />
                                    <span className="text-xs text-gray-600 truncate max-w-[120px]">
                      {file.uploadedBy.fullName}
                    </span>
                                </div>
                            ) : (
                                <span className="text-xs text-gray-400">—</span>
                            )}
                        </td>
                        <td className="px-4 py-3 text-xs text-gray-400
                             hidden lg:table-cell whitespace-nowrap">
                            {formatRelativeTime(file.createdAt)}
                        </td>
                        <td className="px-4 py-3">
                            <div className="flex items-center gap-1 justify-end">
                                {onShare && (
                                    <button
                                        onClick={() => onShare(file)}
                                        className="h-7 w-7 flex items-center justify-center
                                 rounded-lg text-gray-400 hover:text-purple-600
                                 hover:bg-purple-50 transition-colors"
                                        title="Chia sẻ"
                                    >
                                        <Share2 className="h-3.5 w-3.5" />
                                    </button>
                                )}
                                <button
                                    onClick={() => onDownload(file)}
                                    className="h-7 w-7 flex items-center justify-center
                               rounded-lg text-gray-400 hover:text-blue-600
                               hover:bg-blue-50 transition-colors"
                                    title="Tải xuống"
                                >
                                    <Download className="h-3.5 w-3.5" />
                                </button>
                                <button
                                    onClick={() => onDelete(file)}
                                    className="h-7 w-7 flex items-center justify-center
                               rounded-lg text-gray-400 hover:text-red-600
                               hover:bg-red-50 transition-colors"
                                    title="Xóa"
                                >
                                    <Trash2 className="h-3.5 w-3.5" />
                                </button>
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}