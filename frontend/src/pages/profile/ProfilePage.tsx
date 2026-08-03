import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axiosInstance from '@/api/axios'
import { ApiResponse } from '@/types/common'
import { useAuthStore } from '@/store/auth.store'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import Loading from '@/components/common/Loading'
import { Loader2, User, Lock } from 'lucide-react'

interface UserProfile {
  id: string
  email: string
  fullName: string
  avatarUrl: string | null
  storage: {
    usedBytes: number
    limitBytes: number
    usedFormatted: string
    limitFormatted: string
    usedPercent: number
  }
}

// Profile update schema
const profileSchema = z.object({
  fullName: z.string().min(2, 'Tối thiểu 2 ký tự').max(100),
})

// Password schema
const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Nhập mật khẩu hiện tại'),
  newPassword: z.string().min(8, 'Tối thiểu 8 ký tự'),
  confirmPassword: z.string(),
}).refine((d) => d.newPassword === d.confirmPassword, {
  message: 'Mật khẩu không khớp',
  path: ['confirmPassword'],
})

type ProfileForm = z.infer<typeof profileSchema>
type PasswordForm = z.infer<typeof passwordSchema>

export default function ProfilePage() {
  const { setUser } = useAuthStore()

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: async () => {
      const res = await axiosInstance.get<ApiResponse<UserProfile>>('/users/me')
      return res.data.data
    },
  })

  const profileForm = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    values: { fullName: profile?.fullName ?? '' },
  })

  const passwordForm = useForm<PasswordForm>({
    resolver: zodResolver(passwordSchema),
  })

  // Update profile mutation
  const updateProfile = useMutation({
    mutationFn: async (data: ProfileForm) => {
      const res = await axiosInstance.put<ApiResponse<UserProfile>>(
        '/users/me', data
      )
      return res.data.data
    },
    onSuccess: (updated) => {
      setUser({
        id: updated.id,
        email: updated.email,
        fullName: updated.fullName,
        avatarUrl: updated.avatarUrl,
        roles: [],
      })
      toast.success('Cập nhật thành công!')
    },
  })

  // Change password mutation
  const changePassword = useMutation({
    mutationFn: async (data: PasswordForm) => {
      await axiosInstance.put('/users/me/password', data)
    },
    onSuccess: () => {
      toast.success('Đổi mật khẩu thành công!')
      passwordForm.reset()
    },
  })

  if (isLoading) return <Loading text="Đang tải thông tin..." />

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Hồ sơ cá nhân</h1>

      {/* Profile info */}
      <div className="bg-white border border-gray-200 rounded-xl p-6">
        <div className="flex items-center gap-2 mb-5">
          <User className="h-5 w-5 text-primary" />
          <h2 className="font-semibold text-gray-900">Thông tin cá nhân</h2>
        </div>

        {/* Avatar */}
        <div className="flex items-center gap-4 mb-6">
          <div className="h-16 w-16 rounded-full bg-primary/10 flex
                          items-center justify-center text-primary
                          text-2xl font-bold">
            {profile?.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                className="h-16 w-16 rounded-full object-cover"
              />
            ) : (
              profile?.fullName?.charAt(0).toUpperCase()
            )}
          </div>
          <div>
            <p className="font-medium text-gray-900">{profile?.fullName}</p>
            <p className="text-sm text-gray-500">{profile?.email}</p>
          </div>
        </div>

        <form
          onSubmit={profileForm.handleSubmit((d) =>
            updateProfile.mutateAsync(d)
          )}
          className="space-y-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Họ và tên
            </label>
            <input
              {...profileForm.register('fullName')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary/30"
            />
            {profileForm.formState.errors.fullName && (
              <p className="mt-1 text-xs text-red-500">
                {profileForm.formState.errors.fullName.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              value={profile?.email}
              disabled
              className="w-full px-3 py-2 border border-gray-200 rounded-lg
                         text-sm bg-gray-50 text-gray-500 cursor-not-allowed"
            />
          </div>

          <button
            type="submit"
            disabled={updateProfile.isPending}
            className="flex items-center gap-2 px-4 py-2 bg-primary
                       text-white rounded-lg text-sm font-medium
                       hover:bg-primary/90 disabled:opacity-50"
          >
            {updateProfile.isPending && (
              <Loader2 className="h-4 w-4 animate-spin" />
            )}
            Lưu thay đổi
          </button>
        </form>
      </div>

      {/* Storage */}
      {profile?.storage && (
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <h2 className="font-semibold text-gray-900 mb-4">
            Dung lượng lưu trữ
          </h2>
          <div className="h-2 bg-gray-100 rounded-full overflow-hidden mb-2">
            <div
              className="h-full bg-primary rounded-full"
              style={{
                width: `${Math.min(profile.storage.usedPercent, 100)}%`,
              }}
            />
          </div>
          <p className="text-sm text-gray-500">
            Đã dùng {profile.storage.usedFormatted} /
            {profile.storage.limitFormatted}
            ({profile.storage.usedPercent.toFixed(1)}%)
          </p>
        </div>
      )}

      {/* Change password */}
      <div className="bg-white border border-gray-200 rounded-xl p-6">
        <div className="flex items-center gap-2 mb-5">
          <Lock className="h-5 w-5 text-primary" />
          <h2 className="font-semibold text-gray-900">Đổi mật khẩu</h2>
        </div>

        <form
          onSubmit={passwordForm.handleSubmit((d) =>
            changePassword.mutateAsync(d)
          )}
          className="space-y-4"
        >
          {[
            { name: 'currentPassword', label: 'Mật khẩu hiện tại' },
            { name: 'newPassword', label: 'Mật khẩu mới' },
            { name: 'confirmPassword', label: 'Xác nhận mật khẩu mới' },
          ].map(({ name, label }) => (
            <div key={name}>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {label}
              </label>
              <input
                {...passwordForm.register(name as keyof PasswordForm)}
                type="password"
                placeholder="••••••••"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg
                           text-sm focus:outline-none focus:ring-2
                           focus:ring-primary/30"
              />
              {passwordForm.formState.errors[name as keyof PasswordForm] && (
                <p className="mt-1 text-xs text-red-500">
                  {passwordForm.formState.errors[name as keyof PasswordForm]?.message}
                </p>
              )}
            </div>
          ))}

          <button
            type="submit"
            disabled={changePassword.isPending}
            className="flex items-center gap-2 px-4 py-2 bg-primary
                       text-white rounded-lg text-sm font-medium
                       hover:bg-primary/90 disabled:opacity-50"
          >
            {changePassword.isPending && (
              <Loader2 className="h-4 w-4 animate-spin" />
            )}
            Đổi mật khẩu
          </button>
        </form>
      </div>
    </div>
  )
}