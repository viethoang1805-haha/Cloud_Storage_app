package com.saas.cloud_storage_app.modules.activity.service.impl;

import com.saas.cloud_storage_app.modules.activity.dto.request.ActivityFilterRequest;
import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogPageResponse;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import com.saas.cloud_storage_app.modules.activity.mapper.ActivityLogMapper;
import com.saas.cloud_storage_app.modules.activity.repository.ActivityLogRepository;
import com.saas.cloud_storage_app.modules.activity.service.ActivityLogService;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
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
    private final UserService userService;

    // =============================================
    // LƯU LOG — async, transaction riêng
    // =============================================
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(ActivityLog activityLog) {
        try {
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Lỗi ghi activity log: {}", e.getMessage());
        }
    }

    // =============================================
    // LOG CỦA WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getWorkspaceActivities(
            String email, UUID workspaceId, ActivityFilterRequest filter) {

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<ActivityLog> page = activityLogRepository.search(
                workspaceId,
                null,
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable);

        return activityLogMapper.toPageResponse(page);
    }

    // =============================================
    // LOG CỦA TÔI TRONG WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getMyActivities(
            String email, UUID workspaceId, ActivityFilterRequest filter) {

        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<ActivityLog> page = activityLogRepository.search(
                workspaceId,
                user.getId(),
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable);

        return activityLogMapper.toPageResponse(page);
    }

    // =============================================
    // TẤT CẢ LOG — ADMIN ONLY
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ActivityLogPageResponse getAllActivities(ActivityFilterRequest filter) {

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<ActivityLog> page = activityLogRepository.search(
                null,
                null,
                filter.getAction(),
                filter.getTargetType(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable);

        return activityLogMapper.toPageResponse(page);
    }
}