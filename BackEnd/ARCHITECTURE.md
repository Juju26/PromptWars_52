# PromptWars Backend — Architecture Reference

## Module Map

| Module | Port | Database | Description |
|---|---|---|---|
| `common-lib` | — | — | Shared JWT, exceptions, DTOs |
| `user-service` | 8081 | `promptwars_users` | Registration, login, JWT issuance |
| `task-service` | 8082 | `promptwars_tasks` | Kanban task board |
| `messaging-service` | 8083 | `promptwars_messaging` | Channels, messages, Pub/Sub |
| `beacon-service` | 8084 | `promptwars_beacon` | P2P IP registry, TTL eviction |

---

## Key Architectural Decisions

### 1. Stateless JWT (common-lib)
- Every service shares `JwtTokenProvider` and `JwtAuthenticationFilter` from `common-lib`.
- No session state — Cloud Run scales to zero safely.
- Token structure: `sub=userId`, `role=ROLE_*`, `email`, `username`.
- Access token: **15 min**. Refresh token: **7 days**.

### 2. Database Per Service
- Each microservice owns its own Postgres database — no cross-DB joins.
- Cross-service references (e.g. `assigneeId` in tasks) are **soft UUID references**, not FK constraints.
- **Flyway** manages all schema migrations. Hibernate is set to `validate` only.

### 3. Beacon Registry (P2P Fallback)
- Daemons POST to `/api/v1/beacon/register` with their LAN IP + port on startup.
- A `@Scheduled` eviction job sweeps stale rows every 2 minutes.
- Results are cached in-memory (Spring Cache) to handle 500-user burst discovery.
- TTL configurable via `BEACON_TTL_MINUTES` env var (default: 10 min).

### 4. Pub/Sub Event Bus (Messaging Service)
- New messages → `hackathon-notifications` topic (async, non-blocking).
- Organiser announcements → `hackathon-announcements` topic.
- Subscription listener uses **manual ACK mode** — messages are only acknowledged after successful processing.
- Local dev: uses GCP Pub/Sub emulator via `PUBSUB_EMULATOR_HOST`.

### 5. Cursor Pagination
- Message history uses **cursor-based pagination** (`WHERE created_at < :before`) — not OFFSET.
- This is critical: OFFSET degrades to O(n) on large channels.

### 6. HikariCP Tuning
- Pool sizes tuned per service workload: beacon (10), task (15), messaging (15).
- `open-in-view: false` prevents connection leaks from lazy-loading outside transactions.

---

## Local Development

### Prerequisites
- Docker Desktop
- Java 17
- Maven 3.9+

### Start everything

```bash
# 1. Create your env file
cp .env.example .env
# Edit .env and set JWT_SECRET (generate: openssl rand -base64 64)

# 2. Start all services
docker compose up -d

# 3. Check health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

### Swagger UIs (local)
- User Service: http://localhost:8081/swagger-ui.html
- Task Service: http://localhost:8082/swagger-ui.html
- Messaging:    http://localhost:8083/swagger-ui.html
- Beacon:       http://localhost:8084/swagger-ui.html

### Build individual service
```bash
mvn package -pl common-lib,user-service -am -DskipTests
```

### Build all
```bash
mvn package -DskipTests
```

---

## API Endpoints Summary

### User Service (`/api/v1/auth`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Register participant |
| POST | `/login` | Public | Login → JWT pair |
| POST | `/refresh` | Public | Refresh access token |

### Task Service (`/api/v1/tasks`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/` | JWT | Create task |
| GET | `/board/{boardId}` | JWT | Board tasks (ordered) |
| GET | `/board/{boardId}/summary` | JWT | Status count summary |
| GET | `/{taskId}` | JWT | Get single task |
| GET | `/my` | JWT | My assigned tasks |
| PUT | `/{taskId}` | JWT | Update task |
| PATCH | `/{taskId}/move` | JWT | Move to column |
| DELETE | `/{taskId}` | JWT | Delete task |

### Messaging Service (`/api/v1/messaging`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/channels` | JWT | Create channel |
| GET | `/channels/team/{teamId}` | JWT | Team channels |
| GET | `/channels/public` | JWT | Public channels |
| POST | `/channels/{id}/messages` | JWT | Send message |
| GET | `/channels/{id}/messages?before=&limit=` | JWT | Get messages (cursor) |
| DELETE | `/messages/{messageId}` | JWT | Soft-delete message |
| POST | `/announcements` | ORGANIZER/ADMIN | Broadcast announcement |

### Beacon Service (`/api/v1/beacon`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | JWT | Register LAN IP |
| POST | `/heartbeat` | JWT | Refresh lastSeen |
| GET | `/peers` | JWT | All active peers |
| GET | `/peers/team/{teamId}` | JWT | Team peers |
| DELETE | `/deregister` | JWT | Deregister on shutdown |

---

## Production Deployment (GCP Cloud Run)

Each service is deployed as a separate Cloud Run service with:
- `--set-secrets JWT_SECRET=jwt-secret:latest` (Secret Manager)
- `--add-cloudsql-instances` for Cloud SQL connection
- `--min-instances 1` on user/task services to avoid cold-start latency
- `--concurrency 80` (Cloud Run default; tune per profiling)

```bash
gcloud run deploy user-service \
  --image gcr.io/YOUR_PROJECT/user-service:latest \
  --region us-central1 \
  --platform managed \
  --set-env-vars GCP_PROJECT_ID=YOUR_PROJECT \
  --set-secrets JWT_SECRET=jwt-secret:latest,DB_URL=db-url:latest,DB_PASS=db-pass:latest \
  --min-instances 1 \
  --allow-unauthenticated
```
