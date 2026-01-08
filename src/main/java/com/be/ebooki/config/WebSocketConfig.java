package com.be.ebooki.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompAuthInterceptor stompAuthInterceptor;
    private final StompChannelInterceptor stompChannelInterceptor;

    //클라이언트로 받은 데이터 처리
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //구독 경로
        registry.enableSimpleBroker("/topic"); //서버 -> 클라이언트 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 -> 서버 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") //sockjs 연결
                .setAllowedOriginPatterns("*")
                .addInterceptors(stompAuthInterceptor);
                //.withSockJS();
        //ws://localhost:8080/ws
    }
}
