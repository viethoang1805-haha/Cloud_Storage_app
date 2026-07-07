package com.saas.cloud_storage_app.security.service;


import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import com.saas.cloud_storage_app.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository  userRepository;
    //spring security gọi method khi cần xác thực
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException{

        // tim user theo email trong db
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->{
            log.warn("không tìm thây user với email: {}", email);
            return new UsernameNotFoundException(
                    "Không tìm thấy user: " + email
            );
        });
        //nếu tài khoản bị khoá
        if (!user.isEnabled()){
            throw new AppException(ErrorCode.UNAUTHORIZED, "Tài khoản đã bị vô hiệu hóa");
        }

        //chuyển role qua grantedauthority để spr scrt hiểu
        List<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        //trả về user detail
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isEnabled())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
