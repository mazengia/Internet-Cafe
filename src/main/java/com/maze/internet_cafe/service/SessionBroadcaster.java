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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionBroadcaster {

    private final SimpMessagingTemplate template;
    private final SessionRepository sessionRepo;

    @Scheduled(fixedRate = 1000)
    public void broadcast() {
        sessionRepo.findByStatus(SessionStatus.RUNNING).forEach(s -> {
            long sec = ChronoUnit.SECONDS.between(s.getStartTime(), LocalDateTime.now());
            Map<String, Object> payload = Map.of("id", s.getId(), "elapsed", sec);
            template.convertAndSend(Optional.of("/topic/sessions"), payload);
        });
    }
}
