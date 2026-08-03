import { Loader2 } from 'lucide-react'
import { cn } from '@/lib/utils'

interface LoadingProps {
  text?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
  fullPage?: boolean
}

const sizeMap = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-10 w-10',
}

export default function Loading({
  text,
  size = 'md',
  className,
  fullPage = false,
}: LoadingProps) {
  const content = (
    <div className={cn('flex flex-col items-center gap-3', className)}>
      <Loader2 className={cn('animate-spin text-primary-600', sizeMap[size])} />
      {text && (
        <p className="text-sm text-gray-500">{text}</p>
      )}
    </div>
  )

  if (fullPage) {
    return (
      <div className="fixed inset-0 bg-white/80 backdrop-blur-sm
                      flex items-center justify-center z-50">
        {content}
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center py-16">
      {content}
    </div>
  )
}