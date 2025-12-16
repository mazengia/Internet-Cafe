package com.maze.internet_cafe.service;

import com.maze.internet_cafe.session.Session;
import com.maze.internet_cafe.session.SessionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private final BillingService billingService = new BillingService();

    @Test
    void calculate_exactOneHour() {
        Session s = new Session();
        s.setStartTime(LocalDateTime.of(2025,1,1,10,0));
        s.setEndTime(LocalDateTime.of(2025,1,1,11,0));
        s.setPricePerHour(BigDecimal.valueOf(6.00));

        billingService.calculate(s);

        assertEquals(60L, s.getTotalMinutes());
        assertEquals(0, s.getTotalCost().compareTo(BigDecimal.valueOf(6.00)));
    }

    @Test
    void calculate_partialMinutes_roundUp() {
        Session s = new Session();
        s.setStartTime(LocalDateTime.of(2025,1,1,10,0));
        s.setEndTime(LocalDateTime.of(2025,1,1,10,1));
        s.setPricePerHour(BigDecimal.valueOf(10.00));

        billingService.calculate(s);

        assertEquals(1L, s.getTotalMinutes());
        // 1 minute at $10/hr = 10 * 1 / 60 = 0.1666 -> rounding UP to 2 decimals = 0.17
        assertEquals(0, s.getTotalCost().compareTo(new BigDecimal("0.17")));
    }
}

