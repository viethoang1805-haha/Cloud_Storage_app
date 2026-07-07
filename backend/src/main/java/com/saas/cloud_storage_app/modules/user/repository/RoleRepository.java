package com.saas.cloud_storage_app.modules.user.repository;

import com.saas.cloud_storage_app.modules.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Tìm role theo tên — dùng khi đăng ký để gán ROLE_USER cho user mới
    Optional<Role> findByName(String name);
}