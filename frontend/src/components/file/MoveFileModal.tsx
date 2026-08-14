import { useState } from 'react'
import { FolderOpen, Folder as FolderIcon, Loader2, ArrowRight } from 'lucide-react'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import { useRootFolders } from '@/hooks/useFolder'
import { useMutation, useQueryClient, useQuery } from '@tanstack/react-query'
import { fileApi } from '@/api/file.api'
import { folderApi } from '@/api/folder.api'
import { FileItem } from '@/types/file'
import { FolderItem } from '@/types/folder'
import { cn } from '@/lib/utils'
import { fileKeys } from '@/hooks/useFile'
import toast from 'react-hot-toast'

interface MoveFileModalProps {
    isOpen: boolean
    onClose: () => void
    file: FileItem
    workspaceId: string
}

export default function MoveFileModal({
                                          isOpen,
                                          onClose,
                                          file,
                                          workspaceId,
                                      }: MoveFileModalProps) {
    const queryClient = useQueryClient()
    // null = root, string = folderId
    const [selectedFolderId, setSelectedFolderId] = useState<string | null>(
        file.folder?.id ?? null
    )
    const [browseFolderId, setBrowseFolderId] = useState<string | null>(null)
    const [breadcrumb, setBreadcrumb] = useState<{ id: string; name: string }[]>([])

    const { data: rootFolders = [] } = useRootFolders(workspaceId)

    // Load folder con khi browse
    const { data: childFolders = [], isLoading: childLoading } = useQuery({
        queryKey: ['folders', workspaceId, browseFolderId, 'children'],
        queryFn: () => folderApi.getChildren(workspaceId, browseFolderId!),
        enabled: !!browseFolderId,
    })

    const displayFolders = browseFolderId ? childFolders : rootFolders

    const moveMutation = useMutation({
        mutationFn: () =>
            fileApi.move(workspaceId, file.id, selectedFolderId ?? undefined),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: fileKeys.workspace(workspaceId),
            })
            toast.success('Di chuyển file thành công!')
            onClose()
        },
    })

    const handleBrowseInto = (folder: FolderItem) => {
        setBrowseFolderId(folder.id)
        setBreadcrumb((prev) => [...prev, { id: folder.id, name: folder.name }])
    }

    const handleBreadcrumbClick = (index: number) => {
        if (index === -1) {
            // Root
            setBrowseFolderId(null)
            setBreadcrumb([])
        } else {
            const item = breadcrumb[index]
            setBrowseFolderId(item.id)
            setBreadcrumb((prev) => prev.slice(0, index + 1))
        }
    }

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Di chuyển file"
            description={`Di chuyển "${file.originalName}" đến thư mục khác`}
            size="md"
            footer={
                <div className="flex items-center justify-between">
          <span className="text-xs text-gray-400">
            Đang chọn:{' '}
              <strong className="text-gray-700">
              {selectedFolderId
                  ? (breadcrumb.find((b) => b.id === selectedFolderId)?.name ??
                      'Thư mục đã chọn')
                  : 'Root (thư mục gốc)'}
            </strong>
          </span>
                    <div className="flex gap-2">
                        <Button variant="secondary" onClick={onClose}>
                            Hủy
                        </Button>
                        <Button
                            variant="primary"
                            onClick={() => moveMutation.mutate()}
                            isLoading={moveMutation.isPending}
                            disabled={
                                selectedFolderId === (file.folder?.id ?? null)
                            }
                        >
                            Di chuyển
                        </Button>
                    </div>
                </div>
            }
        >
            <div>
                {/* Breadcrumb */}
                <div className="flex items-center gap-1 mb-3 flex-wrap">
                    <button
                        onClick={() => handleBreadcrumbClick(-1)}
                        className={cn(
                            'text-xs px-2 py-1 rounded-lg transition-colors',
                            browseFolderId === null
                                ? 'bg-primary-100 text-primary-700 font-medium'
                                : 'text-gray-500 hover:bg-gray-100'
                        )}
                    >
                        Root
                    </button>
                    {breadcrumb.map((item, idx) => (
                        <span key={item.id} className="flex items-center gap-1">
              <ArrowRight className="h-3 w-3 text-gray-300" />
              <button
                  onClick={() => handleBreadcrumbClick(idx)}
                  className={cn(
                      'text-xs px-2 py-1 rounded-lg transition-colors',
                      browseFolderId === item.id
                          ? 'bg-primary-100 text-primary-700 font-medium'
                          : 'text-gray-500 hover:bg-gray-100'
                  )}
              >
                {item.name}
              </button>
            </span>
                    ))}
                </div>

                {/* Folder list */}
                <div className="border border-gray-100 rounded-xl overflow-hidden
                        max-h-64 overflow-y-auto">

                    {/* Root option */}
                    {!browseFolderId && (
                        <button
                            onClick={() => setSelectedFolderId(null)}
                            className={cn(
                                'w-full flex items-center gap-3 px-4 py-3 text-left',
                                'transition-colors border-b border-gray-50',
                                selectedFolderId === null
                                    ? 'bg-primary-50 text-primary-700'
                                    : 'hover:bg-gray-50 text-gray-700'
                            )}
                        >
                            <FolderOpen className="h-4 w-4 text-primary-500 flex-shrink-0" />
                            <span className="text-sm font-medium">Root (thư mục gốc)</span>
                        </button>
                    )}

                    {/* Loading */}
                    {childLoading && (
                        <div className="flex items-center justify-center py-8">
                            <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
                        </div>
                    )}

                    {/* Folders */}
                    {!childLoading && displayFolders.length === 0 && (
                        <div className="text-center py-8 text-sm text-gray-400">
                            Không có thư mục con
                        </div>
                    )}

                    {!childLoading && displayFolders.map((folder) => {
                        const isCurrentFolder = file.folder?.id === folder.id
                        const isSelected = selectedFolderId === folder.id

                        return (
                            <div
                                key={folder.id}
                                className={cn(
                                    'flex items-center border-b border-gray-50 last:border-0',
                                    isSelected ? 'bg-primary-50' : 'hover:bg-gray-50'
                                )}
                            >
                                {/* Select this folder */}
                                <button
                                    onClick={() => setSelectedFolderId(folder.id)}
                                    disabled={isCurrentFolder}
                                    className={cn(
                                        'flex items-center gap-3 flex-1 px-4 py-3 text-left',
                                        'transition-colors',
                                        isCurrentFolder ? 'opacity-40 cursor-not-allowed' : ''
                                    )}
                                >
                                    <FolderIcon className={cn(
                                        'h-4 w-4 flex-shrink-0',
                                        isSelected ? 'text-primary-500' : 'text-amber-400'
                                    )} />
                                    <span className={cn(
                                        'text-sm flex-1 truncate',
                                        isSelected ? 'text-primary-700 font-medium' : 'text-gray-700'
                                    )}>
                    {folder.name}
                  </span>
                                    {isCurrentFolder && (
                                        <span className="text-xs text-gray-400 flex-shrink-0">
                      (hiện tại)
                    </span>
                                    )}
                                </button>

                                {/* Browse into folder */}
                                {folder.childCount > 0 && (
                                    <button
                                        onClick={() => handleBrowseInto(folder)}
                                        className="px-3 py-3 text-gray-400 hover:text-primary-600
                               transition-colors flex-shrink-0"
                                        title="Mở thư mục con"
                                    >
                                        <ArrowRight className="h-4 w-4" />
                                    </button>
                                )}
                            </div>
                        )
                    })}
                </div>
            </div>
        </Modal>
    )
}