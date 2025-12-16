package com.maze.internet_cafe.service;

import com.maze.internet_cafe.computer.OSType;
import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
import com.maze.internet_cafe.computer.dto.ComputerDto;
import com.maze.internet_cafe.utils.NetworkUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class AgentService {

    static final String SERVER = "http://localhost:8080"; // your app URL

    public void registerAgent() {
        ComputerCreateDto dto = new ComputerCreateDto();
        dto.setName("Agent-PC-1");
        dto.setMacAddress(NetworkUtil.mac());
        dto.setOsType(OSType.fromName(System.getProperty("os.name")).name());
        // Optional: dto.setBranchId(1L);
        try {
            WebClient.create(SERVER)
                    .post()
                    .uri("/api/v1/computers")
                    .bodyValue(dto)  // <-- send proper DTO
                    .retrieve()
                    .bodyToMono(ComputerDto.class)  // <-- expect the response DTO
                    .block();

            HeartbeatTask.start();
        } catch (WebClientResponseException e) {
            System.err.println("Failed to register agent: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        }
    }

}
