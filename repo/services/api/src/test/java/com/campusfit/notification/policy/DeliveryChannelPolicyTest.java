package com.campusfit.notification.policy;

import com.campusfit.notification.entity.NotificationDelivery;
import com.campusfit.notification.service.DeliveryChannelPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DeliveryChannelPolicyTest {

    private DeliveryChannelPolicy createPolicy(boolean email, boolean sms, boolean wecom) {
        DeliveryChannelPolicy policy = new DeliveryChannelPolicy();
        try {
            Field emailField = DeliveryChannelPolicy.class.getDeclaredField("emailEnabled");
            emailField.setAccessible(true);
            emailField.setBoolean(policy, email);

            Field smsField = DeliveryChannelPolicy.class.getDeclaredField("smsEnabled");
            smsField.setAccessible(true);
            smsField.setBoolean(policy, sms);

            Field wecomField = DeliveryChannelPolicy.class.getDeclaredField("wecomEnabled");
            wecomField.setAccessible(true);
            wecomField.setBoolean(policy, wecom);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return policy;
    }

    @Test
    void getEnabledChannels_onlyInApp_whenAllDisabled() {
        DeliveryChannelPolicy policy = createPolicy(false, false, false);

        List<NotificationDelivery.DeliveryChannel> channels = policy.getEnabledChannels();

        assertThat(channels).hasSize(1);
        assertThat(channels).containsExactly(NotificationDelivery.DeliveryChannel.IN_APP);
    }

    @Test
    void getEnabledChannels_onlyInApp_evenWhenFlagsEnabled() {
        // External adapters are not implemented, so flags are ignored
        DeliveryChannelPolicy policy = createPolicy(true, true, true);

        List<NotificationDelivery.DeliveryChannel> channels = policy.getEnabledChannels();

        assertThat(channels).hasSize(1);
        assertThat(channels).containsExactly(NotificationDelivery.DeliveryChannel.IN_APP);
    }
}
