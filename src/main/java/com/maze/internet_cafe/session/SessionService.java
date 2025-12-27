package com.maze.internet_cafe.session;

import com.maze.internet_cafe.computer.Computer;
import com.maze.internet_cafe.computer.ComputerRepository;
import com.maze.internet_cafe.computer.ComputerStatus;
import com.maze.internet_cafe.exception.EntityNotFoundException;
import com.maze.internet_cafe.service.BillingService;
import com.maze.internet_cafe.session.dto.SessionStartRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ComputerRepository computerRepository;
    private final BillingService billingService;

    // ================= START SESSION =================
    public Session start(Long computerId, SessionStartRequest req) {

        // 1. Stop running session if exists
        List<Session> sessions =
                sessionRepository.findAllByComputerIdAndStatus(
                        computerId, SessionStatus.RUNNING
                );

        sessions.forEach(this::stopRunningSessionInternal);

        // 2. Load computer
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() ->
                        new EntityNotFoundException(Computer.class, "id", computerId.toString())
                );

        // 3. Create new session
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

        // 4. Update computer status
        computer.setStatus(ComputerStatus.IN_USE);
        computerRepository.save(computer);

        // 5. Save session
        return sessionRepository.save(s);
    }

    // ================= LISTING =================
    public Page<Session> listByComputer(Long computerId, Pageable pageable) {
        return sessionRepository.findByComputerId(computerId, pageable);
    }

    public Page<Session> findAll(Pageable pageable) {
        return sessionRepository.findAllByOrderByStatusDescIdDesc(pageable);
    }

    public Session get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(Session.class, "id", id.toString())
                );
    }

    // ================= PUBLIC STOP =================
    @Transactional
    public Session stopRunningSession(Long computerId) {

        List<Session> sessions = sessionRepository
                .findAllByComputerIdAndStatus(computerId, SessionStatus.RUNNING);

        if (sessions.isEmpty()) {
            throw new EntityNotFoundException(
                    Session.class,
                    "computerId & status",
                    computerId + " / " + SessionStatus.RUNNING
            );
        }

        Session lastStopped = null;

        for (Session session : sessions) {
            stopRunningSessionInternal(session);
            lastStopped = session;
        }

        return lastStopped;
    }

    // ================= INTERNAL STOP =================
    private void stopRunningSessionInternal(Session session) {

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FINISHED);

        billingService.calculate(session);

        Computer computer = session.getComputer();
        computer.setStatus(ComputerStatus.AVAILABLE);
        computerRepository.save(computer);

        sessionRepository.save(session);
    }
}
