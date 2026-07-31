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
            String email,
            int page,
            int size,
            boolean unreadOnly) {

        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifPage;

        if (unreadOnly) {
            notifPage = notificationRepository
                    .findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                            user.getId(), pageable
                    );
        } else {
            notifPage = notificationRepository
                    .findAllByUserIdOrderByCreatedAtDesc(
                            user.getId(), pageable
                    );
        }

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

        // (1) Validate: notification phải thuộc về user này
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
    // EVENT HANDLERS
    // =============================================

    // (2) @Async — xử lý event trong thread riêng
    // Không làm chậm FileService khi upload
    @Async
    @EventListener  // (3) Spring tự gọi khi FileUploadedEvent được publish
    @Transactional
    @Override
    public void handleFileUploaded(FileUploadedEvent event) {
        var file = event.getFile();
        var uploader = event.getUploader();

        String title = "File mới được upload";
        String message = String.format(
                "%s đã upload file '%s'",
                uploader.getFullName(),
                file.getOriginalName()
        );

        // (4) Tạo notification cho từng recipient
        for (User recipient : event.getRecipients()) {
            Notification notification = Notification.builder()
                    .user(recipient)
                    .title(title)
                    .message(message)
                    .type("FILE_UPLOADED")
                    .refType("FILE")
                    .refId(file.getId())
                    .build();

            Notification saved = notificationRepository.save(notification);

            // (5) Push qua WebSocket
            socketHandler.sendToUser(
                    recipient.getId().toString(),
                    notificationMapper.toResponse(saved)
            );
        }

        log.info("Gửi {} notification FILE_UPLOADED",
                event.getRecipients().size());
    }

    @Async
    @EventListener
    @Transactional
    @Override
    public void handleMemberInvited(MemberInvitedEvent event) {
        var workspace = event.getWorkspace();
        var invitee = event.getInvitee();
        var inviter = event.getInviter();

        String title = "Bạn được mời vào workspace";
        String message = String.format(
                "%s đã mời bạn vào workspace '%s'",
                inviter.getFullName(),
                workspace.getName()
        );

        Notification notification = Notification.builder()
                .user(invitee)
                .title(title)
                .message(message)
                .type("MEMBER_INVITED")
                .refType("WORKSPACE")
                .refId(workspace.getId())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Push WebSocket chỉ đến invitee
        socketHandler.sendToUser(
                invitee.getId().toString(),
                notificationMapper.toResponse(saved)
        );

        log.info("Gửi notification MEMBER_INVITED đến: {}", invitee.getEmail());
    }

    @Async
    @EventListener
    @Transactional
    @Override
    public void handleFileShared(FileSharedEvent event) {
        var file = event.getFile();
        var sharedWith = event.getSharedWith();
        var sharedBy = event.getSharedBy();

        String title = "File được chia sẻ với bạn";
        String message = String.format(
                "%s đã chia sẻ file '%s' với bạn (quyền: %s)",
                sharedBy.getFullName(),
                file.getOriginalName(),
                event.getPermission().name()
        );

        Notification notification = Notification.builder()
                .user(sharedWith)
                .title(title)
                .message(message)
                .type("FILE_SHARED")
                .refType("FILE")
                .refId(file.getId())
                .build();

        Notification saved = notificationRepository.save(notification);

        socketHandler.sendToUser(
                sharedWith.getId().toString(),
                notificationMapper.toResponse(saved)
        );

        log.info("Gửi notification FILE_SHARED đến: {}", sharedWith.getEmail());
    }
}