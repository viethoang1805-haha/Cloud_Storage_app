import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import toast from 'react-hot-toast'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'

const schema = z.object({
  email: z.string().min(1, 'Vui lòng nhập email').email('Email không hợp lệ'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
})

type Form = z.infer<typeof schema>

export default function LoginPage() {
  const { login, isLoading } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as any)?.from?.pathname || '/dashboard'

  const { register, handleSubmit, formState: { errors } } = useForm<Form>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: Form) => {
    try {
      await login(data)
      toast.success('Đăng nhập thành công!')
      navigate(from, { replace: true })
    } catch (error: any) {
      const message = error?.response?.data?.message
        || 'Email hoặc mật khẩu không đúng'
      toast.error(message)
    }
  }

  return (
    <div>
      <div className="mb-7">
        <h1 className="text-2xl font-bold text-gray-900">
          Chào mừng trở lại
        </h1>
        <p className="text-sm text-gray-500 mt-1.5">
          Đăng nhập để tiếp tục
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Email"
          type="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Mật khẩu"
          type="password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register('password')}
        />

        <Button
          type="submit"
          variant="primary"
          className="w-full mt-6"
          isLoading={isLoading}
        >
          Đăng nhập
        </Button>
      </form>

      <div className="divider" />

      <p className="text-center text-sm text-gray-500">
        Chưa có tài khoản?{' '}
        <Link
          to="/register"
          className="text-primary-600 font-medium hover:text-primary-700
                     hover:underline transition-colors"
        >
          Đăng ký ngay
        </Link>
      </p>
    </div>
  )
}