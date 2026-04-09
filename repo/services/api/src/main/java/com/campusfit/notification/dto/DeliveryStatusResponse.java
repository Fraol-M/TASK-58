package com.campusfit.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusResponse {

    private Long targetId;
    private Long userId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
    private String channel;
}
