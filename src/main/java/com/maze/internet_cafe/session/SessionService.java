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


    public Page<Session> listByComputer(Long computerId, Pageable pageable) {
        return sessionRepository.findByComputerId(computerId, pageable);
    }

    public Page<Session> findAll(Pageable pageable) {
        return sessionRepository.findAllByOrderByIdDesc(pageable);
    }

    public Session get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "id", id.toString()));
    }

    // New helper: stop the running session for a computer by id without requiring authentication
    @Transactional
    public Session stopRunningSession(Long computerId) {
        Session session = sessionRepository
                .findByComputerIdAndStatus(computerId, SessionStatus.RUNNING)
                .orElseThrow(() -> new EntityNotFoundException(
                        Session.class,
                        "computerId & status",
                        computerId + " / " + SessionStatus.RUNNING
                ));

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FINISHED);

        billingService.calculate(session);

        Computer computer = session.getComputer();
        computer.setStatus(ComputerStatus.AVAILABLE);
        computerRepository.save(computer);

        return sessionRepository.save(session);
    }

}
