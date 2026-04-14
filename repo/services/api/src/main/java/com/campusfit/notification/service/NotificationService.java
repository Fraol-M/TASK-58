package com.campusfit.notification.service;

import com.campusfit.notification.dto.NotificationRequest;
import com.campusfit.notification.dto.NotificationResponse;
import com.campusfit.notification.entity.Notification;
import com.campusfit.notification.entity.NotificationDelivery;
import com.campusfit.notification.entity.NotificationTarget;
import com.campusfit.notification.repository.NotificationDeliveryRepository;
import com.campusfit.notification.repository.NotificationRepository;
import com.campusfit.notification.repository.NotificationTargetRepository;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTargetRepository targetRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final InAppDeliveryService inAppDeliveryService;
    private final DeliveryChannelPolicy deliveryChannelPolicy;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationTargetRepository targetRepository,
                               NotificationDeliveryRepository deliveryRepository,
                               InAppDeliveryService inAppDeliveryService,
                               DeliveryChannelPolicy deliveryChannelPolicy) {
        this.notificationRepository = notificationRepository;
        this.targetRepository = targetRepository;
        this.deliveryRepository = deliveryRepository;
        this.inAppDeliveryService = inAppDeliveryService;
        this.deliveryChannelPolicy = deliveryChannelPolicy;
    }

    @Transactional
    public void create(NotificationRequest request, Long createdBy) {
        Notification notification = Notification.builder()
                .type(request.getType())
                .title(request.getTitle())
                .body(request.getBody())
                .createdBy(createdBy)
                .build();
        notification = notificationRepository.save(notification);

        List<NotificationDelivery.DeliveryChannel> enabledChannels = deliveryChannelPolicy.getEnabledChannels();

        for (Long userId : request.getTargetUserIds()) {
            NotificationTarget target = NotificationTarget.builder()
                    .notificationId(notification.getId())
                    .userId(userId)
                    .build();
            target = targetRepository.save(target);

            // Deliver via all enabled channels (currently only IN_APP)
            for (NotificationDelivery.DeliveryChannel channel : enabledChannels) {
                if (channel == NotificationDelivery.DeliveryChannel.IN_APP) {
                    inAppDeliveryService.deliver(target.getId());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationTarget> targets = targetRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return targets.map(target -> {
            Notification notification = notificationRepository.findById(target.getNotificationId())
                    .orElse(null);
            if (notification == null) {
                return null;
            }
            return NotificationResponse.builder()
                    .id(target.getId())
                    .notificationId(notification.getId())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .read(target.getReadAt() != null)
                    .readAt(target.getReadAt())
                    .createdAt(target.getCreatedAt())
                    .build();
        });
    }

    @Transactional
    public void markAsRead(Long targetId, Long userId) {
        NotificationTarget target = targetRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTarget", targetId));

        if (!target.getUserId().equals(userId)) {
            throw new com.campusfit.shared.exception.BusinessException(
                    "You do not have permission to mark this notification as read");
        }

        if (target.getReadAt() == null) {
            target.setReadAt(LocalDateTime.now());
            targetRepository.save(target);
        }
    }

    @Transactional(readOnly = true)
    public List<com.campusfit.notification.dto.DeliveryStatusResponse> getDeliveryStatus(Long notificationId) {
        notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        return targetRepository.findByNotificationId(notificationId)
                .stream()
                .map(target -> {
                    // Find the delivery record for this target, if any
                    var deliveries = deliveryRepository.findByTargetId(target.getId());
                    var delivery = deliveries.isEmpty() ? null : deliveries.get(0);

                    return com.campusfit.notification.dto.DeliveryStatusResponse.builder()
                            .targetId(target.getId())
                            .userId(target.getUserId())
                            .read(target.getReadAt() != null)
                            .readAt(target.getReadAt())
                            .deliveredAt(delivery != null ? delivery.getSentAt() : null)
                            .channel(delivery != null ? delivery.getChannel().name() : "IN_APP")
                            .build();
                })
                .collect(Collectors.toList());
    }
}
