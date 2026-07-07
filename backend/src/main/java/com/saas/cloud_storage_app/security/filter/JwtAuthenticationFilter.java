package com.saas.cloud_storage_app.security.filter;

import com.saas.cloud_storage_app.security.jwt.JwtTokenProvider;
import com.saas.cloud_storage_app.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // (1) OncePerRequestFilter đảm bảo filter chỉ chạy đúng 1 lần mỗi request

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain     // (2) chuỗi filter tiếp theo
    ) throws ServletException, IOException {

        try {
            // (3) Bước 1: Lấy token từ header "Authorization"
            String token = extractTokenFromRequest(request);

            // (4) Bước 2: Nếu có token và token hợp lệ → xác thực
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // (5) Bước 3: Lấy email từ token
                String email = jwtTokenProvider.getEmailFromToken(token);

                // (6) Bước 4: Load thông tin user từ DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // (7) Bước 5: Tạo Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                        // credentials = null vì đã xác thực bằng token
                                userDetails.getAuthorities() // roles/permissions
                        );

                // (8) Gắn thêm thông tin request (IP, session...)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // (9) Bước 6: Lưu Authentication vào SecurityContext
                // Từ đây Spring Security biết request này đã được xác thực là ai
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Xác thực thành công cho user: {}", email);
            }

        } catch (Exception e) {
            // (10) Không ném exception ở đây — chỉ log và tiếp tục
            // SecurityContext sẽ rỗng → Spring Security sẽ trả 401 tự động
            log.error("Không thể xác thực user: {}", e.getMessage());
        }

        // (11) Luôn gọi filterChain để request tiếp tục đi
        filterChain.doFilter(request, response);
    }

    // (12) Đọc token từ header Authorization
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Header format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ prefix "Bearer " lấy phần token
        }

        return null;
    }
}