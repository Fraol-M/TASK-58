package com.campusfit.notification.controller;

import com.campusfit.notification.dto.NotificationRequest;
import com.campusfit.notification.dto.NotificationResponse;
import com.campusfit.notification.service.NotificationService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        Page<NotificationResponse> notifications = notificationService.getForUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        notificationService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<List<com.campusfit.notification.dto.DeliveryStatusResponse>>> getStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getDeliveryStatus(id)));
    }
}
