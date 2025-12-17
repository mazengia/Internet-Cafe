package com.maze.internet_cafe.session;

import com.maze.internet_cafe.model.User;
import com.maze.internet_cafe.model.UserRepository;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    //    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")
    @PostMapping("/{computerId}/start")
    public ResponseEntity<Session> start(@PathVariable Long computerId, @Valid @RequestBody SessionStartRequest req) {

        Session dto = sessionService.start(computerId, req);
        return ResponseEntity.status(201).body(dto);
    }

    //    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")
    @PostMapping("/{computerId}/{sessionId}/stop")
    public ResponseEntity<Session> stop(@PathVariable Long computerId, @PathVariable Long sessionId, @RequestBody(required = false) SessionStopRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User actingUser = null;
        if (auth != null && auth.getName() != null) {
            actingUser = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        Collection<? extends GrantedAuthority> authorities = auth != null ? auth.getAuthorities() : null;

        Session dto = sessionService.stop(computerId, sessionId, req, actingUser, authorities);
        return ResponseEntity.ok(dto);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{computerId}/sessions")
    public ResponseEntity<Page<Session>> listByComputer(@PathVariable Long computerId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Session> result = sessionService.listByComputer(computerId, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Session> get(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.get(id));
    }
    @PostMapping("/terminate")
    public ResponseEntity<Void> terminate(@RequestBody Map<String, String> request) {
        String name = request.get("mac");
        sessionService.terminateByName(name);
        return ResponseEntity.ok().build();
    }
}
