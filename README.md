# Hermes Dashboard

A simple mobile-friendly dashboard to chat with your local Hermes agent from anywhere,
and see the services (skills) it offers.

- Frontend: React + Vite + Tailwind (port 5173)
- Backend: Spring Boot 3.3 / Java 21 (port 8080)
- Brain: the local Hermes agent (`hermes chat -q`), run through `backend/hermes-wrapper.sh`
- Auth: single-user login -> JWT (all /api/** except /api/auth/login require a Bearer token)
- Storage: in-memory H2 (chat history survives restarts of the app process only while running)

## Run

Backend (needs the local Hermes install):

```bash
cd backend
HERMES_DASH_HERMES_CMD=$PWD/hermes-wrapper.sh \
HERMES_DASH_HOST_USER=Akash HERMES_DASH_HOST_PASSWORD=yourpass \
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 from any device (vite listens on all interfaces; use the
machine's LAN IP or expose it via a tunnel/domain later — it binds `--host` by default).

## Configuration (env vars)

| Variable | Default | Purpose |
|---|---|---|
| HERMES_DASH_USER / HERMES_DASH_PASSWORD | admin / changeme | dashboard login |
| HERMES_DASH_JWT_SECRET | dev secret | JWT signing key — CHANGE in production |
| HERMES_DASH_JWT_TTL | 720 | token lifetime (minutes) |
| HERMES_DASH_HERMES_CMD | hermes | command that runs the Hermes CLI |
| HERMES_DASH_HERMES_TIMEOUT | 300 | per-message timeout (seconds) |
| HERMES_HOME | ~/.hermes | Hermes home (skills/ discovery) |

## Accounts & approval

- The **host** account is seeded on first boot: username `Akash`, password `Free@2006`
  (override with `HERMES_DASH_HOST_USER` / `HERMES_DASH_HOST_PASSWORD`).
- Anyone can **create an account** from the login screen ("Need an account? Create one").
  New accounts start **pending** and cannot log in.
- The host sees an **Account approvals** panel in the dashboard sidebar and can approve or
  reject each pending account. Only approved accounts can sign in.

## API

- POST /api/auth/login {username, password} -> {token, username, host}
- POST /api/auth/register {username, password} -> account created (pending approval)
- GET /api/auth/pending -> pending accounts (host only)
- POST /api/auth/decide {username, approve} -> approve/reject (host only)
- POST /api/chat/send {message} -> {role, content, createdAt} (runs the real agent; can take a while)
- GET /api/chat/history -> last 100 messages
- GET /api/services -> installed skills [{name, description, source}]
- GET /api/services/status -> {hermesHomeFound, skillCount}

## Tests

```bash
cd backend && mvn test        # 29 tests: unit + controller + full HTTP end-to-end
cd frontend && npm test       # 13 tests: vitest + testing-library, api layer mocked
```

## Notes on `hermes serve`

Hermes also ships `hermes serve` — the headless JSON-RPC/WebSocket backend the desktop app
uses (port 9119). It would allow streaming and persistent sessions, but requires implementing
its session/prompt event protocol and auth gate; the current `hermes chat -q` spawn is the
simpler one-shot path and is what this dashboard uses today.
