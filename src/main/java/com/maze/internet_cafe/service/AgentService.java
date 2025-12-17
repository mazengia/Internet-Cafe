package com.maze.internet_cafe.service;

import com.maze.internet_cafe.computer.OSType;
import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
import com.maze.internet_cafe.computer.dto.ComputerDto;
import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AgentService {
    private Long currentComputerId;
    static final String SERVER = "http://localhost:8080";
    static final String WS_SERVER = "ws://localhost:8080/ws";

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        System.out.println(">>> Agent Application Started.");
        LockService.lock();

        ComputerDto computer = null;
        while (computer == null) {
            computer = registerAgent();
            if (computer == null) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception ignored) {
                }
            }
        }
        this.currentComputerId = computer.getId();
        startAutoSession(currentComputerId);
        registerShutdownHook();
        connectWebSocket();
    }

    public ComputerDto registerAgent() {
        ComputerCreateDto dto = new ComputerCreateDto();
        dto.setName(NetworkUtil.machineName());
        dto.setMacAddress(NetworkUtil.mac());
        dto.setIpAddress(NetworkUtil.ip());
        dto.setOsType(OSType.fromName(System.getProperty("os.name")).name());

        try {
            return WebClient.create(SERVER)
                    .post()
                    .uri("/api/v1/computers")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(ComputerDto.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            System.err.println("!!! Registration failed: " + e.getMessage());
            return null;
        }
    }

    public void startAutoSession(Long computerId) {
        try {
            WebClient.create(SERVER)
                    .post()
                    .uri("/sessions/" + computerId + "/start")
                    .bodyValue(Map.of())
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            System.out.println(">>> Session started successfully.");
        } catch (WebClientResponseException.Conflict e) {
            System.out.println(">>> Session already active on server. Resuming status...");
        } catch (Exception e) {
            System.err.println(">>> Could not start session: " + e.getMessage());
        }
    }

    public void connectWebSocket() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        List<MessageConverter> converters = new ArrayList<>();
        converters.add(new MappingJackson2MessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converters));

        stompClient.connectAsync(WS_SERVER, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println(">>> WebSocket Online.");
                session.subscribe("/topic/computer/" + NetworkUtil.mac(), new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Map.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        processCommand((Map<String, String>) payload);
                    }
                });
                HeartbeatTask.start();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println(">>> WS Error: " + exception.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(5);
                    connectWebSocket();
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void processCommand(Map<String, String> command) {
        String action = command.get("action");
        if ("LOCK".equalsIgnoreCase(action)) {
            LockService.lock();
        } else if ("UNLOCK".equalsIgnoreCase(action)) {
            System.out.println(">>> Received UNLOCK Command");
            // Logic to hide overlay
        }
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // We use the Machine Name as the unique identifier for terminateByName
                WebClient.create(SERVER)
                        .post()
                        .uri("/sessions/terminate")
                        .bodyValue(Map.of("mac", NetworkUtil.machineName()))
                        .retrieve()
                        .toBodilessEntity()
                        .block(Duration.ofSeconds(2));
            } catch (Exception ignored) {
            }
        }));
    }
    public void stopActiveSession() {
        try {
            WebClient.create(SERVER)
                    .post()
                    .uri("/sessions/terminate")
                    .bodyValue(Map.of("mac", NetworkUtil.machineName()))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(2));
            System.out.println(">>> Session terminated successfully on server.");
        } catch (Exception e) {
            System.err.println(">>> Termination failed: " + e.getMessage());
        }
    }

    public void restartSession() {
        if (currentComputerId != null) {
            startAutoSession(currentComputerId);
        }
    }
    public Long getCurrentComputerId() {
        return currentComputerId;
    }
}