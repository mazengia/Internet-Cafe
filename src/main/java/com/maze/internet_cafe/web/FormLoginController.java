package com.maze.internet_cafe.web;

import com.maze.internet_cafe.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FormLoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login/process")
    public String process(String username, String password, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            // Cookie lifetime = match token lifetime (approx 5 hours set in JwtUtil)
            cookie.setMaxAge(60 * 60 * 6);
            response.addCookie(cookie);
            return "redirect:/dashboard";
        } catch (AuthenticationException e) {
            return "redirect:/login?error";
        }
    }
}

