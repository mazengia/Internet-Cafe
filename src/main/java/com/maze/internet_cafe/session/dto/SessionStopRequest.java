package com.maze.internet_cafe.session.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionStopRequest {
    private LocalDateTime endTime; // optional: server will use now if null
}

