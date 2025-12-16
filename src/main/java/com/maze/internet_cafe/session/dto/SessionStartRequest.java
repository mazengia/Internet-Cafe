package com.maze.internet_cafe.session.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionStartRequest {

    @DecimalMin(value = "0.01")
    private BigDecimal pricePerHour;

    private Long userId; // optional: associate session with user
}
