import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { Toaster } from 'react-hot-toast'
import App from './App'
import { queryClient } from '@/lib/queryClient'
import '@/index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        {/* (1) QueryClientProvider bao toàn bộ app */}
        <QueryClientProvider client={queryClient}>

            <App />

            {/* (2) Toast notifications — đặt ở top level */}
            <Toaster
                position="top-right"
                toastOptions={{
                    duration: 4000,
                    style: {
                        background: 'hsl(var(--card))',
                        color: 'hsl(var(--card-foreground))',
                        border: '1px solid hsl(var(--border))',
                    },
                    success: {
                        iconTheme: {
                            primary: 'hsl(var(--primary))',
                            secondary: 'white',
                        },
                    },
                }}
            />

            {/* (3) DevTools chỉ hiện khi development */}
            {import.meta.env.DEV && (
                <ReactQueryDevtools initialIsOpen={false} />
            )}
        </QueryClientProvider>
    </React.StrictMode>
)