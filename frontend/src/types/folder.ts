export interface FolderItem {
    id: string
    name: string
    workspaceId: string
    parent: { id: string; name: string } | null
    childCount: number
    createdAt: string
    updatedAt: string
}

export interface FolderTree extends FolderItem {
    depth: number
    children: FolderTree[]
}

export interface FolderCreateRequest {
    name: string
    parentId?: string
}

export interface FolderRenameRequest {
    name: string
}