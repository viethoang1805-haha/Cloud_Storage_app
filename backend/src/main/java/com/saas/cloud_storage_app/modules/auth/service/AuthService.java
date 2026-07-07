package com.saas.cloud_storage_app.modules.auth.service;

import com.saas.cloud_storage_app.modules.auth.dto.request.LoginRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RefreshTokenRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RegisterRequest;
import com.saas.cloud_storage_app.modules.auth.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    void logoutAll(String email);
}