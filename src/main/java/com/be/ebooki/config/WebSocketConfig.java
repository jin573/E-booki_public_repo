package com.be.ebooki.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompAuthInterceptor stompAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //구독 경로
        registry.enableSimpleBroker("/topic"); //클라이언트에서 보낸 메세지를 받을 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 해당 주소를 구독하고 있는 클라이언트에게 전달할 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") //sockjs 연결
                .setAllowedOriginPatterns("*")
                .addInterceptors(stompAuthInterceptor)
                .withSockJS();
        //ws://localhost:8080/ws
    }
}
