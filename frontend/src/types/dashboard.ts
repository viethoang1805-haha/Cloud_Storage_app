export interface StorageStats {
    usedBytes: number
    limitBytes: number
    availableBytes: number
    usedFormatted: string
    limitFormatted: string
    availableFormatted: string
    usedPercent: number
}

export interface PersonalDashboard {
    storage: StorageStats
    totalFiles: number
    totalFolders: number
    totalWorkspaces: number
    unreadNotifications: number
    recentFiles: RecentFile[]
    recentWorkspaces: RecentWorkspace[]
}

export interface RecentFile {
    id: string
    originalName: string
    contentType: string
    sizeFormatted: string
    workspaceName: string
    folderName: string | null
    uploadedAt: string
}

export interface RecentWorkspace {
    id: string
    name: string
    myRole: string
    memberCount: number
    fileCount: number
}

export interface WorkspaceDashboard {
    workspaceId: string
    workspaceName: string
    totalFiles: number
    totalFolders: number
    totalMembers: number
    totalStorageUsed: number
    totalStorageFormatted: string
    fileTypeStats: FileTypeStats[]
    topContributors: ContributorStats[]
    recentActivities: RecentActivity[]
}

export interface FileTypeStats {
    type: string
    count: number
    totalSize: number
    totalSizeFormatted: string
    percentage: number
}

export interface ContributorStats {
    userId: string
    userName: string
    avatarUrl: string | null
    fileCount: number
    totalSize: number
    totalSizeFormatted: string
}

export interface RecentActivity {
    actorName: string
    actorEmail: string
    action: string
    actionDisplay: string
    targetName: string
    createdAt: string
}

export interface SystemDashboard {
    totalUsers: number
    totalWorkspaces: number
    totalFiles: number
    totalFolders: number
    totalStorageUsed: number
    totalStorageFormatted: string
    newUsersLast30Days: number
    newFilesLast30Days: number
    topWorkspacesByStorage: WorkspaceStorageStats[]
}

export interface WorkspaceStorageStats {
    workspaceId: string
    workspaceName: string
    fileCount: number
    storageUsed: number
    storageFormatted: string
}