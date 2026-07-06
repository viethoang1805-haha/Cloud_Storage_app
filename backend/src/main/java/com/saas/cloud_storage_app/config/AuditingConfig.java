package com.saas.cloud_storage_app.config;


import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configurable
@EnableJpaAuditing //sử dụng baseentity
public class AuditingConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return ()->{
            //lấy thông tin từ authentication hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //chưa đăng nhập thì ghi là hệ thống
            if(authentication==null
                    || !authentication.isAuthenticated()
                    || authentication.getName().equals("anonymousUser")){
                return Optional.of("system");
            }
            //lấy username đã đăng nhập
            return Optional.of(authentication.getName());
        };
    }
}
