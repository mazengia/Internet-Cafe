# Internet Cafe API

Quick usage and example curl commands (assumes app running on http://localhost:8080)

1) Authenticate (get JWT)

POST /api/authenticate
{
  "username": "admin",
  "password": "secret"
}

Response: { "token": "<jwt>" }

2) Create a branch (admin)
- The application includes a JPA Branch entity. You can create it via repository or expose an endpoint. For tests, branch is created via repo.

3) Register a computer (ADMIN or AGENT)

POST /api/v1/computers
Headers: Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "PC-01",
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "ipAddress": "10.0.0.2",
  "osType": "WINDOWS",
  "branchId": 1
}

4) Start session (AGENT / ADMIN / USER)

POST /api/v1/computers/{computerId}/sessions/start
Headers: Authorization: Bearer <token>
Content-Type: application/json

{
  "pricePerHour": 5.00 // optional; will fall back to branch pricePerHour if omitted
}

5) Stop session

POST /api/v1/computers/{computerId}/sessions/{sessionId}/stop
Headers: Authorization: Bearer <token>

6) Daily billing report (ADMIN only)

GET /api/v1/billing/reports/daily?from=2025-01-01&to=2025-01-31
Headers: Authorization: Bearer <token>


## Curl examples

You can find ready-to-run curl examples in `scripts/curl_examples.sh` — make it executable and run or copy the snippets.

Make executable:

```bash
chmod +x scripts/curl_examples.sh
```

Run (edit credentials/ids as needed):

```bash
./scripts/curl_examples.sh
```
# Internet-Cafe
# Internet-Cafe

###### deployments
 

## 7. Windows Agent Setup

To ensure the Agent starts automatically, tracks screen locks (**Win+L**), and restarts sessions on sign-in on Windows:

### Deployment Location

1. Create a folder at `C:\internet-cafe`.
2. Copy your compiled JAR to this folder:
```cmd
move C:\Users\YourUser\Downloads\internet-cafe-0.0.1-SNAPSHOT.jar C:\internet-cafe\internet-cafe.jar

```



### Configuration (Startup Folder)

This method ensures the Agent runs in the user's graphical session to detect lock/unlock events.

1. Press `Win + R`, type `shell:startup`, and press **Enter**.
2. In the folder that opens, create a new file named `internet-cafe-agent.bat`.
3. Right-click the file, select **Edit**, and paste the following:
```batch
@echo off
start /min javaw -jar "C:\internet-cafe\internet-cafe.jar" > "C:\internet-cafe\agent.log" 2>&1

```


*(Note: `javaw` is used to run the app without keeping a command prompt window open).*

### Viewing Agent Logs

Windows does not have `tail` by default. To monitor the Agent's behavior:

* **Option A (PowerShell):** Open PowerShell and run:
```powershell
Get-Content C:\internet-cafe\agent.log -Wait

```


* **Option B (Notepad):** Simply open `C:\internet-cafe\agent.log` in any text editor.

---

### Key Differences for Windows

* **Detection:** The `SessionMonitor` uses `tasklist` to check for `LogonUI.exe` (the Windows lock screen process) instead of D-Bus.
* **Execution:** We use `javaw` instead of `java` to prevent a black console window from staying on the taskbar all day.
* **Paths:** Uses backslashes (`\`) and drive letters (`C:`) instead of the Linux root structure.

### Troubleshooting

If the session doesn't restart on Windows:

1. Ensure the `java` or `javaw` command is in your system **PATH**.
2. Verify that `LogonUI.exe` is the correct process for your version of Windows (standard for Win 10/11) by checking Task Manager while the "Switch User" screen is active.
 
## 8. Linux Agent Setup (Ubuntu/GNOME)

To ensure the Agent starts automatically, tracks screen locks (Win+L), and restarts sessions on sign-in, follow these steps:

### Deployment Location

1. Copy your compiled JAR to `/opt/internet-cafe/internet-cafe.jar`.
### like   `sudo mv /home/mtesfa/Downloads/internet-cafe/target/internet-cafe-0.0.1-SNAPSHOT.jar /opt/internet-cafe/internet-cafe.jar`
2. Ensure you have the `dbus-monitor` package installed: `sudo apt install dbus-x11`.

### Configuration (Startup Applications)

This is the recommended way to run the Agent so it can listen to your User Session D-Bus.

1. Open **Startup Applications** from the Ubuntu menu.
2. Click **Add**.
3. Fill in the following:
* **Name:** `Internet Cafe Agent`
* **Command:** ```bash
  /bin/bash -c "java -jar /opt/internet-cafe/internet-cafe.jar > /home/$USER/agent.log 2>&1"
```

```


* **Comment:** `Manages sessions and screen locking.`



### Viewing Agent Logs

To monitor the Agent's behavior (registration, lock/unlock events, and heartbeats) in real-time, use:

```bash
tail -f ~/agent.log

```
 
### Next Step

The README is now complete. Would you like me to help you create that **`scripts/curl_examples.sh`** file with the exact commands needed to test the whole flow from the command line?