import { Users, FolderOpen, Lock } from 'lucide-react'
import { Workspace } from '@/types/workspace'
import { RoleBadge } from '@/components/common/Badge'

interface WorkspaceCardProps {
    workspace: Workspace
    onClick: () => void
}

export default function WorkspaceCard({ workspace, onClick }: WorkspaceCardProps) {
    return (
        <div onClick={onClick} className="card-hover p-5 group">
            {/* Header */}
            <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="h-10 w-10 rounded-xl bg-primary-50 flex
                          items-center justify-center flex-shrink-0">
                        <FolderOpen className="h-5 w-5 text-primary-600" />
                    </div>
                    <div className="min-w-0">
                        <h3 className="font-semibold text-gray-900 truncate
                           group-hover:text-primary-600 transition-colors">
                            {workspace.name}
                        </h3>
                        {workspace.description && (
                            <p className="text-xs text-gray-400 mt-0.5 truncate">
                                {workspace.description}
                            </p>
                        )}
                    </div>
                </div>
                <RoleBadge role={workspace.myRole} />
            </div>

            {/* Footer */}
            <div className="flex items-center gap-3 text-xs text-gray-400 mt-3
                      pt-3 border-t border-gray-50">
        <span className="flex items-center gap-1">
          <Users className="h-3.5 w-3.5" />
            {workspace.memberCount}
        </span>
                {workspace.isPersonal && (
                    <span className="flex items-center gap-1 text-primary-500">
            <Lock className="h-3.5 w-3.5" />
            Cá nhân
          </span>
                )}
                <span className="ml-auto">
          {new Date(workspace.createdAt).toLocaleDateString('vi-VN')}
        </span>
            </div>
        </div>
    )
}