package com.saas.cloud_storage_app.config;


import com.saas.cloud_storage_app.security.filter.JwtAuthenticationFilter;
import com.saas.cloud_storage_app.security.handler.AccessDeniedHandlerImpl;
import com.saas.cloud_storage_app.security.handler.AuthEntryPointImpl;
import com.saas.cloud_storage_app.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AuthEntryPointImpl authEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    //danh sách công khai
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",          // đăng nhập, đăng ký, refresh token
            "/api/v1/share/public/**",  // truy cập file qua link chia sẻ công khai
            "/swagger-ui/**",           // Swagger UI
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    //cấu hình
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Tắt CSRF vì dùng jwt
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //Stateless session — không lưu session trên server
                // Mỗi request phải tự mang token, server không nhớ state
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Cấu hình handler cho 401 và 403
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler)   // 403
                )

                // Phân quyền
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()   // public endpoints
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")                       // chỉ admin
                        .anyRequest().authenticated()               // còn lại phải đăng nhập
                )

                // (1 Gắn authentication provider
                .authenticationProvider(authenticationProvider())

                // (11) Thêm JWT filter VÀO TRƯỚC UsernamePasswordAuthenticationFilter
                // Tức là JWT filter chạy trước, nếu xác thực được bằng token
                // thì không cần đến UsernamePasswordAuthenticationFilter nữa
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // Dùng trong AuthService để thực hiện login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //câu hinh cors
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép frontend React (dev) gọi API
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000"   // CRA dev server (phòng trường hợp)
        ));

        // Cho phép các HTTP method
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Cho phép tất cả header (bao gồm Authorization)
        config.setAllowedHeaders(List.of("*"));

        // Cho phép gửi credentials (cookie, Authorization header)
        config.setAllowCredentials(true);

        // Áp dụng config cho tất cả endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Kết nối UserDetailsService + PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // cách load user
        provider.setPasswordEncoder(passwordEncoder());     // cách verify password
        return provider;
    }
    // dùng bcryp
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt tự động thêm salt, hash nhiều vòng → rất khó brute force
        return new BCryptPasswordEncoder();
    }


}
