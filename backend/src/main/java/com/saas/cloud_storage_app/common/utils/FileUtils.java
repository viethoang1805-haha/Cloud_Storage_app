package com.saas.cloud_storage_app.common.utils;

public class FileUtils {

    private FileUtils() {
        // (1) Private constructor — không cho phép tạo instance
        // Đây là utility class, chỉ dùng static methods
    }

    private static final long KB = 1024L;
    private static final long MB = 1024L * KB;
    private static final long GB = 1024L * MB;
    private static final long TB = 1024L * GB;

    // (2) Format bytes → chuỗi dễ đọc
    public static String formatSize(Long bytes) {
        if (bytes == null || bytes < 0) return "0 B";

        if (bytes >= TB) {
            return String.format("%.2f TB", (double) bytes / TB);
        } else if (bytes >= GB) {
            return String.format("%.2f GB", (double) bytes / GB);
        } else if (bytes >= MB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else if (bytes >= KB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else {
            return bytes + " B";
        }
    }

    // (3) Tính phần trăm đã dùng
    public static Double calcUsedPercent(Long used, Long limit) {
        if (limit == null || limit == 0) return 0.0;
        double percent = (double) used / limit * 100;
        // Làm tròn 2 chữ số thập phân
        return Math.round(percent * 100.0) / 100.0;
    }

    // (4) Lấy extension của file từ tên file
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // (5) Tạo tên file unique để lưu vào MinIO
    // tránh trùng tên khi 2 user upload file cùng tên
    public static String generateStorageKey(String userId, String folderId, String filename) {
        String extension = getExtension(filename);
        String uniqueName = java.util.UUID.randomUUID().toString();
        // Format: userId/folderId/uuid.extension
        // Ví dụ: "abc-123/folder-456/xyz-789.pdf"
        return String.format("%s/%s/%s.%s", userId, folderId, uniqueName, extension);
    }
}