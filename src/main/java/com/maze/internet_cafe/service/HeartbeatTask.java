package com.maze.internet_cafe.service;

import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HeartbeatTask {

    private static ScheduledExecutorService executor;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private HeartbeatTask() {
        // utility class
    }

    /* ===================== START ===================== */

    public static synchronized void start() {
        if (running.get()) {
            return; // already running
        }

        running.set(true);

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-task");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(
                HeartbeatTask::sendHeartbeat,
                0,
                10,
                TimeUnit.SECONDS
        );

        System.out.println(">>> Heartbeat started");
    }

    /* ===================== STOP ===================== */

    public static synchronized void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        System.out.println(">>> Heartbeat stopped");
    }


    private static void sendHeartbeat() {
        try {
            WebClient.create(AgentService.SERVER)
                    .post()
                    .uri("/computers/heartbeat")
                    .bodyValue(Map.of("name", NetworkUtil.machineName()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .subscribe(
                            v -> { },
                            e -> System.err.println(">>> Heartbeat failed: " + e.getMessage())
                    );
        } catch (Exception e) {
            System.err.println(">>> Heartbeat exception: " + e.getMessage());
        }
    }
}
