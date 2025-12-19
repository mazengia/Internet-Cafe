package com.maze.internet_cafe.service;

import com.maze.internet_cafe.session.SessionRepository;
import com.maze.internet_cafe.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionBroadcaster {

    private final SimpMessagingTemplate template;
    private final SessionRepository sessionRepo;

    @Scheduled(fixedRate = 1000)
    public void broadcast() {
        sessionRepo.findByStatus(SessionStatus.RUNNING).forEach(s -> {
            long sec = ChronoUnit.SECONDS.between(s.getStartTime(), LocalDateTime.now());

            // Build a mutable payload and only include non-null values
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", s.getId());
            if (s.getComputer() != null) {
                payload.put("computerId", s.getComputer().getId());
                payload.put("macAddress", s.getComputer().getMacAddress());
                payload.put("name", s.getComputer().getName());
            }
            payload.put("elapsed", sec);
            payload.put("startTime", s.getStartTime() != null ? s.getStartTime().toString() : null);
            if (s.getPricePerHour() != null) payload.put("pricePerHour", s.getPricePerHour().toString());
            if (s.getTotalCost() != null) payload.put("totalCost", s.getTotalCost().toString());

            // Convert and send - cast payload to Object to disambiguate overload resolution
            template.convertAndSend("/topic/sessions", (Object) payload);
        });
    }
}