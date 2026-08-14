import { useState } from 'react'
import {
    FolderOpen,
    Folder as FolderIcon,
    Plus,
    ChevronRight,
    ChevronDown,
    Trash2,
    Loader2,
} from 'lucide-react'
import { FolderItem } from '@/types/folder'
import { cn } from '@/lib/utils'
import { useDeleteFolder, useRootFolders } from '@/hooks/useFolder'
import { useQuery } from '@tanstack/react-query'
import { folderApi } from '@/api/folder.api'
import ConfirmDialog from '@/components/common/ConfirmDialog'

interface FolderSidebarProps {
    workspaceId: string
    selectedFolderId: string | null
    onSelectFolder: (folderId: string | null) => void
    onCreateFolder: () => void
}

export default function FolderSidebar({
                                          workspaceId,
                                          selectedFolderId,
                                          onSelectFolder,
                                          onCreateFolder,
                                      }: FolderSidebarProps) {
    const { data: folders = [], isLoading } = useRootFolders(workspaceId)
    const deleteMutation = useDeleteFolder(workspaceId)
    const [deleteTarget, setDeleteTarget] = useState<FolderItem | null>(null)
    const [expanded, setExpanded] = useState<Set<string>>(new Set())

    const toggleExpand = (id: string) => {
        setExpanded((prev) => {
            const next = new Set(prev)
            next.has(id) ? next.delete(id) : next.add(id)
            return next
        })
    }

    return (
        <div className="w-56 flex-shrink-0">
            {/* Header */}
            <div className="flex items-center justify-between mb-2 px-1">
        <span className="text-xs font-semibold text-gray-500
                         uppercase tracking-wide">
          Thư mục
        </span>
                <button
                    onClick={onCreateFolder}
                    className="h-6 w-6 flex items-center justify-center rounded
                     text-gray-400 hover:text-primary-600
                     hover:bg-primary-50 transition-colors"
                    title="Tạo thư mục mới"
                >
                    <Plus className="h-3.5 w-3.5" />
                </button>
            </div>

            {/* Root — tất cả file */}
            <button
                onClick={() => onSelectFolder(null)}
                className={cn(
                    'w-full flex items-center gap-2 px-3 py-2 rounded-xl',
                    'text-sm transition-colors mb-1',
                    selectedFolderId === null
                        ? 'bg-primary-50 text-primary-700 font-medium'
                        : 'text-gray-600 hover:bg-gray-50'
                )}
            >
                <FolderOpen className="h-4 w-4 flex-shrink-0" />
                <span className="truncate">Tất cả file</span>
            </button>

            {/* Loading */}
            {isLoading && (
                <div className="flex items-center gap-2 px-3 py-2 text-gray-400">
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                    <span className="text-xs">Đang tải...</span>
                </div>
            )}

            {/* Folders tree */}
            <ul className="space-y-0.5">
                {folders.map((folder) => (
                    <FolderTreeNode
                        key={folder.id}
                        folder={folder}
                        workspaceId={workspaceId}
                        depth={0}
                        selectedFolderId={selectedFolderId}
                        expanded={expanded}
                        onSelect={onSelectFolder}
                        onToggle={toggleExpand}
                        onDelete={setDeleteTarget}
                    />
                ))}
            </ul>

            {/* Delete confirm */}
            <ConfirmDialog
                isOpen={!!deleteTarget}
                onClose={() => setDeleteTarget(null)}
                onConfirm={async () => {
                    if (!deleteTarget) return
                    await deleteMutation.mutateAsync(deleteTarget.id)
                    setDeleteTarget(null)
                    if (selectedFolderId === deleteTarget.id) onSelectFolder(null)
                }}
                title="Xóa thư mục"
                message={`Xóa thư mục "${deleteTarget?.name}" và toàn bộ nội dung?`}
                confirmText="Xóa"
                isLoading={deleteMutation.isPending}
            />
        </div>
    )
}

