package com.maze.internet_cafe.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maze.internet_cafe.exception.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RevokedTokenService revokedTokenService;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService, RevokedTokenService revokedTokenService) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.TOKEN_HEADER);
        // If no Authorization header, check for a cookie named JWT
        if ((header == null || header.isBlank()) && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    header = SecurityConstants.TOKEN_PREFIX + c.getValue();
                    break;
                }
            }
        }

        String username = null;
        String token = null;
        try {
            if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                token = header.substring(SecurityConstants.TOKEN_PREFIX.length());
                // If token is revoked, respond 401
                if (revokedTokenService.isRevoked(token)) {
                    ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, new RuntimeException("Token revoked"));
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(new ObjectMapper().writeValueAsString(apiError));
                    return;
                }
                username = jwtUtil.getUsernameFromToken(token);
            }
            if (username != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, e);
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(apiError));
            return;
        }
        chain.doFilter(request, response);
    }

}
