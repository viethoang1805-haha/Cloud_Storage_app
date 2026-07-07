package com.saas.cloud_storage_app.config;


import com.saas.cloud_storage_app.security.filter.JwtAuthenticationFilter;
import com.saas.cloud_storage_app.security.handler.AccessDeniedHandlerImpl;
import com.saas.cloud_storage_app.security.handler.AuthEntryPointImpl;
import com.saas.cloud_storage_app.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AuthEntryPointImpl authEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;



}
