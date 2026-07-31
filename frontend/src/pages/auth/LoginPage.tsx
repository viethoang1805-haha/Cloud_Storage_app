import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import toast from 'react-hot-toast'
import { Loader2 } from 'lucide-react'

// (1) Schema validation với Zod
const loginSchema = z.object({
    email: z
        .string()
        .min(1, 'Email không được để trống')
        .email('Email không đúng định dạng'),
    password: z
        .string()
        .min(1, 'Mật khẩu không được để trống'),
})

type LoginForm = z.infer<typeof loginSchema>

export default function LoginPage() {
    const { login, isLoading } = useAuthStore()
    const navigate = useNavigate()
    const location = useLocation()

    // (2) Redirect về URL trước đó sau khi login
    const from = (location.state as { from?: Location })?.from?.pathname
        || '/dashboard'

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginForm>({
        resolver: zodResolver(loginSchema),
    })

    const onSubmit = async (data: LoginForm) => {
        try {
            await login(data)
            toast.success('Đăng nhập thành công!')
            navigate(from, { replace: true })
        } catch (error: any) {
            // (3) Hiển thị lỗi từ API
            const message = error?.response?.data?.message
                || 'Email hoặc mật khẩu không đúng'
            toast.error(message)
        }
    }

    return (
        <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
                Đăng nhập
            </h1>
            <p className="text-gray-500 mb-6">
                Chưa có tài khoản?{' '}
                <Link to="/register"
                      className="text-primary hover:underline font-medium">
                    Đăng ký ngay
                </Link>
            </p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

                {/* Email field */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Email
                    </label>
                    <input
                        {...register('email')}
                        type="email"
                        placeholder="you@example.com"
                        className={`w-full px-3 py-2 border rounded-lg text-sm
                       focus:outline-none focus:ring-2 focus:ring-primary/30
                       ${errors.email
                            ? 'border-red-400 bg-red-50'
                            : 'border-gray-300'}`}
                    />
                    {/* (4) Hiển thị lỗi validation */}
                    {errors.email && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                {/* Password field */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Mật khẩu
                    </label>
                    <input
                        {...register('password')}
                        type="password"
                        placeholder="••••••••"
                        className={`w-full px-3 py-2 border rounded-lg text-sm
                       focus:outline-none focus:ring-2 focus:ring-primary/30
                       ${errors.password
                            ? 'border-red-400 bg-red-50'
                            : 'border-gray-300'}`}
                    />
                    {errors.password && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                {/* Submit button */}
                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-primary text-white py-2 px-4 rounded-lg
                     text-sm font-medium hover:bg-primary/90 transition-colors
                     disabled:opacity-50 disabled:cursor-not-allowed
                     flex items-center justify-center gap-2"
                >
                    {isLoading && (
                        <Loader2 className="h-4 w-4 animate-spin" />
                    )}
                    {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
                </button>

            </form>
        </div>
    )
}