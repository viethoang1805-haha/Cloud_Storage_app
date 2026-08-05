export const APP_NAME = 'CloudStorage'
export const MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

export const PERMISSION_LABELS: Record<string, string> = {
    VIEW: 'Xem',
    DOWNLOAD: 'Tải xuống',
    EDIT: 'Chỉnh sửa',
    DELETE: 'Xóa',
}

export const ROLE_LABELS: Record<string, string> = {
    OWNER: 'Chủ sở hữu',
    ADMIN: 'Quản trị viên',
    MEMBER: 'Thành viên',
    VIEWER: 'Người xem',
}

export const ACTION_LABELS: Record<string, string> = {
    FILE_UPLOADED: 'Đã upload file',
    FILE_DOWNLOADED: 'Đã tải file',
    FILE_DELETED: 'Đã xóa file',
    FILE_SHARED: 'Đã chia sẻ file',
    FOLDER_CREATED: 'Đã tạo thư mục',
    FOLDER_DELETED: 'Đã xóa thư mục',
    MEMBER_INVITED: 'Đã mời thành viên',
    MEMBER_REMOVED: 'Đã xóa thành viên',
    WORKSPACE_CREATED: 'Đã tạo workspace',
}