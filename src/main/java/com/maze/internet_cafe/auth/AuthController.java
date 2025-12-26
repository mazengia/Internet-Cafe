package com.maze.internet_cafe.auth;

import com.maze.internet_cafe.security.AuthenticationRequest;
import com.maze.internet_cafe.security.AuthenticationResponse;
import com.maze.internet_cafe.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest req, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) JwtUtil.JWT_TOKEN_VALIDITY);
            response.addCookie(cookie);
            StringBuilder sb = new StringBuilder();
            sb.append("JWT=").append(token)
              .append("; Path=/; HttpOnly; Max-Age=").append((int) JwtUtil.JWT_TOKEN_VALIDITY)
              .append("; SameSite=Lax");
            response.setHeader("Set-Cookie", sb.toString());

            return ResponseEntity.ok(new AuthenticationResponse(token, userDetails.getUsername()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
