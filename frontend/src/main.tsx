(globalThis as any).global = globalThis;

import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { Toaster } from 'react-hot-toast'
import App from './App'
import { queryClient } from '@/lib/queryClient'

// (1) Import CSS bằng đường dẫn tương đối — tránh lỗi TS2882
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>

            <App />

            <Toaster
                position="top-right"
                gutter={8}
                toastOptions={{
                    duration: 4000,
                    style: {
                        background: '#fff',
                        color: '#111827',
                        border: '1px solid #e5e7eb',
                        borderRadius: '12px',
                        padding: '12px 16px',
                        fontSize: '14px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
                    },
                    success: {
                        iconTheme: { primary: '#2563eb', secondary: '#fff' },
                    },
                    error: {
                        iconTheme: { primary: '#dc2626', secondary: '#fff' },
                    },
                }}
            />

            {import.meta.env.DEV && (
                <ReactQueryDevtools initialIsOpen={false} />
            )}

        </QueryClientProvider>
    </React.StrictMode>
)