package com.maze.internet_cafe.service;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker
        // /topic is for broadcasting to many, /queue is for one-to-one
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This MUST match your Agent's WS_SERVER URL
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Allow connections from any IP (cafe workstations)

        // Note: We removed .withSockJS() because your Java Agent uses a pure STOMP client
    }
}