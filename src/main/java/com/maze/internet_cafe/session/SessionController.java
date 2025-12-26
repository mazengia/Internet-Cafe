package com.maze.internet_cafe.session;

import com.maze.internet_cafe.session.dto.SessionStartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;


    //    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")
    @PostMapping("/{computerId}/start")
    public ResponseEntity<Session> start(@PathVariable Long computerId, @Valid @RequestBody SessionStartRequest req) {

        Session dto = sessionService.start(computerId, req);
        return ResponseEntity.status(201).body(dto);
    }

    //    @PreAuthorize("hasAnyRole('ADMIN','AGENT','USER')")

    @PostMapping("/{computerId}/stop-running")
    public ResponseEntity<Session> stopRunning(@PathVariable Long computerId) {
        Session stopped = sessionService.stopRunningSession(computerId);
        return ResponseEntity.ok(stopped);
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<Session>> list(
            @RequestParam(required = false) Long computerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Session> result;
        if (computerId != null) {
            result = sessionService.listByComputer(computerId, pageable);
        } else {
            result = sessionService.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }
}
