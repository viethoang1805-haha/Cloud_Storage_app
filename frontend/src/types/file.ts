export interface FileItem {
    id: string
    originalName: string
    contentType: string
    size: number
    sizeFormatted: string
    extension: string
    isImage: boolean
    isVideo: boolean
    folder: FolderInfo | null
    workspaceId: string
    uploadedBy: UploaderInfo | null
    createdAt: string
    updatedAt: string
    downloadUrl?: string
}

export interface FolderInfo {
    id: string
    name: string
}

export interface UploaderInfo {
    id: string
    fullName: string
    avatarUrl: string | null
}

export interface FilePageResponse {
    files: FileItem[]
    currentPage: number
    totalPages: number
    totalElements: number
    pageSize: number
    hasNext: boolean
    hasPrevious: boolean
}

export interface DownloadUrlResponse {
    fileId: string
    originalName: string
    downloadUrl: string
    expiresAt: string
    size: number
    contentType: string
}