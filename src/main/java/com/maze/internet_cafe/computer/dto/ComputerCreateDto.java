package com.maze.internet_cafe.computer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ComputerCreateDto {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address")
    private String macAddress;

    private String ipAddress;

    private String osType;

    private Long branchId;
}

