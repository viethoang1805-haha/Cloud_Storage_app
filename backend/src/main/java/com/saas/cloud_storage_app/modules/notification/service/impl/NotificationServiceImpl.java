package com.saas.cloud_storage_app.modules.notification.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.notification.dto.response.NotificationPageResponse;
import com.saas.cloud_storage_app.modules.notification.dto.response.NotificationResponse;
import com.saas.cloud_storage_app.modules.notification.entity.Notification;
import com.saas.cloud_storage_app.modules.notification.event.FileSharedEvent;
import com.saas.cloud_storage_app.modules.notification.event.FileUploadedEvent;
import com.saas.cloud_storage_app.modules.notification.event.MemberInvitedEvent;
import com.saas.cloud_storage_app.modules.notification.mapper.NotificationMapper;
import com.saas.cloud_storage_app.modules.notification.repository.NotificationRepository;
import com.saas.cloud_storage_app.modules.notification.service.NotificationService;
import com.saas.cloud_storage_app.modules.notification.websocket.NotificationSocketHandler;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSocketHandler socketHandler;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    // =============================================
    // LẤY DANH SÁCH NOTIFICATION
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getMyNotifications(
            String email, int page, int size, boolean unreadOnly) {

        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifPage = unreadOnly
                ? notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                user.getId(), pageable)
                : notificationRepository.findAllByUserIdOrderByCreatedAtDesc(
                user.getId(), pageable);

        long unreadCount = notificationRepository
                .countByUserIdAndIsReadFalse(user.getId());

        List<NotificationResponse> notifications = notifPage.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        return NotificationPageResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .currentPage(notifPage.getNumber())
                .totalPages(notifPage.getTotalPages())
                .totalElements(notifPage.getTotalElements())
                .hasNext(notifPage.hasNext())
                .build();
    }

    // =============================================
    // ĐÁNH DẤU ĐÃ ĐỌC
    // =============================================
    @Override
    @Transactional
    public void markAsRead(String email, UUID notificationId) {
        User user = userService.getUserByEmail(email);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        notificationRepository.markAsRead(notificationId, user.getId());
    }

    // =============================================
    // ĐÁNH DẤU TẤT CẢ ĐÃ ĐỌC
    // =============================================
    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = userService.getUserByEmail(email);
        notificationRepository.markAllAsRead(user.getId());
        log.info("Mark all read cho user: {}", email);
    }

    // =============================================
    // ĐẾM CHƯA ĐỌC
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = userService.getUserByEmail(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    // =============================================
    // EVENT HANDLERS — chạy async, transaction riêng
    // =============================================
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void handleFileUploaded(FileUploadedEvent event) {
        String title = "File mới được upload";
        String message = String.format("%s đã upload file '%s'",
                event.getUploader().getFullName(),
                event.getFile().getOriginalName());

        for (User recipient : event.getRecipients()) {
            try {
                Notification saved = notificationRepository.save(
                        Notification.builder()
                                .user(recipient)
                                .title(title)
                                .message(message)
                                .type("FILE_UPLOADED")
                                .refType("FILE")
                                .refId(event.getFile().getId())
                                .build()
                );
                socketHandler.sendToUser(
                        recipient.getId().toString(),
                        notificationMapper.toResponse(saved));
            } catch (Exception e) {
                log.error("Lỗi gửi notification cho user {}: {}",
                        recipient.getId(), e.getMessage());
            }
        }
        log.info("Gửi {} notification FILE_UPLOADED", event.getRecipients().size());
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void handleMemberInvited(MemberInvitedEvent event) {
        try {
            String title = "Bạn được mời vào workspace";
            String message = String.format("%s đã mời bạn vào workspace '%s'",
                    event.getInviter().getFullName(),
                    event.getWorkspace().getName());

            Notification saved = notificationRepository.save(
                    Notification.builder()
                            .user(event.getInvitee())
                            .title(title)
                            .message(message)
                            .type("MEMBER_INVITED")
                            .refType("WORKSPACE")
                            .refId(event.getWorkspace().getId())
                            .build()
            );

            socketHandler.sendToUser(
                    event.getInvitee().getId().toString(),
                    notificationMapper.toResponse(saved));

            log.info("Gửi notification MEMBER_INVITED đến: {}",
                    event.getInvitee().getEmail());
        } catch (Exception e) {
            log.error("Lỗi gửi notification MEMBER_INVITED: {}", e.getMessage());
        }
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void handleFileShared(FileSharedEvent event) {
        try {
            String title = "File được chia sẻ với bạn";
            String message = String.format("%s đã chia sẻ file '%s' với bạn (quyền: %s)",
                    event.getSharedBy().getFullName(),
                    event.getFile().getOriginalName(),
                    event.getPermission().name());

            Notification saved = notificationRepository.save(
                    Notification.builder()
                            .user(event.getSharedWith())
                            .title(title)
                            .message(message)
                            .type("FILE_SHARED")
                            .refType("FILE")
                            .refId(event.getFile().getId())
                            .build()
            );

            socketHandler.sendToUser(
                    event.getSharedWith().getId().toString(),
                    notificationMapper.toResponse(saved));

            log.info("Gửi notification FILE_SHARED đến: {}",
                    event.getSharedWith().getEmail());
        } catch (Exception e) {
            log.error("Lỗi gửi notification FILE_SHARED: {}", e.getMessage());
        }
    }
}