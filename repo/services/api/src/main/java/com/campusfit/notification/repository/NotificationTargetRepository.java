package com.campusfit.notification.repository;

import com.campusfit.notification.entity.NotificationTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTargetRepository extends JpaRepository<NotificationTarget, Long> {

    Page<NotificationTarget> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<NotificationTarget> findByNotificationIdAndUserId(Long notificationId, Long userId);

    java.util.List<NotificationTarget> findByNotificationId(Long notificationId);

    long countByUserIdAndReadAtIsNull(Long userId);
}
