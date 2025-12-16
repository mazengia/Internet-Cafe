package com.maze.internet_cafe.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyReportDto {
    private LocalDate date;
    private long totalSessions;
    private long totalMinutes;
    private BigDecimal totalRevenue;
}

