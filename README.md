# Internet Cafe API

A minimal Internet Cafe backend for registering computers, starting/stopping user sessions, and generating billing reports.

This README covers how to build, run, authenticate, the main HTTP endpoints, example curl commands, the desktop "Agent" behavior, and troubleshooting.

Highlights
- Register computers and assign them to branches.
- Start and stop computer sessions (track duration and compute price).
- Generate daily billing reports for a date range.
- JWT-based authentication with roles: ADMIN, AGENT, USER.

Requirements
- Java 17+ (or the JDK version your project is configured for in `pom.xml`).
- Maven (the project includes the Maven wrapper `mvnw`).

Quick start (build & run)
1. Build with the included wrapper (Linux/macOS):

```bash
./mvnw -DskipTests clean package
```

2. Run the app:

```bash
./mvnw spring-boot:run
# or run the generated jar
java -jar target/internet-cafe-0.0.1-SNAPSHOT.jar
```

3. Tests

```bash
./mvnw test
```

Configuration
- Application configuration is in `src/main/resources/application.yaml`.
- Test configuration is in `src/test/resources/application-test.properties`.
- You can configure data source, server port, JWT secret and other properties there.

Authentication and roles
- Authenticate with POST /api/authenticate using JSON { "username": "...", "password": "..." }.
- Response contains { "token": "<jwt>" }.
- Send the JWT in requests with the Authorization header: `Authorization: Bearer <token>`.

Built-in roles (used by the API):
- ADMIN: Full access (create branches, computers, view billing reports).
- AGENT: Create/register computers, start/stop sessions on behalf of users.
- USER: Start/stop sessions (if allowed) and view their own session info.

Main API endpoints
(Assumes base path http://localhost:8080)

Authentication
- POST /api/authenticate
  - Request: { "username": "admin", "password": "secret" }
  - Response: { "token": "<jwt>" }

Branch management
- (Admin-only) The code contains a JPA `Branch` entity. Branches are created in tests via repository; you can create a simple controller or use data SQL if you need seed data.

Computer management
- POST /api/v1/computers
  - Roles: ADMIN or AGENT
  - Body example:
    {
      "name": "PC-01",
      "macAddress": "AA:BB:CC:DD:EE:FF",
      "ipAddress": "10.0.0.2",
      "osType": "WINDOWS",
      "branchId": 1
    }
  - Registers a computer assigned to a branch.

Session management
- POST /api/v1/computers/{computerId}/sessions/start
  - Roles: AGENT, ADMIN, USER (depending on your security config)
  - Optional body: { "pricePerHour": 5.00 }
  - Starts a session for the specified computer. If `pricePerHour` is omitted it falls back to the branch price.

- POST /api/v1/computers/{computerId}/sessions/{sessionId}/stop
  - Roles: AGENT, ADMIN, USER
  - Stops the session and calculates total duration and cost.

Billing and reports
- GET /api/v1/billing/reports/daily?from=YYYY-MM-DD&to=YYYY-MM-DD
  - Roles: ADMIN
  - Returns a daily aggregation (sessions, total minutes/hours, amounts) for the requested date range.

Example curl flows
- Authenticate (get token):

```bash
curl -s -X POST http://localhost:8080/api/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"secret"}'
```

- Create a computer (replace <token>):

```bash
curl -s -X POST http://localhost:8080/api/v1/computers \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{"name":"PC-01","macAddress":"AA:BB:CC:DD:EE:FF","ipAddress":"10.0.0.2","osType":"WINDOWS","branchId":1}'
```

- Start a session:

```bash
curl -s -X POST http://localhost:8080/api/v1/computers/1/sessions/start \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{"pricePerHour":5.00}'
```

- Stop a session (replace IDs):

```bash
curl -s -X POST http://localhost:8080/api/v1/computers/1/sessions/1/stop \
  -H "Authorization: Bearer <token>"
```

- Get daily billing report:

```bash
curl -s -X GET "http://localhost:8080/api/v1/billing/reports/daily?from=2025-01-01&to=2025-01-31" \
  -H "Authorization: Bearer <token>"
```

Scripts
- `scripts/curl_examples.sh` is referenced by this README. If it does not exist, create it using the cURL examples above and make it executable:

```bash
chmod +x scripts/curl_examples.sh
./scripts/curl_examples.sh
```

Agent (desktop) behavior
- This project also contains an optional Agent component that can run on a user's desktop (Linux or Windows).
- The Agent is intended to detect screen lock/unlock and automatically stop or restart sessions accordingly. This is helpful for tracking session time when users lock their workstation.

Windows Agent setup
1. Create a folder at `C:\internet-cafe` and copy the compiled JAR there.
2. Use the user's Startup folder (`shell:startup`) and add a small `.bat` file to run `javaw -jar internet-cafe.jar` minimized. Redirect logs to `C:\internet-cafe\agent.log`.
3. The agent tries to detect the `LogonUI.exe` process to detect the lock screen on Windows.

Linux Agent setup (Ubuntu/GNOME)
1. Copy the jar to `/opt/internet-cafe/internet-cafe.jar` (requires root for `/opt`).
2. Install dbus-monitor if needed: `sudo apt install dbus-x11`.
3. Add a Startup Applications entry with a command like:

```bash
/bin/bash -c "java -jar /opt/internet-cafe/internet-cafe.jar > /home/$USER/agent.log 2>&1"
```

Troubleshooting
- If authentication fails, check `application.yaml` for JWT secret and validity window.
- If the application won't start, ensure your Java version matches project's target Java.
- For Agent issues: confirm `dbus-monitor` exists on Linux and that `LogonUI.exe` is available on your Windows version.

Further improvements you might consider (ideas)
- Add endpoints for managing branches (create/update/list).
- Add pagination and filtering to computers and sessions endpoints.
- Add CSV or PDF export for billing reports.
- Add more detailed user audit logs and session history per user.

Contributing
- Pull requests are welcome. Please include unit tests for new behavior and keep changes focused.

License
- (Add your preferred license here)


-- End of README

