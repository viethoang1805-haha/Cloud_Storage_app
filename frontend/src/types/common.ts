// (2) Generic API response wrapper — khớp với backend ApiResponse<T>
export interface ApiResponse<T> {
    status: number
    message: string
    data: T
    timestamp: string
}

// (3) Paginated response
export interface PageResponse<T> {
    content?: T[]          // Spring Page format
    files?: T[]            // FilePageResponse format
    notifications?: T[]    // NotificationPageResponse format
    activities?: T[]       // ActivityLogPageResponse format
    currentPage: number
    totalPages: number
    totalElements: number
    pageSize?: number
    hasNext: boolean
    hasPrevious?: boolean
}

// (4) Error response từ GlobalExceptionHandler
export interface ApiError {
    status: number
    message: string
    data?: Record<string, string> // validation errors
    timestamp: string
}