function FolderTreeNode({
                            folder,
                            workspaceId,
                            depth,
                            selectedFolderId,
                            expanded,
                            onSelect,
                            onToggle,
                            onDelete,
                        }: {
    folder: FolderItem
    workspaceId: string
    depth: number
    selectedFolderId: string | null
    expanded: Set<string>
    onSelect: (id: string | null) => void
    onToggle: (id: string) => void
    onDelete: (folder: FolderItem) => void
}) {
    const [showActions, setShowActions] = useState(false)
    const isExpanded = expanded.has(folder.id)
    const isSelected = selectedFolderId === folder.id
    const hasChildren = folder.childCount > 0

    // (1) Chỉ fetch khi expanded VÀ có childCount > 0 VÀ workspaceId hợp lệ
    const {
        data: children = [],
        isLoading: childrenLoading,
    } = useQuery({
        queryKey: ['folders', workspaceId, folder.id, 'children'],
        queryFn: () => folderApi.getChildren(workspaceId, folder.id),
        // (2) Fix: enabled đúng điều kiện
        enabled: isExpanded && hasChildren && !!workspaceId && !!folder.id,
        staleTime: 30 * 1000,
    })

    return (
        <li>
            <div
                className={cn(
                    'flex items-center gap-1 rounded-xl text-sm transition-colors',
                    'group cursor-pointer',
                    isSelected
                        ? 'bg-primary-50 text-primary-700 font-medium'
                        : 'text-gray-600 hover:bg-gray-50'
                )}
                style={{ paddingLeft: `${8 + depth * 12}px`, paddingRight: '8px' }}
                onMouseEnter={() => setShowActions(true)}
                onMouseLeave={() => setShowActions(false)}
            >
                {/* Expand toggle */}
                <button
                    onClick={(e) => {
                        e.stopPropagation()
                        if (hasChildren) onToggle(folder.id)
                    }}
                    className={cn(
                        'h-7 w-5 flex items-center justify-center flex-shrink-0',
                        'text-gray-400 transition-colors',
                        hasChildren ? 'hover:text-gray-600' : 'cursor-default'
                    )}
                >
                    {hasChildren ? (
                        childrenLoading ? (
                            <Loader2 className="h-3 w-3 animate-spin" />
                        ) : isExpanded ? (
                            <ChevronDown className="h-3.5 w-3.5" />
                        ) : (
                            <ChevronRight className="h-3.5 w-3.5" />
                        )
                    ) : (
                        // (3) Dot để fill space nếu không có children
                        <span className="h-1 w-1 rounded-full bg-gray-200 block mx-auto" />
                    )}
                </button>

                {/* Folder name */}
                <button
                    onClick={() => onSelect(folder.id)}
                    className="flex items-center gap-2 flex-1 min-w-0 text-left
                     py-2"
                >
                    <FolderIcon className={cn(
                        'h-4 w-4 flex-shrink-0',
                        isSelected ? 'text-primary-500' : 'text-amber-400'
                    )} />
                    <span className="truncate text-xs font-medium">
            {folder.name}
          </span>
                    {hasChildren && !isExpanded && (
                        <span className="text-xs text-gray-300 ml-auto flex-shrink-0">
              {folder.childCount}
            </span>
                    )}
                </button>

                {/* Delete action */}
                {showActions && (
                    <button
                        onClick={(e) => {
                            e.stopPropagation()
                            onDelete(folder)
                        }}
                        className="h-5 w-5 flex items-center justify-center rounded
                       text-gray-300 hover:text-red-500
                       hover:bg-red-50 transition-colors flex-shrink-0"
                        title="Xóa thư mục"
                    >
                        <Trash2 className="h-3 w-3" />
                    </button>
                )}
            </div>

            {/* Children — đệ quy */}
            {isExpanded && (
                <ul className="space-y-0.5">
                    {children.length === 0 && !childrenLoading ? (
                        // (4) Không có folder con nhưng có file — hiển thị gợi ý
                        <li className="pl-8 py-1">
              <span className="text-xs text-gray-300 italic">
                Trống
              </span>
                        </li>
                    ) : (
                        children.map((child) => (
                            <FolderTreeNode
                                key={child.id}
                                folder={child}
                                workspaceId={workspaceId}
                                depth={depth + 1}
                                selectedFolderId={selectedFolderId}
                                expanded={expanded}
                                onSelect={onSelect}
                                onToggle={onToggle}
                                onDelete={onDelete}
                            />
                        ))
                    )}
                </ul>
            )}
        </li>
    )
}