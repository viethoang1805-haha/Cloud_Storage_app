import { useState, useRef, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import {
    Upload, Search, Grid, List,
    FolderPlus, Share2, Files,
} from 'lucide-react'
import {
    useRootFiles, useFolderFiles,
    useUploadFile, useDeleteFile, useDownloadFile,
} from '@/hooks/useFile'
import { useCreateFolder } from '@/hooks/useFolder'
import { useWorkspace } from '@/hooks/useWorkspace'
import Loading from '@/components/common/Loading'
import EmptyState from '@/components/common/EmptyState'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import FileCard from '@/components/file/FileCard'
import FileTable from '@/components/file/FileTable'
import UploadDropzone from '@/components/file/UploadDropzone'
import FolderSidebar from '@/components/folder/FolderSidebar'
import ShareFileModal from '@/components/share/ShareFileModal'
import Modal from '@/components/common/Modal'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'
import { FileItem } from '@/types/file'
import { cn } from '@/lib/utils'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

type ViewMode = 'grid' | 'list'

const folderSchema = z.object({
    name: z.string().min(1, 'Tên không được để trống').max(255),
})

export default function FileListPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const [viewMode, setViewMode] = useState<ViewMode>('grid')
    const [search, setSearch] = useState('')
    const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null)
    const [deleteTarget, setDeleteTarget] = useState<FileItem | null>(null)
    const [shareTarget, setShareTarget] = useState<FileItem | null>(null)
    const [isDragOver, setIsDragOver] = useState(false)
    const [showCreateFolder, setShowCreateFolder] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const { data: workspace } = useWorkspace(workspaceId!)

    // Files query — root hoặc folder
    const rootQuery = useRootFiles(workspaceId!)
    const folderQuery = useFolderFiles(
        workspaceId!,
        selectedFolderId ?? ''
    )

    const activeQuery = selectedFolderId ? folderQuery : rootQuery
    const allFiles = activeQuery.data?.pages.flatMap((p) => p.files) ?? []
    const filteredFiles = search
        ? allFiles.filter((f) =>
            f.originalName.toLowerCase().includes(search.toLowerCase())
        )
        : allFiles

    const uploadMutation = useUploadFile(workspaceId!)
    const deleteMutation = useDeleteFile(workspaceId!)
    const downloadMutation = useDownloadFile(workspaceId!)
    const createFolderMutation = useCreateFolder(workspaceId!)

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm({ resolver: zodResolver(folderSchema) })

    const handleFiles = async (files: FileList) => {
        for (const file of Array.from(files)) {
            await uploadMutation.mutateAsync({
                file,
                folderId: selectedFolderId ?? undefined,
            })
        }
    }

    const handleDragOver = useCallback((e: React.DragEvent) => {
        e.preventDefault()
        setIsDragOver(true)
    }, [])

    const handleDragLeave = useCallback(() => setIsDragOver(false), [])

    const handleDrop = useCallback(
        async (e: React.DragEvent) => {
            e.preventDefault()
            setIsDragOver(false)
            if (e.dataTransfer.files.length > 0) {
                await handleFiles(e.dataTransfer.files)
            }
        },
        [selectedFolderId]
    )


    const onCreateFolder = async (data: { name: string }) => {
        await createFolderMutation.mutateAsync({
            name: data.name,
            parentId: selectedFolderId ?? undefined,
        })
        reset()
        setShowCreateFolder(false)
    }

    if (activeQuery.isLoading && !activeQuery.data) {
        return <Loading text="Đang tải file..." />
    }

    return (
        <div className="flex gap-6 h-full">
            {/* Sidebar folders */}
            <FolderSidebar
                workspaceId={workspaceId!}
                selectedFolderId={selectedFolderId}
                onSelectFolder={setSelectedFolderId}
                onCreateFolder={() => setShowCreateFolder(true)}
            />

            {/* Main content */}
            <div
                className="flex-1 relative"
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
            >
                {/* Drag overlay */}
                {isDragOver && (
                    <div className="absolute inset-0 z-20 rounded-2xl bg-primary-50
                          border-2 border-dashed border-primary-400
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
                <div className="flex items-start justify-between mb-5">
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">
                            {workspace?.name || 'Files'}
                        </h1>
                        <p className="text-sm text-gray-400 mt-0.5">
                            {allFiles.length} file
                            {selectedFolderId && ' trong thư mục này'}
                        </p>
                    </div>

                    <div className="flex items-center gap-2">
                        {/* View toggle */}
                        <div className="flex border border-gray-200 rounded-xl overflow-hidden">
                            {(['grid', 'list'] as ViewMode[]).map((mode) => (
                                <button
                                    key={mode}
                                    onClick={() => setViewMode(mode)}
                                    className={cn(
                                        'p-2 transition-colors',
                                        viewMode === mode
                                            ? 'bg-primary-600 text-white'
                                            : 'text-gray-500 hover:bg-gray-50'
                                    )}
                                >
                                    {mode === 'grid'
                                        ? <Grid className="h-4 w-4" />
                                        : <List className="h-4 w-4" />}
                                </button>
                            ))}
                        </div>

                        {/* New folder */}
                        <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => setShowCreateFolder(true)}
                        >
                            <FolderPlus className="h-4 w-4" />
                            Thư mục
                        </Button>

                        {/* Upload */}
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
                                onShare={() => setShareTarget(file)}
                            />
                        ))}
                    </div>
                ) : (
                    <FileTable
                        files={filteredFiles}
                        onDownload={(f) => downloadMutation.mutate(f.id)}
                        onDelete={(f) => setDeleteTarget(f)}
                        onShare={(f) => setShareTarget(f)}
                    />
                )}

                {/* Load more */}
                {activeQuery.hasNextPage && (
                    <div className="text-center mt-6">
                        <Button
                            variant="secondary"
                            onClick={() => activeQuery.fetchNextPage()}
                            isLoading={activeQuery.isFetchingNextPage}
                        >
                            Tải thêm
                        </Button>
                    </div>
                )}
            </div>

            {/* Create folder modal */}
            <Modal
                isOpen={showCreateFolder}
                onClose={() => { setShowCreateFolder(false); reset() }}
                title="Tạo thư mục mới"
                size="sm"
                footer={
                    <div className="flex gap-3 justify-end">
                        <Button
                            variant="secondary"
                            onClick={() => { setShowCreateFolder(false); reset() }}
                        >
                            Hủy
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleSubmit(onCreateFolder)}
                            isLoading={createFolderMutation.isPending}
                        >
                            Tạo
                        </Button>
                    </div>
                }
            >
                <Input
                    label="Tên thư mục"
                    placeholder="VD: Tài liệu Q1"
                    error={errors.name?.message}
                    autoFocus
                    {...register('name')}
                />
            </Modal>

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

            {/* Share modal */}
            {shareTarget && (
                <ShareFileModal
                    isOpen={!!shareTarget}
                    onClose={() => setShareTarget(null)}
                    file={shareTarget}
                    workspaceId={workspaceId!}
                />
            )}
        </div>
    )
}