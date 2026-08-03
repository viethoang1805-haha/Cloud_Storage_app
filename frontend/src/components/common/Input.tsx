import { forwardRef, InputHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  hint?: string
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className, ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="label">
            {label}
            {props.required && (
              <span className="text-red-500 ml-1">*</span>
            )}
          </label>
        )}
        <input
          ref={ref}
          className={cn(
            'input',
            error && 'input-error',
            className
          )}
          {...props}
        />
        {error && (
          <p className="error-msg">{error}</p>
        )}
        {hint && !error && (
          <p className="mt-1 text-xs text-gray-400">{hint}</p>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input'
export default Input