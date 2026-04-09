package com.campusfit.notification.dto;

import com.campusfit.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long notificationId;
    private Notification.NotificationType type;
    private String title;
    private String body;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
