package com.maze.internet_cafe.service;

import com.maze.internet_cafe.session.SessionRepository;
import com.maze.internet_cafe.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

            // Construct payload without Optional
            Map<Object, Object> payload = Map.of(
                    "id", s.getId(),
                    "elapsed", sec,
                    "macAddress", s.getComputer().getMacAddress(),
                    "name", s.getComputer().getName()
            );

            // Correct: template.convertAndSend(String destination, Object payload)
            template.convertAndSend("/topic/sessions", payload);
        });
    }
}