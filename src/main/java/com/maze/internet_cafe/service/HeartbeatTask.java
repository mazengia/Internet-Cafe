package com.maze.internet_cafe.service;

import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatTask {

    public static void start() {
        ScheduledExecutorService exec =
                Executors.newSingleThreadScheduledExecutor();

        exec.scheduleAtFixedRate(() -> {
            WebClient.create(AgentService.SERVER)
                    .post()
//                    .uri("/api/agents/heartbeat")
                    .uri("/api/v1/computers/heartbeat")
                    .bodyValue(Map.of("mac", NetworkUtil.mac()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
        }, 0, 10, TimeUnit.SECONDS);
    }
}
