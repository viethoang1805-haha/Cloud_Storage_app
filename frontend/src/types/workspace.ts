export interface Workspace {
    id: string
    name: string
    description: string | null
    isPersonal: boolean
    owner: OwnerInfo
    myRole: WorkspaceRole
    memberCount: number
    createdAt: string
}

export interface OwnerInfo {
    id: string
    fullName: string
    email: string
    avatarUrl: string | null
}

export type WorkspaceRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'

export interface WorkspaceCreateRequest {
    name: string
    description?: string
}

export interface WorkspaceUpdateRequest {
    name: string
    description?: string
}