package com.maze.internet_cafe.users;

import com.maze.internet_cafe.authority.Authority;
import com.maze.internet_cafe.authority.AuthorityRepository;
import com.maze.internet_cafe.session.dto.UserCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class ApiUserController {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserCreateDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("username exists");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setName(dto.getName());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setEnabled(true);
        u.setAccountNonExpired(true);
        u.setAccountNonLocked(true);
        u.setCredentialsNonExpired(true);
        List<Authority> authorities = dto.getRoles().stream().map(r -> authorityRepository.findByName(r).orElseGet(() -> {
            Authority a = new Authority();
            a.setName(r);
            return authorityRepository.save(a);
        })).collect(Collectors.toList());
        u.setAuthorities(authorities);
        userRepository.save(u);
        return ResponseEntity.ok(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserCreateDto dto) {
        return userRepository.findById(id).map(u -> {
            if (dto.getName() != null) u.setName(dto.getName());
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            if (dto.getRoles() != null) {
                List<Authority> authorities = dto.getRoles().stream().map(r -> authorityRepository.findByName(r).orElseGet(() -> {
                    Authority a = new Authority();
                    a.setName(r);
                    return authorityRepository.save(a);
                })).collect(Collectors.toList());
                u.setAuthorities(authorities);
            }
            userRepository.save(u);
            return ResponseEntity.ok(u);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return ResponseEntity.ok().build();
    }
}
