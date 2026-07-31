package com.saas.cloud_storage_app.modules.activity.service.impl;

import com.saas.cloud_storage_app.modules.activity.dto.request.ActivityFilterRequest;
import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogPageResponse;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import com.saas.cloud_storage_app.modules.activity.mapper.ActivityLogMapper;
import com.saas.cloud_storage_app.modules.activity.repository.ActivityLogRepository;
import com.saas.cloud_storage_app.modules.activity.service.ActivityLogService;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final WorkspaceService workspaceService;
    private final UserService userService;

    // =============================================
    // LƯU LOG
    // =============================================
    @Override
    @Async  // (1) Async — không làm chậm main request
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // (2) REQUIRES_NEW — transaction riêng
    // Nếu main transaction rollback, log vẫn được lưu
    public void save(ActivityLog activityLog) {
        try {
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            // (3) Log lỗi nhưng không throw
            // Không để lỗi ghi log ảnh hưởng business logic chính
            log.error("Lỗi ghi activity log: {}", e.getMessage());
        }
    }

    // =============================================
    // LOG CỦA WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getWorkspaceActivities(
            String email,
            UUID workspaceId,
            ActivityFilterRequest filter) {

        // (4) Validate member access
        workspaceService.validateMemberAccess(email, workspaceId);

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        // (5) Dùng dynamic query với search method
        Page<ActivityLog> page = activityLogRepository.search(
                workspaceId,
                null,   // không filter theo actor
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable
        );

        return activityLogMapper.toPageResponse(page);
    }

    // =============================================
    // LOG CỦA TÔI TRONG WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getMyActivities(
            String email,
            UUID workspaceId,
            ActivityFilterRequest filter) {

        User user = userService.getUserByEmail(email);
        workspaceService.validateMemberAccess(email, workspaceId);

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<ActivityLog> page = activityLogRepository.search(
                workspaceId,
                user.getId(),   // (6) filter theo actor = current user
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable
        );

        return activityLogMapper.toPageResponse(page);
    }

    // =============================================
    // TẤT CẢ LOG — ADMIN ONLY
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getAllActivities(ActivityFilterRequest filter) {

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        // (7) Admin có thể xem mọi workspace
        Page<ActivityLog> page = activityLogRepository.search(
                null,   // không giới hạn workspace
                null,
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable
        );

        return activityLogMapper.toPageResponse(page);
    }
}