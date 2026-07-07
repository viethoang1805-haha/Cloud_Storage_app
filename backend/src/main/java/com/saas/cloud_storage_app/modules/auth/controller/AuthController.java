package com.saas.cloud_storage_app.modules.auth.controller;


import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.auth.dto.request.LoginRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RefreshTokenRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RegisterRequest;
import com.saas.cloud_storage_app.modules.auth.dto.response.TokenResponse;
import com.saas.cloud_storage_app.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "authentication" , description = "API đăng ký, đăng nhập, refresh token")


public class AuthController {
    private final AuthService authService;

    //đăng ký
    @PostMapping("/register")
    @Operation(summary = "Đăng ký")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        TokenResponse tokenResponse = authService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 thành công
                .body(ApiResponse.success(tokenResponse,"đăng ký thành công"));

    }

    //đăng nhập
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse,"Đăng nhập thành công"));
    }

    //refresh token
    @PostMapping("/refresh")
    @Operation(summary = "lấy access token mới bằng refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid@RequestBody RefreshTokenRequest refreshTokenRequest){
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse,"Refresh token thành công"));
    }

    //logout
    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<Void>> logout(
        @Valid@RequestBody RefreshTokenRequest refreshTokenRequest
    ){
        authService.logout(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("đăng xuât thành công"));
    }

    //logout tất cả
    @PostMapping("/logout-all")
    @Operation(summary = "Đăng xuât tất cả thiết bị")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        authService.logoutAll(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Đã đăng xuất tất cả thiết bị"));

    }








}
