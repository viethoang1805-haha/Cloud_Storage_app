import { cn, getRoleBadgeClass } from '@/lib/utils'

interface BadgeProps {
  children: React.ReactNode
  variant?: 'blue' | 'green' | 'red' | 'yellow' | 'purple' | 'gray'
  className?: string
}

const variantMap = {
  blue:   'badge-blue',
  green:  'badge-green',
  red:    'badge-red',
  yellow: 'badge-yellow',
  purple: 'badge-purple',
  gray:   'badge-gray',
}

export function Badge({ children, variant = 'gray', className }: BadgeProps) {
  return (
    <span className={cn(variantMap[variant], className)}>
      {children}
    </span>
  )
}

export function RoleBadge({ role }: { role: string }) {
  return (
    <span className={cn('badge', getRoleBadgeClass(role))}>
      {role}
    </span>
  )
}