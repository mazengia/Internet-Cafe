package com.maze.internet_cafe.service;

import com.maze.internet_cafe.session.Session;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Service
public class BillingService {

    public void calculate(Session session) {
        long minutes = ChronoUnit.MINUTES.between(
                session.getStartTime(),
                session.getEndTime()
        );

        BigDecimal cost = BigDecimal.valueOf(minutes)
                .multiply(session.getPricePerHour())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.UP);

        session.setTotalMinutes(minutes);
        session.setTotalCost(cost);
    }
}
