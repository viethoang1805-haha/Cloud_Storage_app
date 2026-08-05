import { create } from 'zustand'

interface FileStore {
    uploadProgress: Record<string, number>
    setProgress: (fileId: string, progress: number) => void
    removeProgress: (fileId: string) => void
}

export const useFileStore = create<FileStore>((set) => ({
    uploadProgress: {},

    setProgress: (fileId, progress) =>
        set((state) => ({
            uploadProgress: { ...state.uploadProgress, [fileId]: progress },
        })),

    removeProgress: (fileId) =>
        set((state) => {
            const { [fileId]: _, ...rest } = state.uploadProgress
            return { uploadProgress: rest }
        }),
}))