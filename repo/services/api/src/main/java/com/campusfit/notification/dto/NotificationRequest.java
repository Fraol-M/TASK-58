package com.campusfit.notification.dto;

import com.campusfit.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Type is required")
    private Notification.NotificationType type;

    @NotBlank(message = "Title is required")
    private String title;

    private String body;

    @NotEmpty(message = "At least one target user is required")
    private List<Long> targetUserIds;
}
