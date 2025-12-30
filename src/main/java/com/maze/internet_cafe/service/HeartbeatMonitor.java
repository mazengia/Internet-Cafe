package com.maze.internet_cafe.service;

import com.maze.internet_cafe.computer.Computer;
import com.maze.internet_cafe.computer.ComputerRepository;
import com.maze.internet_cafe.computer.ComputerStatus;
import com.maze.internet_cafe.session.Session;
import com.maze.internet_cafe.session.SessionRepository;
import com.maze.internet_cafe.session.SessionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class HeartbeatMonitor {

    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    private final ComputerRepository computerRepository;
    private final SessionRepository sessionRepository;
    private final BillingService billingService;

    @Scheduled(fixedDelay = 40_000)
    @Transactional
    public void checkHeartbeats() {
        System.out.println("tttttttt=> Checking heartbeats...");
        Instant now = Instant.now();
        Instant cutoff = now.minus(TIMEOUT);

        List<Computer> deadComputers =
                computerRepository.findByLastHeartbeatBeforeAndStatusNot(
                        cutoff, ComputerStatus.SHUTDOWN);

        for (Computer computer : deadComputers) {

            List<Session> runningSessions =
                    sessionRepository.findByComputerIdAndStatus(
                            computer.getId(),
                            SessionStatus.RUNNING
                    );

            for (Session session : runningSessions) {

                Instant endTime = computer.getLastHeartbeat() != null
                        ? computer.getLastHeartbeat()
                        : Instant.now();
                session.setEndTime(
                        LocalDateTime.ofInstant(
                                endTime,
                                ZoneId.of("Africa/Addis_Ababa")
                        )
                );

//                session.setStatus(SessionStatus.FINISHED);
                session.setStatus(SessionStatus.ABORTED);

                billingService.calculate(session);

                sessionRepository.save(session);

                System.out.println(">>> Auto-stopped session " + session.getId()
                        + " for computer " + computer.getName());
            }

            computer.setStatus(ComputerStatus.SHUTDOWN_SUDDENLY);
            computerRepository.save(computer);
        }
    }
}
