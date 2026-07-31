import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import toast from 'react-hot-toast'
import { Loader2 } from 'lucide-react'

const registerSchema = z.object({
    fullName: z
        .string()
        .min(2, 'Họ tên phải ít nhất 2 ký tự')
        .max(100, 'Họ tên không quá 100 ký tự'),
    email: z
        .string()
        .min(1, 'Email không được để trống')
        .email('Email không đúng định dạng'),
    password: z
        .string()
        .min(8, 'Mật khẩu phải ít nhất 8 ký tự')
        .max(100),
    confirmPassword: z.string(),
}).refine(
    // (1) Custom validation: 2 field phải khớp nhau
    (data) => data.password === data.confirmPassword,
    {
        message: 'Xác nhận mật khẩu không khớp',
        path: ['confirmPassword'],
    }
)

type RegisterForm = z.infer<typeof registerSchema>

export default function RegisterPage() {
    const { register: registerUser, isLoading } = useAuthStore()
    const navigate = useNavigate()

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<RegisterForm>({
        resolver: zodResolver(registerSchema),
    })

    const onSubmit = async (data: RegisterForm) => {
        try {
            await registerUser({
                fullName: data.fullName,
                email: data.email,
                password: data.password,
            })
            toast.success('Đăng ký thành công!')
            navigate('/dashboard')
        } catch (error: any) {
            const message = error?.response?.data?.message
                || 'Đăng ký thất bại, vui lòng thử lại'
            toast.error(message)
        }
    }

    // (2) Reusable input class
    const inputClass = (hasError: boolean) =>
        `w-full px-3 py-2 border rounded-lg text-sm
     focus:outline-none focus:ring-2 focus:ring-primary/30
     ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'}`

    return (
        <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
                Tạo tài khoản
            </h1>
            <p className="text-gray-500 mb-6">
                Đã có tài khoản?{' '}
                <Link to="/login"
                      className="text-primary hover:underline font-medium">
                    Đăng nhập
                </Link>
            </p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

                {/* Họ tên */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Họ và tên
                    </label>
                    <input
                        {...register('fullName')}
                        type="text"
                        placeholder="Nguyễn Văn A"
                        className={inputClass(!!errors.fullName)}
                    />
                    {errors.fullName && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.fullName.message}
                        </p>
                    )}
                </div>

                {/* Email */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Email
                    </label>
                    <input
                        {...register('email')}
                        type="email"
                        placeholder="you@example.com"
                        className={inputClass(!!errors.email)}
                    />
                    {errors.email && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                {/* Mật khẩu */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Mật khẩu
                    </label>
                    <input
                        {...register('password')}
                        type="password"
                        placeholder="Tối thiểu 8 ký tự"
                        className={inputClass(!!errors.password)}
                    />
                    {errors.password && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                {/* Xác nhận mật khẩu */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Xác nhận mật khẩu
                    </label>
                    <input
                        {...register('confirmPassword')}
                        type="password"
                        placeholder="Nhập lại mật khẩu"
                        className={inputClass(!!errors.confirmPassword)}
                    />
                    {errors.confirmPassword && (
                        <p className="mt-1 text-xs text-red-500">
                            {errors.confirmPassword.message}
                        </p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-primary text-white py-2 px-4 rounded-lg
                     text-sm font-medium hover:bg-primary/90 transition-colors
                     disabled:opacity-50 disabled:cursor-not-allowed
                     flex items-center justify-center gap-2 mt-2"
                >
                    {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
                    {isLoading ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
                </button>

            </form>
        </div>
    )
}