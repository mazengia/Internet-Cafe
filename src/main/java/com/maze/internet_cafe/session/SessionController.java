package com.maze.internet_cafe.session;

import com.maze.internet_cafe.model.User;
import com.maze.internet_cafe.model.UserRepository;
import com.maze.internet_cafe.session.dto.SessionDto;
import com.maze.internet_cafe.session.dto.SessionStartRequest;
import com.maze.internet_cafe.session.dto.SessionStopRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")
    @PostMapping("/computers/{computerId}/sessions/start")
    public ResponseEntity<SessionDto> start(@PathVariable Long computerId, @Valid @RequestBody SessionStartRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.getName() != null) {
            Optional<User> u = userRepository.findByUsername(auth.getName());
            if (u.isPresent()) user = u.get();
        }
        SessionDto dto = sessionService.start(computerId, req, user);
        return ResponseEntity.status(201).body(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")
    @PostMapping("/computers/{computerId}/sessions/{sessionId}/stop")
    public ResponseEntity<SessionDto> stop(@PathVariable Long computerId, @PathVariable Long sessionId, @RequestBody(required = false) SessionStopRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User actingUser = null;
        if (auth != null && auth.getName() != null) {
            actingUser = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        Collection<? extends GrantedAuthority> authorities = auth != null ? auth.getAuthorities() : null;

        SessionDto dto = sessionService.stop(computerId, sessionId, req, actingUser, authorities);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/computers/{computerId}/sessions")
    public ResponseEntity<Page<SessionDto>> listByComputer(@PathVariable Long computerId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SessionDto> result = sessionService.listByComputer(computerId, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.get(id));
    }
}
