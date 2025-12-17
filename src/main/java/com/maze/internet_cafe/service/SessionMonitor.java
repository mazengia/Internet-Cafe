package com.maze.internet_cafe.service;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class SessionMonitor {

    private final AgentService agentService;

    public SessionMonitor(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostConstruct
    public void init() {
        // Run in a separate thread so it doesn't block the main application
        Thread monitorThread = new Thread(this::listenToUbuntuEvents);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void listenToUbuntuEvents() {
        System.out.println(">>> Initializing Ubuntu D-Bus Event Listener...");

        // Command to watch for GNOME ScreenSaver signals
        String[] cmd = {
                "dbus-monitor",
                "--session",
                "type='signal',interface='org.gnome.ScreenSaver'"
        };

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Ubuntu sends 'boolean true' when Locked
                if (line.contains("boolean true")) {
                    System.out.println(">>> [OS EVENT] Lock detected.");
                    agentService.stopActiveSession();
                    LockService.lock(); // Ensure the local overlay is active
                }
                // Ubuntu sends 'boolean false' when Unlocked
                else if (line.contains("boolean false")) {
                    System.out.println(">>> [OS EVENT] Unlock detected. Restarting session...");
                    agentService.restartSession();
                    // Optional: re-connect WS if it dropped during sleep
                    agentService.connectWebSocket();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> Monitor Error: " + e.getMessage());
            // Fallback to simple polling if dbus-monitor is missing
        }
    }
}