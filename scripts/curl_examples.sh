#!/usr/bin/env bash
# Example curl commands for Internet Cafe API (assumes server at http://localhost:8080)

# 1) Authenticate and get JWT
# Replace username/password accordingly
curl -s -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.token'

# 2) Create a computer (ADMIN or AGENT) — set BRANCH_ID and TOKEN
# TOKEN=$(curl ...)
# BRANCH_ID=1
# curl -X POST http://localhost:8080/api/v1/computers -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"PC-01","macAddress":"AA:BB:CC:DD:EE:FF","ipAddress":"10.0.0.10","branchId":1}'

# 3) Start a session (optional pricePerHour; uses branch default if omitted)
# curl -X POST http://localhost:8080/api/v1/computers/$COMPUTER_ID/sessions/start -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"pricePerHour":5.00}'

# 4) Stop a session
# curl -X POST http://localhost:8080/api/v1/computers/$COMPUTER_ID/sessions/$SESSION_ID/stop -H "Authorization: Bearer $TOKEN"

# 5) Daily billing report (ADMIN only)
# curl -X GET "http://localhost:8080/api/v1/billing/reports/daily?from=2025-01-01&to=2025-01-31" -H "Authorization: Bearer $TOKEN"

# Note: jq is used to parse JSON in examples. Install jq to run these easily.

