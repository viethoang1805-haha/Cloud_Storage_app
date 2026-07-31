import { Download, Trash2 } from 'lucide-react'
import { FileItem } from '@/types/file'
import { formatFileSize, formatRelativeTime, getFileIcon } from '@/lib/utils'

interface FileTableProps {
    files: FileItem[]
    onDownload: (file: FileItem) => void
    onDelete: (file: FileItem) => void
}

export default function FileTable({
                                      files,
                                      onDownload,
                                      onDelete,
                                  }: FileTableProps) {
    return (
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
            <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                    <th className="text-left px-4 py-3 font-medium text-gray-600">
                        Tên file
                    </th>
                    <th className="text-left px-4 py-3 font-medium text-gray-600 hidden md:table-cell">
                        Kích thước
                    </th>
                    <th className="text-left px-4 py-3 font-medium text-gray-600 hidden lg:table-cell">
                        Ngày upload
                    </th>
                    <th className="text-left px-4 py-3 font-medium text-gray-600 hidden lg:table-cell">
                        Người upload
                    </th>
                    <th className="px-4 py-3" />
                </tr>
                </thead>
                <tbody>
                {files.map((file, index) => (
                    <tr
                        key={file.id}
                        className={`border-b border-gray-100 hover:bg-gray-50
                         transition-colors ${index === files.length - 1 ? 'border-b-0' : ''}`}
                    >
                        {/* Tên file */}
                        <td className="px-4 py-3">
                            <div className="flex items-center gap-3">
                  <span className="text-xl flex-shrink-0">
                    {getFileIcon(file.contentType)}
                  </span>
                                <div className="min-w-0">
                                    <p className="font-medium text-gray-900 truncate">
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

                        {/* Size */}
                        <td className="px-4 py-3 text-gray-500 hidden md:table-cell">
                            {formatFileSize(file.size)}
                        </td>

                        {/* Date */}
                        <td className="px-4 py-3 text-gray-500 hidden lg:table-cell">
                            {formatRelativeTime(file.createdAt)}
                        </td>

                        {/* Uploader */}
                        <td className="px-4 py-3 hidden lg:table-cell">
                            <div className="flex items-center gap-2">
                                <div className="h-6 w-6 rounded-full bg-primary/10
                                  flex items-center justify-center
                                  text-primary text-xs font-semibold">
                                    {file.uploadedBy?.fullName?.charAt(0) ?? '?'}
                                </div>
                                <span className="text-gray-600 text-xs truncate max-w-[120px]">
                    {file.uploadedBy?.fullName}
                  </span>
                            </div>
                        </td>

                        {/* Actions */}
                        <td className="px-4 py-3">
                            <div className="flex items-center gap-1 justify-end">
                                <button
                                    onClick={() => onDownload(file)}
                                    className="p-1.5 rounded-lg hover:bg-blue-50
                               text-gray-400 hover:text-blue-600
                               transition-colors"
                                    title="Tải xuống"
                                >
                                    <Download className="h-4 w-4" />
                                </button>
                                <button
                                    onClick={() => onDelete(file)}
                                    className="p-1.5 rounded-lg hover:bg-red-50
                               text-gray-400 hover:text-red-600
                               transition-colors"
                                    title="Xóa"
                                >
                                    <Trash2 className="h-4 w-4" />
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