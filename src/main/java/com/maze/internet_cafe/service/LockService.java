package com.maze.internet_cafe.service;

public class LockService {

    public static void lock() {
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec(
                        "rundll32 user32.dll,LockWorkStation"
                );
            } else {
                Runtime.getRuntime().exec("loginctl lock-session");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
