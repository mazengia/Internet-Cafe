package com.maze.internet_cafe.service;

import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
public final class HeartbeatTask {

    private static ScheduledExecutorService executor;
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final int HEARTBEAT_INTERVAL_SECONDS = 10;

    private HeartbeatTask() { }

    public static synchronized void start() {
        if (running.get()) return;
        running.set(true);

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-task");
            t.setDaemon(true);
            return t;
        });

        // Send first heartbeat immediately
        executor.submit(HeartbeatTask::sendHeartbeat);

        // Schedule subsequent heartbeats with fixed delay after completion
        executor.scheduleWithFixedDelay(
                HeartbeatTask::sendHeartbeat,
                HEARTBEAT_INTERVAL_SECONDS,
                HEARTBEAT_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        System.out.println(">>> Heartbeat started");
    }

    public static synchronized void stop() {
        if (!running.get()) return;
        running.set(false);

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        System.out.println(">>> Heartbeat stopped");
    }

    private static void sendHeartbeat() {
        System.out.println(">>> Sending heartbeat...");
        try {
            WebClient.create(AgentService.SERVER)
                    .post()
                    .uri("/computers/heartbeat")
                    .bodyValue(Map.of("name", NetworkUtil.machineName()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(13))
                    .subscribe(
                            v -> System.out.println(">>> Heartbeat success"),
                            e -> System.err.println(">>> Heartbeat failed: " + e.getMessage())
                    );
        } catch (Exception e) {
            System.err.println(">>> Heartbeat exception: " + e.getMessage());
        }
    }
}
