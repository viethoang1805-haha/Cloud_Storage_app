package com.saas.cloud_storage_app.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    // đọc config từ application-dev.yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    //tạo SecretKey từ chuỗi secret trong config
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //tạo accesstoken
    public String generateAccessToken(UserDetails userDetails) {

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + accessTokenExpiration
                ))
                .signWith(getSigningKey())
                .compact();
    }

    //tạo refresh token
    public String generateRefreshToken(UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + refreshTokenExpiration
                ))
                .signWith(getSigningKey())
                .compact();
    }


    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    //kiểm tra token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token đã hết hạn: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token không được hỗ trợ: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token không đúng định dạng: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature không hợp lệ: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token rỗng: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // xác thực chữ ký
                .build()
                .parseSignedClaims(token)     // parse token
                .getPayload();               // lấy phần payload
    }
}