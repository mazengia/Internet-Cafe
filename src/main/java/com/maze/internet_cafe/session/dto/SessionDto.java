package com.maze.internet_cafe.session.dto;

import com.maze.internet_cafe.session.SessionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SessionDto {
    private Long id;
    private Long computerId;
    private Long branchId;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal pricePerHour;
    private Long totalMinutes;
    private BigDecimal totalCost;
    private SessionStatus status;
}
