import { useState, useRef, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import {
    Upload,
    Search,
    FolderPlus,
    Grid,
    List,
    Loader2,
} from 'lucide-react'
import {
    useRootFiles,
    useUploadFile,
    useDeleteFile,
    useDownloadFile,
} from '@/hooks/useFile'
import { useWorkspace } from '@/hooks/useWorkspace'
import Loading from '@/components/common/Loading'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import FileCard from '@/components/file/FileCard'
import FileTable from '@/components/file/FileTable'
import UploadDropzone from '@/components/file/UploadDropzone'
import { FileItem } from '@/types/file'

type ViewMode = 'grid' | 'list'

export default function FileListPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const [viewMode, setViewMode] = useState<ViewMode>('grid')
    const [searchKeyword, setSearchKeyword] = useState('')
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

    // (1) Flatten infinite query pages thành flat array
    const allFiles = filesData?.pages.flatMap((page) => page.files) ?? []

    // (2) Handle file upload từ input
    const handleFileSelect = async (files: FileList) => {
        const fileArray = Array.from(files)
        for (const file of fileArray) {
            await uploadMutation.mutateAsync({ file })
        }
    }

    // (3) Drag & drop handlers
    const handleDragOver = useCallback((e: React.DragEvent) => {
        e.preventDefault()
        setIsDragOver(true)
    }, [])

    const handleDragLeave = useCallback(() => {
        setIsDragOver(false)
    }, [])

    const handleDrop = useCallback(
        async (e: React.DragEvent) => {
            e.preventDefault()
            setIsDragOver(false)
            const files = e.dataTransfer.files
            if (files.length > 0) await handleFileSelect(files)
        },
        [workspaceId]
    )

    const handleDelete = async () => {
        if (!deleteTarget) return
        await deleteMutation.mutateAsync(deleteTarget.id)
        setDeleteTarget(null)
    }

    if (isLoading) return <Loading text="Đang tải file..." />

    return (
        <div
            onDragOver={handleDragOver}
    onDragLeave={handleDragLeave}
    onDrop={handleDrop}
    className="relative"
        >
        {/* (4) Drag overlay */}
    {isDragOver && (
        <div className="absolute inset-0 z-10 bg-primary/10 border-2
        border-dashed border-primary rounded-xl flex
        items-center justify-center">
    <div className="text-center">
    <Upload className="h-12 w-12 text-primary mx-auto mb-2" />
    <p className="text-primary font-semibold">
        Thả file vào đây để upload
    </p>
    </div>
    </div>
    )}

    {/* Page header */}
    <div className="flex items-center justify-between mb-6">
    <div>
        <h1 className="text-2xl font-bold text-gray-900">
        {workspace?.name || 'Files'}
    </h1>
    <p className="text-gray-500 mt-1 text-sm">
        {allFiles.length} file
    </p>
    </div>

    <div className="flex items-center gap-2">
        {/* View toggle */}
        <div className="flex border border-gray-200 rounded-lg overflow-hidden">
    <button
        onClick={() => setViewMode('grid')}
    className={`p-2 ${viewMode === 'grid'
        ? 'bg-primary text-white'
        : 'hover:bg-gray-50 text-gray-600'}`}
>
    <Grid className="h-4 w-4" />
        </button>
        <button
    onClick={() => setViewMode('list')}
    className={`p-2 ${viewMode === 'list'
        ? 'bg-primary text-white'
        : 'hover:bg-gray-50 text-gray-600'}`}
>
    <List className="h-4 w-4" />
        </button>
        </div>

    {/* Upload button */}
    <button
        onClick={() => fileInputRef.current?.click()}
    disabled={uploadMutation.isPending}
    className="flex items-center gap-2 bg-primary text-white
    px-4 py-2 rounded-lg hover:bg-primary/90
    disabled:opacity-50 transition-colors text-sm
    font-medium"
    >
    {uploadMutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
        ) : (
            <Upload className="h-4 w-4" />
        )}
    Upload
    </button>

    {/* Hidden file input */}
    <input
        ref={fileInputRef}
    type="file"
    multiple
    className="hidden"
    onChange={(e) => {
        if (e.target.files) handleFileSelect(e.target.files)
        e.target.value = ''  // (5) Reset để upload cùng file lại được
    }}
    />
    </div>
    </div>

    {/* Search bar */}
    <div className="relative mb-6">
    <Search className="absolute left-3 top-1/2 -translate-y-1/2
    h-4 w-4 text-gray-400" />
    <input
    value={searchKeyword}
    onChange={(e) => setSearchKeyword(e.target.value)}
    placeholder="Tìm kiếm file..."
    className="w-full pl-10 pr-4 py-2 border border-gray-200
    rounded-lg text-sm focus:outline-none
    focus:ring-2 focus:ring-primary/30"
    />
    </div>

    {/* File list */}
    {allFiles.length === 0 ? (
            <UploadDropzone
                onFileSelect={handleFileSelect}
        isUploading={uploadMutation.isPending}
        />
    ) : viewMode === 'grid' ? (
            <div className="grid grid-cols-2 sm:grid-cols-3
        lg:grid-cols-4 xl:grid-cols-5 gap-4">
        {allFiles.map((file) => (
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
            files={allFiles}
        onDownload={(file) => downloadMutation.mutate(file.id)}
        onDelete={(file) => setDeleteTarget(file)}
        />
    )}

    {/* (6) Load more button */}
    {hasNextPage && (
        <div className="text-center mt-6">
        <button
            onClick={() => fetchNextPage()}
        disabled={isFetchingNextPage}
        className="px-6 py-2 border border-gray-200 rounded-lg
        text-sm text-gray-600 hover:bg-gray-50
        disabled:opacity-50 flex items-center gap-2 mx-auto"
    >
    {isFetchingNextPage && (
        <Loader2 className="h-4 w-4 animate-spin" />
    )}
        {isFetchingNextPage ? 'Đang tải...' : 'Tải thêm'}
        </button>
        </div>
    )}

    {/* Delete confirm */}
    <ConfirmDialog
        isOpen={!!deleteTarget}
    onClose={() => setDeleteTarget(null)}
    onConfirm={handleDelete}
    title="Xóa file"
    message={`Bạn có chắc muốn xóa "${deleteTarget?.originalName}"? Hành động này không thể hoàn tác.`}
    confirmText="Xóa"
    isLoading={deleteMutation.isPending}
    />
    </div>
)
}