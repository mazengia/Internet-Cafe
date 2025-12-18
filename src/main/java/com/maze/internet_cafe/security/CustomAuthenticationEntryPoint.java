package com.maze.internet_cafe.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable AuthenticationException authException) throws IOException {
        if (response == null) return; // nothing we can do

        String accept = request != null ? request.getHeader("Accept") : null;
        String xrw = request != null ? request.getHeader("X-Requested-With") : null;

        // If browser likely requesting HTML (Accept contains text/html) and not an AJAX call, redirect to /login
        boolean wantsHtml = accept != null && accept.contains("text/html");
        boolean isAjax = xrw != null && ("XMLHttpRequest".equalsIgnoreCase(xrw));

        if (wantsHtml && !isAjax) {
            response.sendRedirect("/login");
            return;
        }

        // Otherwise return JSON 401
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String message = authException != null ? authException.getMessage() : "Unauthorized";
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of("error", message, "status", HttpStatus.UNAUTHORIZED.value())));
    }
}
