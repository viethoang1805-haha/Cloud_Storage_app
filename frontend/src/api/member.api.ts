import axiosInstance from './axios'
import { ApiResponse } from '@/types/common'
import { Member, MemberInviteRequest } from '@/types/member'

export { type Member, type MemberInviteRequest }

export const memberApi = {
    getMembers: async (workspaceId: string): Promise<Member[]> => {
        const res = await axiosInstance.get<ApiResponse<Member[]>>(
            `/workspaces/${workspaceId}/members`
        )
        return res.data.data
    },

    invite: async (
        workspaceId: string,
        data: MemberInviteRequest
    ): Promise<Member> => {
        const res = await axiosInstance.post<ApiResponse<Member>>(
            `/workspaces/${workspaceId}/members`,
            data
        )
        return res.data.data
    },

    updateRole: async (
        workspaceId: string,
        userId: string,
        role: string
    ): Promise<Member> => {
        const res = await axiosInstance.put<ApiResponse<Member>>(
            `/workspaces/${workspaceId}/members/${userId}`,
            { role }
        )
        return res.data.data
    },

    remove: async (workspaceId: string, userId: string): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/members/${userId}`
        )
    },

    leave: async (workspaceId: string): Promise<void> => {
        await axiosInstance.delete(
            `/workspaces/${workspaceId}/members/me`
        )
    },
}