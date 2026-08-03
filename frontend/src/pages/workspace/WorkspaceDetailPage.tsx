// src/pages/file/FileListPage.tsx
import { useState, useRef, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { Upload, Search, Grid, List, Loader2, Files } from 'lucide-react'
import {
    useRootFiles,
    useUploadFile,
    useDeleteFile,
    useDownloadFile,
} from '@/hooks/useFile'
import { useWorkspace } from '@/hooks/useWorkspace'
import Loading from '@/components/common/Loading'
import EmptyState from '@/components/common/EmptyState'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import FileCard from '@/components/file/FileCard'
import FileTable from '@/components/file/FileTable'
import UploadDropzone from '@/components/file/UploadDropzone'
import Button from '@/components/common/Button'
import { FileItem } from '@/types/file'
import { cn } from '@/lib/utils'

type ViewMode = 'grid' | 'list'

export default function FileListPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const [viewMode, setViewMode] = useState<ViewMode>('grid')
    const [search, setSearch] = useState('')
    const [deleteTarget, setDeleteTarget] = useState<FileItem | null>(null)
    const [isDragOver, setIsDragOver] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const { data: workspace } = useWorkspace(workspaceId!)
    const {
        data: filesData,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
        isLoading,
    } = useRootFiles(workspaceId!)

    const uploadMutation = useUploadFile(workspaceId!)
    const deleteMutation = useDeleteFile(workspaceId!)
    const downloadMutation = useDownloadFile(workspaceId!)

    const allFiles = filesData?.pages.flatMap((p) => p.files) ?? []

    const filteredFiles = search
        ? allFiles.filter((f) =>
            f.originalName.toLowerCase().includes(search.toLowerCase())
        )
        : allFiles

    const handleFiles = async (files: FileList) => {
        for (const file of Array.from(files)) {
            await uploadMutation.mutateAsync({ file })
        }
    }

    const handleDragOver = useCallback((e: React.DragEvent) => {
        e.preventDefault()
        setIsDragOver(true)
    }, [])

    const handleDragLeave = useCallback((e: React.DragEvent) => {
        e.preventDefault()
        setIsDragOver(false)
    }, [])

    const handleDrop = useCallback(async (e: React.DragEvent) => {
        e.preventDefault()
        setIsDragOver(false)
        if (e.dataTransfer.files.length > 0) {
            await handleFiles(e.dataTransfer.files)
        }
    }, [workspaceId])

    if (isLoading) return <Loading text="Đang tải file..." />

    return (
        <div
            className="relative"
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
        >
            {/* Drag overlay */}
            {isDragOver && (
                <div className="absolute inset-0 z-20 rounded-2xl
                        bg-primary-50 border-2 border-dashed border-primary-400
                        flex items-center justify-center">
                    <div className="text-center">
                        <Upload className="h-12 w-12 text-primary-500 mx-auto mb-2" />
                        <p className="text-primary-700 font-semibold">
                            Thả file để upload
                        </p>
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="flex items-start justify-between mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        {workspace?.name || 'Files'}
                    </h1>
                    <p className="text-sm text-gray-400 mt-1">
                        {allFiles.length} file
                    </p>
                </div>

                <div className="flex items-center gap-2">
                    {/* View toggle */}
                    <div className="flex border border-gray-200 rounded-xl
                          overflow-hidden bg-white">
                        <button
                            onClick={() => setViewMode('grid')}
                            className={cn(
                                'p-2 transition-colors',
                                viewMode === 'grid'
                                    ? 'bg-primary-600 text-white'
                                    : 'text-gray-500 hover:bg-gray-50'
                            )}
                        >
                            <Grid className="h-4 w-4" />
                        </button>
                        <button
                            onClick={() => setViewMode('list')}
                            className={cn(
                                'p-2 transition-colors',
                                viewMode === 'list'
                                    ? 'bg-primary-600 text-white'
                                    : 'text-gray-500 hover:bg-gray-50'
                            )}
                        >
                            <List className="h-4 w-4" />
                        </button>
                    </div>

                    <Button
                        variant="primary"
                        onClick={() => fileInputRef.current?.click()}
                        isLoading={uploadMutation.isPending}
                    >
                        <Upload className="h-4 w-4" />
                        Upload
                    </Button>

                    <input
                        ref={fileInputRef}
                        type="file"
                        multiple
                        className="hidden"
                        onChange={(e) => {
                            if (e.target.files) handleFiles(e.target.files)
                            e.target.value = ''
                        }}
                    />
                </div>
            </div>

            {/* Search */}
            <div className="relative mb-5">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2
                           h-4 w-4 text-gray-400 pointer-events-none" />
                <input
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Tìm kiếm file..."
                    className="input pl-10"
                />
            </div>

            {/* Content */}
            {allFiles.length === 0 ? (
                <UploadDropzone
                    onFileSelect={handleFiles}
                    isUploading={uploadMutation.isPending}
                />
            ) : filteredFiles.length === 0 ? (
                <EmptyState
                    icon={Files}
                    title="Không tìm thấy file"
                    description={`Không có file nào khớp với "${search}"`}
                />
            ) : viewMode === 'grid' ? (
                <div className="grid grid-cols-2 sm:grid-cols-3
                        lg:grid-cols-4 xl:grid-cols-5 gap-3">
                    {filteredFiles.map((file) => (
                        <FileCard
                            key={file.id}
                            file={file}
                            onDownload={() => downloadMutation.mutate(file.id)}
                            onDelete={() => setDeleteTarget(file)}
                        />
                    ))}
                </div>
            ) : (
                <FileTable
                    files={filteredFiles}
                    onDownload={(f) => downloadMutation.mutate(f.id)}
                    onDelete={(f) => setDeleteTarget(f)}
                />
            )}

            {/* Load more */}
            {hasNextPage && (
                <div className="text-center mt-6">
                    <Button
                        variant="secondary"
                        onClick={() => fetchNextPage()}
                        isLoading={isFetchingNextPage}
                    >
                        Tải thêm
                    </Button>
                </div>
            )}

            {/* Delete confirm */}
            <ConfirmDialog
                isOpen={!!deleteTarget}
                onClose={() => setDeleteTarget(null)}
                onConfirm={async () => {
                    if (!deleteTarget) return
                    await deleteMutation.mutateAsync(deleteTarget.id)
                    setDeleteTarget(null)
                }}
                title="Xóa file"
                message={`Xóa "${deleteTarget?.originalName}"? Không thể hoàn tác.`}
                confirmText="Xóa"
                isLoading={deleteMutation.isPending}
            />
        </div>
    )
}