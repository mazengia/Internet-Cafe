package com.maze.internet_cafe.security;

public class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static util class");
    }
}
