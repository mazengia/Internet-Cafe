package com.maze.internet_cafe.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SessionMonitor {

    private final AgentService agentService;

    private final String os = System.getProperty("os.name").toLowerCase();

    @PostConstruct
    public void init() {
        System.out.println(">>> SessionMonitor initialized (" + os + ")");

        if (isWindows()) {
            startWindowsListener();
        } else if (isLinux()) {
            startLinuxListener();
        } else {
            System.out.println(">>> OS lock detection not supported");
        }
    }

    /* ===================== WINDOWS ===================== */

    private void startWindowsListener() {
        Thread t = new Thread(this::listenWindowsEvents, "windows-session-monitor");
        t.setDaemon(true);
        t.start();
    }

    private void listenWindowsEvents() {
        System.out.println(">>> Listening for Windows lock/unlock events");

        String psScript = """
            Register-WmiEvent -Class Win32_SessionChangeEvent -Action {
                $type = $Event.SourceEventArgs.NewEvent.SessionChangeType
                if ($type -eq 7) { Write-Output 'LOCK' }
                if ($type -eq 8) { Write-Output 'UNLOCK' }
            }
            while ($true) { Wait-Event }
            """;

        try {
            Process process = new ProcessBuilder(
                    "powershell",
                    "-NoLogo",
                    "-NoProfile",
                    "-ExecutionPolicy", "Bypass",
                    "-Command", psScript
            ).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                switch (line.trim()) {
                    case "LOCK" -> handleLock("WINDOWS");
                    case "UNLOCK" -> handleUnlock("WINDOWS");
                }
            }
        } catch (Exception e) {
            System.err.println(">>> Windows session monitor failed");
            e.printStackTrace();
        }
    }

    /* ===================== LINUX (GNOME) ===================== */

    private void startLinuxListener() {
        Thread t = new Thread(this::listenLinuxEvents, "linux-session-monitor");
        t.setDaemon(true);
        t.start();
    }

    private void listenLinuxEvents() {
        System.out.println(">>> Listening for GNOME lock/unlock events");

        try {
            Process process = new ProcessBuilder(
                    "dbus-monitor",
                    "--session",
                    "type='signal',interface='org.gnome.ScreenSaver'"
            ).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("boolean true")) {
                    handleLock("LINUX");
                }
                if (line.contains("boolean false")) {
                    handleUnlock("LINUX");
                }
            }
        } catch (Exception e) {
            System.err.println(">>> Linux session monitor failed");
            e.printStackTrace();
        }
    }

    /* ===================== HANDLERS ===================== */

    private void handleLock(String source) {
        System.out.println(">>> " + source + " SESSION LOCKED");
        agentService.stopActiveSession();
        LockService.lock();
    }

    private void handleUnlock(String source) {
        System.out.println(">>> " + source + " SESSION UNLOCKED");
        agentService.restartSession();
        agentService.connectWebSocket();
    }

    /* ===================== OS CHECK ===================== */

    private boolean isLinux() {
        return os.contains("linux");
    }

    private boolean isWindows() {
        return os.contains("win");
    }
}
