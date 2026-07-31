import {
    useQuery,
    useMutation,
    useQueryClient,
} from '@tanstack/react-query'
import { workspaceApi } from '@/api/workspace.api'
import {
    WorkspaceCreateRequest,
    WorkspaceUpdateRequest,
} from '@/types/workspace'
import toast from 'react-hot-toast'

// (1) Query keys — tập trung để dễ invalidate
export const workspaceKeys = {
    all: ['workspaces'] as const,
    lists: () => [...workspaceKeys.all, 'list'] as const,
    detail: (id: string) => [...workspaceKeys.all, 'detail', id] as const,
}

// (2) Lấy danh sách workspace
export function useWorkspaces() {
    return useQuery({
        queryKey: workspaceKeys.lists(),
        queryFn: workspaceApi.getMyWorkspaces,
    })
}

// (3) Lấy chi tiết workspace
export function useWorkspace(id: string) {
    return useQuery({
        queryKey: workspaceKeys.detail(id),
        queryFn: () => workspaceApi.getById(id),
        enabled: !!id,   // (4) Chỉ fetch khi có id
    })
}

// (5) Tạo workspace mới
export function useCreateWorkspace() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (data: WorkspaceCreateRequest) =>
            workspaceApi.create(data),

        onSuccess: (newWorkspace) => {
            // (6) Thêm vào cache ngay không cần refetch
            queryClient.setQueryData(
                workspaceKeys.lists(),
                (old: typeof newWorkspace[] | undefined) =>
                    old ? [newWorkspace, ...old] : [newWorkspace]
            )
            toast.success('Tạo workspace thành công!')
        },
    })
}

// Cập nhật workspace
export function useUpdateWorkspace(id: string) {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (data: WorkspaceUpdateRequest) =>
            workspaceApi.update(id, data),

        onSuccess: (updated) => {
            // (7) Cập nhật cả list và detail cache
            queryClient.setQueryData(workspaceKeys.detail(id), updated)
            queryClient.setQueryData(
                workspaceKeys.lists(),
                (old: typeof updated[] | undefined) =>
                    old?.map((w) => (w.id === id ? updated : w))
            )
            toast.success('Cập nhật thành công!')
        },
    })
}

// Xóa workspace
export function useDeleteWorkspace() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: (id: string) => workspaceApi.delete(id),

        onSuccess: (_, id) => {
            // (8) Xóa khỏi cache
            queryClient.setQueryData(
                workspaceKeys.lists(),
                (old: { id: string }[] | undefined) =>
                    old?.filter((w) => w.id !== id)
            )
            queryClient.removeQueries({ queryKey: workspaceKeys.detail(id) })
            toast.success('Đã xóa workspace!')
        },
    })
}