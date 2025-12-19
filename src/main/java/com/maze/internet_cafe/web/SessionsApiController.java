package com.maze.internet_cafe.web;

import com.maze.internet_cafe.session.Session;
import com.maze.internet_cafe.session.SessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionsApiController {

    private final SessionRepository sessionRepository;

    public SessionsApiController(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
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
            result = sessionRepository.findByComputerId(computerId, pageable);
        } else {
            result = sessionRepository.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }
}

