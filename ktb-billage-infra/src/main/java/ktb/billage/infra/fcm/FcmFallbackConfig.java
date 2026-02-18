package ktb.billage.infra.fcm;

import ktb.billage.websocket.application.port.ChatPushNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmFallbackConfig {
    @Bean
    @ConditionalOnMissingBean(ChatPushNotifier.class)
    public ChatPushNotifier chatPushNotifier() {
        return new NoOpChatPushNotifier();
    }
}
