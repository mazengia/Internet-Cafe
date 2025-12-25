package com.maze.internet_cafe.service;


/**
 * Command sent from server → computer agent via WebSocket (STOMP)
 */
public record ComputerCommand(
        Long computerId,
        String action
) {
}
