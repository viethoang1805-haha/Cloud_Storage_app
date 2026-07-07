package com.saas.cloud_storage_app.modules.auth.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.auth.dto.request.LoginRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RefreshTokenRequest;
import com.saas.cloud_storage_app.modules.auth.dto.request.RegisterRequest;
import com.saas.cloud_storage_app.modules.auth.dto.response.TokenResponse;
import com.saas.cloud_storage_app.modules.auth.entity.RefreshToken;
import com.saas.cloud_storage_app.modules.auth.mapper.AuthMapper;
import com.saas.cloud_storage_app.modules.auth.repository.RefreshTokenRepository;
import com.saas.cloud_storage_app.modules.auth.service.AuthService;
import com.saas.cloud_storage_app.modules.user.entity.Role;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.repository.RoleRepository;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import com.saas.cloud_storage_app.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // ms

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;  // ms

    // =============================================
    // ĐĂNG KÝ
    // =============================================
    @Override
    @Transactional  // (1) Nếu bất kỳ bước nào lỗi → rollback toàn bộ
    public TokenResponse register(RegisterRequest request) {

        // (2) Bước 1: Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // (3) Bước 2: Tìm role mặc định ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Role mặc định không tồn tại, hãy kiểm tra migration"
                ));

        // (4) Bước 3: Tạo User entity
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim()) // (5)
                .password(passwordEncoder.encode(request.getPassword())) // (6)
                .fullName(request.getFullName().trim())
                .isEnabled(true)
                .build();

        user.addRole(userRole);

        // (7) Bước 4: Lưu user vào DB
        User savedUser = userRepository.save(user);
        log.info("Đăng ký thành công cho email: {}", savedUser.getEmail());

        // (8) Bước 5: Tạo token và trả về
        return generateTokenResponse(savedUser);
    }

    // =============================================
    // ĐĂNG NHẬP
    // =============================================
    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {

        // (9) Bước 1: Xác thực email + password qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );
        // Nếu sai → AuthenticationManager tự ném BadCredentialsException
        // GlobalExceptionHandler sẽ bắt và trả về 401

        // (10) Bước 2: Lấy User từ DB
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // (11) Bước 3: Xóa refresh token cũ (nếu có) để không tích lũy
        refreshTokenRepository.deleteExpiredOrRevokedByUserId(user.getId());

        log.info("Đăng nhập thành công: {}", user.getEmail());

        // (12) Bước 4: Tạo token và trả về
        return generateTokenResponse(user);
    }

    // =============================================
    // REFRESH TOKEN
    // =============================================
    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {

        // (13) Bước 1: Tìm refresh token trong DB
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        // (14) Bước 2: Kiểm tra token còn hợp lệ không
        if (!refreshToken.isValid()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // (15) Bước 3: Thu hồi refresh token cũ (rotation strategy)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // (16) Bước 4: Tạo cặp token mới
        User user = refreshToken.getUser();
        log.info("Refresh token cho user: {}", user.getEmail());

        return generateTokenResponse(user);
    }

    // =============================================
    // LOGOUT (1 thiết bị)
    // =============================================
    @Override
    @Transactional
    public void logout(String token) {

        // (17) Tìm và thu hồi refresh token
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Logout thành công cho user: {}",
                            refreshToken.getUser().getEmail());
                });
    }

    // =============================================
    // LOGOUT ALL (tất cả thiết bị)
    // =============================================
    @Override
    @Transactional
    public void logoutAll(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // (18) Thu hồi tất cả refresh token của user
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("Logout all devices cho user: {}", email);
    }

    // =============================================
    // PRIVATE HELPER
    // =============================================
    private TokenResponse generateTokenResponse(User user) {

        // (19) Load UserDetails để tạo token (cần authorities)
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority
                                        .SimpleGrantedAuthority(r.getName()))
                                .toList())
                        .build();

        // (20) Tạo access token
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        // (21) Tạo và lưu refresh token vào DB
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000)) // (22)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        // (23) Build và trả về response
        return authMapper.toTokenResponse(
                accessToken,
                rawRefreshToken,
                accessTokenExpiration / 1000, // đổi ms → giây
                user
        );
    }
}