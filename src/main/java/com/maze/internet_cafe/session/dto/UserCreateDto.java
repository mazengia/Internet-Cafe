package com.maze.internet_cafe.session.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserCreateDto {
    private String username;
    private String name;
    private String password;
    private List<String> roles; // e.g. ["ROLE_ADMIN","ROLE_USER"]
}

