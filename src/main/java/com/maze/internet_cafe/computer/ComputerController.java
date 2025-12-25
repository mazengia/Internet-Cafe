package com.maze.internet_cafe.computer;

import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
import com.maze.internet_cafe.computer.dto.ComputerDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/computers")
public class ComputerController {

    private final ComputerService computerService;

    public ComputerController(ComputerService computerService) {
        this.computerService = computerService;
    }

    //    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    @PostMapping
    public ResponseEntity<ComputerDto> create(@Valid @RequestBody ComputerCreateDto dto) {
        ComputerDto created = computerService.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<ComputerDto>> list(
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComputerDto> result = computerService.list(branchId, pageable);
        return ResponseEntity.ok(result);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ComputerDto> get(@PathVariable Long id) {
        System.out.println("bbbb=");
        Computer c = computerService.findById(id);
        return ResponseEntity.ok(computerService.toDto(c));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> handleHeartbeat(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        computerService.handleHeartbeat(name);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}/lock")
    public void lock(@PathVariable Long id) {
        computerService.lockComputer(id);
    }
}
