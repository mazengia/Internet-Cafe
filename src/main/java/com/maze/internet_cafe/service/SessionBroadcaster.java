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
    private final SessionRepository repo;

    @Scheduled(fixedRate = 1000)
    public void broadcast() {
        repo.findByStatus(SessionStatus.RUNNING)
                .forEach(s -> {
                    long sec = ChronoUnit.SECONDS.between(
                            s.getStartTime(),
                            LocalDateTime.now()
                    );

                    Map<String, Object> payload = Map.of(
                            "id", s.getId(),
                            "elapsed", sec
                    );

                    // Cast payload to Object to avoid ambiguity
                    template.convertAndSend("/topic/sessions", (Object) payload);
                });
    }
}
