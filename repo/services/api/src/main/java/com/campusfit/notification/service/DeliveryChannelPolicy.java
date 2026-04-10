package com.campusfit.notification.service;

import com.campusfit.notification.entity.NotificationDelivery;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryChannelPolicy {

    private static final Logger log = LoggerFactory.getLogger(DeliveryChannelPolicy.class);

    // EMAIL, SMS, and WECOM adapters are not yet implemented.
    // These flags are read to warn operators on startup when they are misconfigured.
    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notification.sms-enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notification.wecom-enabled:false}")
    private boolean wecomEnabled;

    @PostConstruct
    void warnUnimplementedChannels() {
        if (emailEnabled) {
            log.warn("NOTIFICATION: app.notification.email-enabled=true but EMAIL adapter is not implemented. " +
                     "Email deliveries will not be sent. Disable this flag or implement the adapter.");
        }
        if (smsEnabled) {
            log.warn("NOTIFICATION: app.notification.sms-enabled=true but SMS adapter is not implemented. " +
                     "SMS deliveries will not be sent. Disable this flag or implement the adapter.");
        }
        if (wecomEnabled) {
            log.warn("NOTIFICATION: app.notification.wecom-enabled=true but WeCom adapter is not implemented. " +
                     "WeCom deliveries will not be sent. Disable this flag or implement the adapter.");
        }
    }

    /**
     * Returns the enabled delivery channels based on configuration flags.
     * IN_APP is always included. EMAIL, SMS, and WECOM are included when their
     * respective flags are enabled (adapters are not yet implemented — delivery
     * attempts for those channels will be logged as warnings).
     */
    public List<NotificationDelivery.DeliveryChannel> getEnabledChannels() {
        List<NotificationDelivery.DeliveryChannel> channels = new ArrayList<>();
        channels.add(NotificationDelivery.DeliveryChannel.IN_APP);
        if (emailEnabled) channels.add(NotificationDelivery.DeliveryChannel.EMAIL);
        if (smsEnabled) channels.add(NotificationDelivery.DeliveryChannel.SMS);
        if (wecomEnabled) channels.add(NotificationDelivery.DeliveryChannel.WECOM);
        return channels;
    }
}
