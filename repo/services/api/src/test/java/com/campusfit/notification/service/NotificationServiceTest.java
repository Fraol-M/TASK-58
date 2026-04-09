package com.campusfit.notification.service;

import com.campusfit.notification.dto.NotificationRequest;
import com.campusfit.notification.dto.NotificationResponse;
import com.campusfit.notification.entity.Notification;
import com.campusfit.notification.entity.NotificationDelivery;
import com.campusfit.notification.entity.NotificationTarget;
import com.campusfit.notification.repository.NotificationDeliveryRepository;
import com.campusfit.notification.repository.NotificationRepository;
import com.campusfit.notification.repository.NotificationTargetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationTargetRepository targetRepository;
    @Mock
    private NotificationDeliveryRepository deliveryRepository;
    @Mock
    private InAppDeliveryService inAppDeliveryService;
    @Mock
    private DeliveryChannelPolicy deliveryChannelPolicy;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository,
                targetRepository, deliveryRepository, inAppDeliveryService, deliveryChannelPolicy);
    }

    @Test
    void createNotification_createsInAppDelivery() {
        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Test Notification")
                .body("This is a test")
                .targetUserIds(List.of(100L))
                .build();

        Notification savedNotification = Notification.builder()
                .id(1L).type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Test Notification").body("This is a test").createdBy(42L).build();

        NotificationTarget savedTarget = NotificationTarget.builder()
                .id(10L).notificationId(1L).userId(100L).build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(targetRepository.save(any(NotificationTarget.class))).thenReturn(savedTarget);
        when(deliveryChannelPolicy.getEnabledChannels())
                .thenReturn(List.of(NotificationDelivery.DeliveryChannel.IN_APP));
        when(inAppDeliveryService.deliver(10L)).thenReturn(
                NotificationDelivery.builder().id(1L).targetId(10L)
                        .channel(NotificationDelivery.DeliveryChannel.IN_APP)
                        .status(NotificationDelivery.DeliveryStatus.SENT).build());

        notificationService.create(request, 42L);

        verify(inAppDeliveryService).deliver(10L);
        verify(notificationRepository).save(any(Notification.class));
        verify(targetRepository).save(any(NotificationTarget.class));
    }

    @Test
    void getNotifications_returnsOnlyForCurrentUser() {
        Long userId = 100L;

        NotificationTarget target = NotificationTarget.builder()
                .id(10L).notificationId(1L).userId(userId)
                .createdAt(LocalDateTime.now()).build();

        Notification notification = Notification.builder()
                .id(1L).type(Notification.NotificationType.REMINDER)
                .title("Reminder").body("Check your goals").createdBy(42L).build();

        Page<NotificationTarget> targetPage = new PageImpl<>(List.of(target));

        when(targetRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(targetPage);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Page<NotificationResponse> result = notificationService.getForUser(userId, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Reminder");
        assertThat(result.getContent().get(0).getNotificationId()).isEqualTo(1L);
    }

    @Test
    void markRead_setsReadAt() {
        NotificationTarget target = NotificationTarget.builder()
                .id(10L).notificationId(1L).userId(100L)
                .readAt(null).build();

        when(targetRepository.findById(10L))
                .thenReturn(Optional.of(target));
        when(targetRepository.save(any(NotificationTarget.class))).thenReturn(target);

        notificationService.markAsRead(10L, 100L);

        assertThat(target.getReadAt()).isNotNull();
        verify(targetRepository).save(target);
    }

    @Test
    void getDeliveryStatus_returnsReadReceiptsForTargets() {
        Notification notification = Notification.builder()
                .id(1L)
                .type(Notification.NotificationType.ANNOUNCEMENT)
                .title("Test")
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationTarget readTarget = NotificationTarget.builder()
                .id(10L).notificationId(1L).userId(100L)
                .readAt(LocalDateTime.now().minusHours(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        NotificationTarget unreadTarget = NotificationTarget.builder()
                .id(11L).notificationId(1L).userId(200L)
                .readAt(null)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(targetRepository.findByNotificationId(1L))
                .thenReturn(List.of(readTarget, unreadTarget));

        NotificationDelivery delivery = NotificationDelivery.builder()
                .id(50L).targetId(10L)
                .channel(NotificationDelivery.DeliveryChannel.IN_APP)
                .status(NotificationDelivery.DeliveryStatus.SENT)
                .sentAt(LocalDateTime.now().minusDays(1))
                .build();

        when(deliveryRepository.findByTargetId(10L)).thenReturn(List.of(delivery));
        when(deliveryRepository.findByTargetId(11L)).thenReturn(List.of());

        var statuses = notificationService.getDeliveryStatus(1L);

        assertThat(statuses).hasSize(2);

        // First target: read
        var readStatus = statuses.stream().filter(s -> s.getUserId().equals(100L)).findFirst().orElseThrow();
        assertThat(readStatus.isRead()).isTrue();
        assertThat(readStatus.getReadAt()).isNotNull();
        assertThat(readStatus.getDeliveredAt()).isNotNull();
        assertThat(readStatus.getChannel()).isEqualTo("IN_APP");

        // Second target: unread
        var unreadStatus = statuses.stream().filter(s -> s.getUserId().equals(200L)).findFirst().orElseThrow();
        assertThat(unreadStatus.isRead()).isFalse();
        assertThat(unreadStatus.getReadAt()).isNull();
    }
}
