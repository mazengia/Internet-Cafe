package com.maze.internet_cafe.session;

import com.maze.internet_cafe.branch.BranchRepository;
import com.maze.internet_cafe.computer.Computer;
import com.maze.internet_cafe.computer.ComputerRepository;
import com.maze.internet_cafe.exception.EntityNotFoundException;
import com.maze.internet_cafe.model.User;
import com.maze.internet_cafe.service.BillingService;
import com.maze.internet_cafe.session.dto.SessionDto;
import com.maze.internet_cafe.session.dto.SessionStartRequest;
import com.maze.internet_cafe.session.dto.SessionStopRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ComputerRepository computerRepository;
    private final BranchRepository branchRepository;
    private final BillingService billingService;
    private final ModelMapper mapper = new ModelMapper();

    public SessionService(SessionRepository sessionRepository, ComputerRepository computerRepository, BranchRepository branchRepository, BillingService billingService) {
        this.sessionRepository = sessionRepository;
        this.computerRepository = computerRepository;
        this.branchRepository = branchRepository;
        this.billingService = billingService;
    }

    public SessionDto start(Long computerId, SessionStartRequest req, User user) {
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new EntityNotFoundException(Computer.class, "id", computerId.toString()));

        if (computer.getStatus() == com.maze.internet_cafe.computer.ComputerStatus.IN_USE) {
            throw new IllegalStateException("Computer is already in use");
        }

        Session s = new Session();
        s.setComputer(computer);
        s.setBranch(computer.getBranch());
        s.setStartTime(LocalDateTime.now());
        if (req.getPricePerHour() != null) {
            s.setPricePerHour(req.getPricePerHour());
        } else if (computer.getBranch() != null && computer.getBranch().getPricePerHour() != null) {
            s.setPricePerHour(computer.getBranch().getPricePerHour());
        } else {
            throw new IllegalStateException("No price configured for session and branch");
        }
        s.setStatus(SessionStatus.RUNNING);
        s.setUser(user);

        computer.setStatus(com.maze.internet_cafe.computer.ComputerStatus.IN_USE);
        computerRepository.save(computer);

        Session saved = sessionRepository.save(s);

        return toDto(saved);
    }

    /**
     * Stop a session. This method enforces permission: the actingUser must be the owner of the session
     * or must have ROLE_ADMIN or ROLE_AGENT authority.
     *
     * @param computerId    computer id (for validation)
     * @param sessionId     session id to stop
     * @param req           optional stop request (contains endTime)
     * @param actingUser    the user performing the action (may be null for system calls)
     * @param authorities   authorities of the acting principal
     * @return updated SessionDto
     */
    public SessionDto stop(Long computerId, Long sessionId, SessionStopRequest req, User actingUser, Collection<? extends GrantedAuthority> authorities) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "id", sessionId.toString()));

        if (s.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Session is not running");
        }

        // Permission check: owner or admin/agent
        boolean isOwner = actingUser != null && s.getUser() != null && actingUser.getId() != null && actingUser.getId().equals(s.getUser().getId());
        boolean hasAdminOrAgent = authorities != null && authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_AGENT".equals(a.getAuthority()));
        if (!isOwner && !hasAdminOrAgent) {
            throw new SecurityException("Forbidden");
        }

        LocalDateTime end = req != null && req.getEndTime() != null ? req.getEndTime() : LocalDateTime.now();
        s.setEndTime(end);
        s.setStatus(SessionStatus.FINISHED);

        billingService.calculate(s);

        // mark computer available
        Computer computer = s.getComputer();
        computer.setStatus(com.maze.internet_cafe.computer.ComputerStatus.AVAILABLE);
        computerRepository.save(computer);

        Session saved = sessionRepository.save(s);
        return toDto(saved);
    }

    public SessionDto toDto(Session s) {
        SessionDto dto = mapper.map(s, SessionDto.class);
        if (s.getComputer() != null) dto.setComputerId(s.getComputer().getId());
        if (s.getBranch() != null) dto.setBranchId(s.getBranch().getId());
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            dto.setUsername(s.getUser().getUsername());
        }
        return dto;
    }

    public Page<SessionDto> listByComputer(Long computerId, Pageable pageable) {
        Page<Session> page = sessionRepository.findByComputerId(computerId, pageable);
        return page.map(this::toDto);
    }

    public SessionDto get(Long id) {
        return sessionRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Session.class, "id", id.toString()));
    }
}
