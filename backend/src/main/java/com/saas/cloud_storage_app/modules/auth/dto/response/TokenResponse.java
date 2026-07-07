package com.saas.cloud_storage_app.modules.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer"; // (1) frontend dùng để biết prefix khi gửi

    private Long expiresIn;  // (2) số giây access token còn hiệu lực
    private UserInfo user;   // (3) thông tin cơ bản user trả về ngay khi login

    // (4) Inner class — gọn hơn tạo file riêng vì chỉ dùng ở đây
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String fullName;
        private String avatarUrl;
        private java.util.List<String> roles;
    }
}