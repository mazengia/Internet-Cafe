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
