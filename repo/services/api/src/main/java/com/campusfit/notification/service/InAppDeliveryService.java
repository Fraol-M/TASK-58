package com.campusfit.notification.service;

import com.campusfit.notification.entity.NotificationDelivery;
import com.campusfit.notification.repository.NotificationDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InAppDeliveryService {

    private final NotificationDeliveryRepository deliveryRepository;

    public InAppDeliveryService(NotificationDeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public NotificationDelivery deliver(Long targetId) {
        NotificationDelivery delivery = NotificationDelivery.builder()
                .targetId(targetId)
                .channel(NotificationDelivery.DeliveryChannel.IN_APP)
                .status(NotificationDelivery.DeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        return deliveryRepository.save(delivery);
    }
}
