package fu.se.myplatform.config;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // Broker cho kênh chung và hàng đợi riêng
        config.setApplicationDestinationPrefixes("/app"); // Tiền tố cho endpoint ứng dụng
        config.setUserDestinationPrefix("/user"); // Tiền tố cho đích người dùng (dùng trong chat riêng)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket (SockJS fallback) cho client kết nối
        registry.addEndpoint("/ws-chat").setAllowedOrigins("*").withSockJS();
    }
}
