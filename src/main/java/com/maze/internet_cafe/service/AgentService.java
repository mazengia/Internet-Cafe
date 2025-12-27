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

    public static final String SERVER = "http://localhost:8052";
    private static final String WS_SERVER = "ws://localhost:8052/ws";

    /* ===================== APPLICATION START ===================== */

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        System.out.println(">>> Agent Application Started.");

        ComputerDto computer = null;
        while (computer == null) {
            computer = registerAgent();
            if (computer == null) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {
                }
            }
        }

        this.currentComputerId = computer.getId();

        startAutoSession(currentComputerId);
        registerShutdownHook();
        connectWebSocket();
    }

    /* ===================== REGISTRATION ===================== */

    public ComputerDto registerAgent() {
        ComputerCreateDto dto = new ComputerCreateDto();
        dto.setName(NetworkUtil.machineName());
        dto.setMacAddress(NetworkUtil.mac());
        dto.setIpAddress(NetworkUtil.ip());
        dto.setOsType(OSType.fromName(System.getProperty("os.name")).name());

        try {
            return WebClient.create(SERVER)
                    .post()
                    .uri("/computers")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(ComputerDto.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            System.err.println("!!! Registration failed: " + e.getMessage());
            return null;
        }
    }

    /* ===================== SESSION CONTROL ===================== */

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
            System.out.println(">>> Session already active. Continuing.");
        } catch (Exception e) {
            System.err.println(">>> Could not start session: " + e.getMessage());
        }
    }

    public void stopActiveSession() {
        System.out.println(">>> Stopping session for " + NetworkUtil.machineName());
        terminateSessionBestEffort();
    }

    public void restartSession() {
        if (currentComputerId != null) {
            startAutoSession(currentComputerId);
        }
    }

    /* ===================== SHUTDOWN HANDLING ===================== */

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">>> JVM SHUTDOWN detected");
            handleSystemShutdown();
        }, "agent-shutdown-hook"));
    }

    public void handleSystemShutdown() {
        // MUST be fast & non-blocking
        terminateSessionBestEffort();
        closeWebSocket();
    }

    private void terminateSessionBestEffort() {
        if (currentComputerId == null) {
            System.err.println(">>> Cannot terminate session: computerId is null");
            return;
        }

        try {
            // Stop the running session
            WebClient.create(SERVER)
                    .post()
                    .uri("/sessions/{computerId}/stop-running", currentComputerId)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(2));

            System.out.println(">>> Session stopped on server (computerId=" + currentComputerId + ")");

            // Also update computer status to SHUTDOWN
            WebClient.create(SERVER)
                    .post()
                    .uri("/computers/{id}/shoutdown", currentComputerId)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(2));

            System.out.println(">>> Computer status updated to SHUTDOWN (computerId=" + currentComputerId + ")");

        } catch (Exception e) {
            System.err.println(">>> Session stop or shutdown update failed during termination: " + e.getMessage());
        }
    }



    /* ===================== WEBSOCKET ===================== */

    public void connectWebSocket() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        List<MessageConverter> converters = new ArrayList<>();
        converters.add(new MappingJackson2MessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converters));

        stompClient.connectAsync(WS_SERVER, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                System.out.println(">>> WebSocket connected");

                session.subscribe("/topic/computer/" + NetworkUtil.machineName(),
                        new StompFrameHandler() {
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
                System.err.println(">>> WS error: " + exception.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(5);
                    connectWebSocket();
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void closeWebSocket() {
        HeartbeatTask.stop();
    }

    private void processCommand(Map<String, String> command) {
        String action = command.get("action");

        if ("LOCK".equalsIgnoreCase(action)) {
            LockService.lock();
        } else if ("UNLOCK".equalsIgnoreCase(action)) {
            System.out.println(">>> Received UNLOCK command");
        }
    }
}
