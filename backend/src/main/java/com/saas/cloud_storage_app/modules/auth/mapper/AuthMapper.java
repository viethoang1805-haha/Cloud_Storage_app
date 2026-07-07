package com.saas.cloud_storage_app.modules.auth.mapper;

import com.saas.cloud_storage_app.modules.auth.dto.response.TokenResponse;
import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthMapper {

    // (1) Chuyển User entity → UserInfo DTO
    public TokenResponse.UserInfo toUserInfo(User user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        return TokenResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .build();
    }

    // (2) Build TokenResponse hoàn chỉnh
    public TokenResponse toTokenResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            User user) {

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(toUserInfo(user))
                .build();
    }
}