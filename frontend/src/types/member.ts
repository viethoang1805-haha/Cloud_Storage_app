export type WorkspaceRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'

export interface Member {
    id: string
    role: WorkspaceRole
    joinedAt: string
    user: {
        id: string
        fullName: string
        email: string
        avatarUrl: string | null
    }
    invitedBy: {
        id: string
        fullName: string
        email: string
    } | null
}

export interface MemberInviteRequest {
    email: string
    role: 'ADMIN' | 'MEMBER' | 'VIEWER'
}

export interface UpdateMemberRoleRequest {
    role: 'ADMIN' | 'MEMBER' | 'VIEWER'
}