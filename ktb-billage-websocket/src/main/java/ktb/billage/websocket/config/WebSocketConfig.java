package ktb.billage.websocket.config;

import ktb.billage.websocket.exception.StompErrorHandler;
import ktb.billage.websocket.interceptor.ChatroomSubscriptionInterceptor;
import ktb.billage.websocket.interceptor.StompAuthChannelInterceptor;
import ktb.billage.websocket.interceptor.StompLifecycleLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${cors.allowed.origin}")
    private String[] allowedOrigins;

    private final ObjectMapper objectMapper;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final ChatroomSubscriptionInterceptor chatroomSubscriptionInterceptor;
    private final StompLifecycleLoggingInterceptor stompLifecycleLoggingInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { // 웹소켓 연결을 위한 엔드포인트 billages.com/ws
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 서버에서 브로드캐스팅을 위한 토픽 프리픽스
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 웹소켓 요청을 위한 프리픽스
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompLifecycleLoggingInterceptor, stompAuthChannelInterceptor, chatroomSubscriptionInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompLifecycleLoggingInterceptor);
    }

    @Bean(name = "stompSubProtocolErrorHandler")
    public StompSubProtocolErrorHandler stompSubProtocolErrorHandler() {
        return new StompErrorHandler(objectMapper);
    }
}
