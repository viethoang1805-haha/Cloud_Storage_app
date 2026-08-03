import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import toast from 'react-hot-toast'
import Button from '@/components/common/Button'
import Input from '@/components/common/Input'

const schema = z.object({
  fullName: z
    .string()
    .min(2, 'Tên phải ít nhất 2 ký tự')
    .max(100, 'Tên không quá 100 ký tự'),
  email: z.string().min(1, 'Vui lòng nhập email').email('Email không hợp lệ'),
  password: z.string().min(8, 'Mật khẩu phải ít nhất 8 ký tự'),
  confirmPassword: z.string(),
}).refine((d) => d.password === d.confirmPassword, {
  message: 'Mật khẩu không khớp',
  path: ['confirmPassword'],
})

type Form = z.infer<typeof schema>

export default function RegisterPage() {
  const { register: signup, isLoading } = useAuthStore()
  const navigate = useNavigate()

  const { register, handleSubmit, formState: { errors } } = useForm<Form>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: Form) => {
    try {
      await signup({
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

  return (
    <div>
      <div className="mb-7">
        <h1 className="text-2xl font-bold text-gray-900">
          Tạo tài khoản
        </h1>
        <p className="text-sm text-gray-500 mt-1.5">
          Miễn phí, không cần thẻ tín dụng
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Họ và tên"
          placeholder="Nguyễn Văn A"
          error={errors.fullName?.message}
          {...register('fullName')}
        />

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
          placeholder="Tối thiểu 8 ký tự"
          error={errors.password?.message}
          hint="Ít nhất 8 ký tự"
          {...register('password')}
        />

        <Input
          label="Xác nhận mật khẩu"
          type="password"
          placeholder="Nhập lại mật khẩu"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        <Button
          type="submit"
          variant="primary"
          className="w-full mt-6"
          isLoading={isLoading}
        >
          Tạo tài khoản
        </Button>
      </form>

      <div className="divider" />

      <p className="text-center text-sm text-gray-500">
        Đã có tài khoản?{' '}
        <Link
          to="/login"
          className="text-primary-600 font-medium hover:text-primary-700
                     hover:underline transition-colors"
        >
          Đăng nhập
        </Link>
      </p>
    </div>
  )
}