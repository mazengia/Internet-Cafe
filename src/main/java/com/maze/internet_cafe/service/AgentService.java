package com.maze.internet_cafe.service;

import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class AgentService {

    static final String SERVER = "https://cafe-system.com";

    public static void register() {
        WebClient.create(SERVER)
                .post()
                .uri("/api/agents/register")
                .bodyValue(Map.of(
                        "mac", NetworkUtil.mac(),
                        "os", System.getProperty("os.name")
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
