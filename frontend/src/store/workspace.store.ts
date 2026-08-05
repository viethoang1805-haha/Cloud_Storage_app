import { create } from 'zustand'
import { Workspace } from '@/types/workspace'

interface WorkspaceState {
    currentWorkspace: Workspace | null
    setCurrentWorkspace: (ws: Workspace | null) => void
}

export const useWorkspaceStore = create<WorkspaceState>((set) => ({
    currentWorkspace: null,
    setCurrentWorkspace: (ws) => set({ currentWorkspace: ws }),
}))