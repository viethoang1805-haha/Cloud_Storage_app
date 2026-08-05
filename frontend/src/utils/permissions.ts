import { WorkspaceRole } from '@/types/workspace'

export const canUpload = (role: WorkspaceRole): boolean =>
    role !== 'VIEWER'

export const canDelete = (role: WorkspaceRole): boolean =>
    role === 'OWNER' || role === 'ADMIN'

export const canManageMembers = (role: WorkspaceRole): boolean =>
    role === 'OWNER' || role === 'ADMIN'

export const canDeleteWorkspace = (role: WorkspaceRole): boolean =>
    role === 'OWNER'

export const canShareFile = (role: WorkspaceRole): boolean =>
    role !== 'VIEWER'