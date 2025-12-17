package com.maze.internet_cafe.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/computers")
public class ComputerCommandController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a LOCK or UNLOCK command to a specific computer.
     * @param macAddress The MAC address of the target computer
     * @param action Either "LOCK" or "UNLOCK"
     */
    @PostMapping("/{macAddress}/command")
    public String sendCommand(@PathVariable String macAddress, @RequestParam String action) {
        // Construct the topic name exactly how the Agent subscribes to it
        String destination = "/topic/computer/" + macAddress;

        // Create the payload exactly as the Agent's Map.class expects
        Map<String, String> payload = Map.of("action", action.toUpperCase());

        // Push the message over the WebSocket
        messagingTemplate.convertAndSend(destination, payload);

        return "Command " + action + " sent to " + macAddress;
    }
}