package com.maze.internet_cafe.computer.dto;

import com.maze.internet_cafe.computer.ComputerStatus;
import com.maze.internet_cafe.computer.OSType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComputerDto {
    private Long id;
    private String name;
    private OSType osType;
    private String macAddress;
    private String ipAddress;
    private ComputerStatus status;
    private Long branchId;
    private LocalDateTime lastHeartbeat;
}

