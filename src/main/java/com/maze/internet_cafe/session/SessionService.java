package com.maze.internet_cafe.session;

import com.maze.internet_cafe.branch.BranchRepository;
import com.maze.internet_cafe.computer.Computer;
import com.maze.internet_cafe.computer.ComputerRepository;
import com.maze.internet_cafe.computer.ComputerStatus;
import com.maze.internet_cafe.exception.EntityNotFoundException;
import com.maze.internet_cafe.model.User;
import com.maze.internet_cafe.service.BillingService;
import com.maze.internet_cafe.session.dto.SessionStartRequest;
import com.maze.internet_cafe.session.dto.SessionStopRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ComputerRepository computerRepository;
    private final BillingService billingService;

    public Session start(Long computerId, SessionStartRequest req) {
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new EntityNotFoundException(Computer.class, "id", computerId.toString()));

        if (computer.getStatus() == com.maze.internet_cafe.computer.ComputerStatus.IN_USE) {
            throw new IllegalStateException("Computer is already in use");
        }

        Session s = new Session();
        s.setComputer(computer);
        s.setStartTime(LocalDateTime.now());
        if (req.getPricePerHour() != null) {
            s.setPricePerHour(req.getPricePerHour());
        } else if (computer.getBranch() != null && computer.getBranch().getPricePerHour() != null) {
            s.setPricePerHour(computer.getBranch().getPricePerHour());
        } else {
            throw new IllegalStateException("No price configured for session and branch");
        }
        s.setStatus(SessionStatus.RUNNING);

        computer.setStatus(com.maze.internet_cafe.computer.ComputerStatus.IN_USE);
        computerRepository.save(computer);

        return sessionRepository.save(s);
    }

    /**
     * Stop a session. This method enforces permission: the actingUser must be the owner of the session
     * or must have ROLE_ADMIN or ROLE_AGENT authority.
     *
     * @param computerId  computer id (for validation)
     * @param sessionId   session id to stop
     * @param req         optional stop request (contains endTime)
     * @param actingUser  the user performing the action (may be null for system calls)
     * @param authorities authorities of the acting principal
     * @return updated SessionDto
     */
    public Session stop(Long computerId, Long sessionId, SessionStopRequest req, User actingUser, Collection<? extends GrantedAuthority> authorities) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "id", sessionId.toString()));

        if (s.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Session is not running");
        }

        // Permission check: owner or admin/agent
        boolean hasAdminOrAgent = authorities != null && authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_AGENT".equals(a.getAuthority()));
        if (!hasAdminOrAgent) {
            throw new SecurityException("Forbidden");
        }

        LocalDateTime end = req != null && req.getEndTime() != null ? req.getEndTime() : LocalDateTime.now();
        s.setEndTime(end);
        s.setStatus(SessionStatus.FINISHED);

        billingService.calculate(s);

        Computer computer = s.getComputer();
        computer.setStatus(com.maze.internet_cafe.computer.ComputerStatus.AVAILABLE);
        computerRepository.save(computer);

        return sessionRepository.save(s);
    }


    public Page<Session> listByComputer(Long computerId, Pageable pageable) {
        return sessionRepository.findByComputerId(computerId, pageable);
    }

    public Session get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "id", id.toString()));
    }


    @Transactional
    public void terminateByName(String name) {

        Computer computer = computerRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(Computer.class, "name", name));

        List<Session> runningSessions =
                sessionRepository.findAllByComputerIdAndStatus(
                        computer.getId(), SessionStatus.RUNNING);

        if (runningSessions.isEmpty()) {
            return;
        }

        // pick the latest session
        Session session = runningSessions.stream()
                .max(Comparator.comparing(Session::getStartTime))
                .orElseThrow();

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FINISHED);

        long minutes = ChronoUnit.MINUTES.between(
                session.getStartTime(), session.getEndTime());

        BigDecimal cost = session.getPricePerHour()
                .multiply(BigDecimal.valueOf(Math.max(minutes, 1)))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        session.setTotalMinutes(minutes);
        session.setTotalCost(cost);

        computer.setStatus(ComputerStatus.AVAILABLE);

        sessionRepository.save(session);
        computerRepository.save(computer);
    }


    // New helper: stop the running session for a computer by id without requiring authentication
    @Transactional
    public Session stopRunningSession(Long computerId) {
        Session session = sessionRepository.findByComputerIdAndStatus(computerId, SessionStatus.RUNNING)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "computerId", computerId.toString()));

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FINISHED);

        billingService.calculate(session);

        Computer computer = session.getComputer();
        computer.setStatus(ComputerStatus.AVAILABLE);
        computerRepository.save(computer);

        return sessionRepository.save(session);
    }

    // New helper: terminate by computer id (similar to terminateByName but using id)
    @Transactional
    public void terminateByComputerId(Long computerId) {
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new EntityNotFoundException(Computer.class, "id", computerId.toString()));

        sessionRepository.findByComputerIdAndStatus(computer.getId(), SessionStatus.RUNNING)
                .ifPresent(session -> {
                    session.setEndTime(LocalDateTime.now());
                    session.setStatus(SessionStatus.FINISHED);

                    long minutes = ChronoUnit.MINUTES.between(session.getStartTime(), session.getEndTime());
                    BigDecimal cost = session.getPricePerHour()
                            .multiply(BigDecimal.valueOf(Math.max(minutes, 1)))
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                    session.setTotalMinutes(minutes);
                    session.setTotalCost(cost);

                    computer.setStatus(ComputerStatus.AVAILABLE);

                    sessionRepository.save(session);
                    computerRepository.save(computer);
                });
    }
}
