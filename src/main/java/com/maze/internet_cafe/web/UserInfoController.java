package com.maze.internet_cafe.web;

import com.maze.internet_cafe.security.JwtUtil;
import com.maze.internet_cafe.security.RevokedTokenService;
import com.maze.internet_cafe.security.UserDetailsImp;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class UserInfoController {

    private final JwtUtil jwtUtil;
    private final RevokedTokenService revokedTokenService;

    public UserInfoController(JwtUtil jwtUtil, RevokedTokenService revokedTokenService) {
        this.jwtUtil = jwtUtil;
        this.revokedTokenService = revokedTokenService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        UserDetailsImp user = (UserDetailsImp) authentication.getPrincipal();
        return ResponseEntity.ok(
                Map.of(
                        "username", authentication.getName(),
                        "authorities", authentication.getAuthorities(),
                        "name", Objects.requireNonNull(user).getName()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Try to extract token from Authorization header
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // If no header token, check cookie
        if (token == null && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            long exp = jwtUtil.getExpirationEpochMillis(token);
            if (exp > 0) revokedTokenService.revokeToken(token, exp);
        }

        // Clear the JWT cookie so browser-based sessions are logged out
        Cookie cookie = new Cookie("JWT", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // expire immediately
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }
}
