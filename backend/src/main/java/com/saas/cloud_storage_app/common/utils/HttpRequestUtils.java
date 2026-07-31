package com.saas.cloud_storage_app.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class HttpRequestUtils {

    private HttpRequestUtils() {}

    // (1) Lấy IP thực của client — xử lý trường hợp có proxy/load balancer
    public static String getClientIpAddress(HttpServletRequest request) {
        // (2) Các header proxy thường set
        String[] ipHeaders = {
                "X-Forwarded-For",      // Load balancer phổ biến nhất
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // (3) X-Forwarded-For có thể chứa nhiều IP: "client, proxy1, proxy2"
                // Lấy IP đầu tiên = IP của client thực
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // (4) Lấy User-Agent, truncate nếu quá dài
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown";
        // Giới hạn 500 ký tự — khớp với column length
        return ua.length() > 500 ? ua.substring(0, 500) : ua;
    }
}