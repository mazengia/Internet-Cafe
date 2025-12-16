package com.maze.internet_cafe.computer;

public enum OSType {
    WINDOWS, LINUX;

    public static OSType fromName(String name) {
        if (name == null) return WINDOWS; // default
        name = name.toLowerCase();
        if (name.contains("win")) return WINDOWS;
        if (name.contains("linux")) return LINUX;
        return WINDOWS; // default if unknown
    }
}
