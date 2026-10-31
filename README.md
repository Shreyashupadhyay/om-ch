# StrangerChat - Omegle-Style 1-to-1 Random Chat Platform

StrangerChat is a modern, production-grade anonymous 1-to-1 random text chat platform. It pairs online users in real-time for spontaneous conversations with zero signup required (Guest mode) or allows users to register to save chat histories and manage blocks.

---

## Architecture Overview

```
                      ┌───────────────────────────────────────┐
                      │          React 19 + TypeScript        │
                      │       Tailwind CSS + Vite App         │
                      └──────────────┬─────────────────┬──────┘
                                     │                 │
                           REST API  │                 │ WebSocket (STOMP/SockJS)
                        /api/auth/** │                 │ /ws
                        /api/conv/** │                 │ /app/queue.join, chat.send
                                     ▼                 ▼
                      ┌───────────────────────────────────────┐
                      │       Spring Boot 3.3.5 Backend       │
                      │  - JWT Auth (Guests & Registered)     │
                      │  - Thread-Safe Matchmaking Engine     │
                      │  - STOMP User Destinations            │
                      │  - Content Filter & Rate Limiter      │
                      └──────────────────┬────────────────────┘
                                         │
                                         │ JPA / Flyway Migrations
                                         ▼
                      ┌───────────────────────────────────────┐
                      │             PostgreSQL 16+            │
                      │ users, conversations, messages,       │
                      │ reports, blocks                       │
                      └───────────────────────────────────────┘
```

- **Backend**: Java 21+, Spring Boot 3.3.5 (Web, WebSocket/STOMP, Security, Data JPA, Validation), Flyway, JJWT.
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS, `@stomp/stompjs`, `sockjs-client`, `lucide-react`.
- **Database**: PostgreSQL with Flyway schema versioning.
- **Matchmaking Engine**: Interface-based thread-safe in-memory queue with self-match avoidance, rematch backoff, and user block enforcement. Swappable for Redis in distributed setups.
- **Safety & Moderation**: 18+ age gate, reporting system (`reports` table), persistent blocking (`blocks` table), HTML/XSS escaping, and profanity filtering.

---

## Directory Layout

```
├── /backend            # Spring Boot Maven application
│   ├── src/main/java   # Java source code (controllers, services, entities, config)
│   ├── src/main/resources # application.yml & Flyway migrations (db/migration)
│   └── pom.xml         # Maven project descriptor
├── /frontend           # React TypeScript Vite application
│   ├── src/components  # Navbar, modals, chat controls
│   ├── src/context     # AuthContext (Guest & User token persistence)
│   ├── src/pages       # Landing, Chat, Login, Register, History
│   └── src/services    # REST API client & useChatSocket STOMP hook
├── docker-compose.yml  # Docker Compose for PostgreSQL & pgAdmin
├── .env.example        # Environment variables reference
└── README.md           # Documentation and setup guide
```

---

## Quickstart Guide

### 1. Database Setup

#### Option A: Docker Compose (Recommended if Docker is running)
```bash
docker compose up -d
```
PostgreSQL will be available at `localhost:5432` with user `postgres` and password `1234`. pgAdmin is accessible at `http://localhost:5050` (`admin@strangerchat.local` / `admin123`).

#### Option B: Local PostgreSQL Service
If PostgreSQL is already installed locally, create the database:
```sql
CREATE DATABASE strangerchat;
```
Ensure your credentials match `.env.example` or update `application.yml`. Flyway will automatically run all migrations on startup.

---

### 2. Run the Backend

```bash
cd backend
.\mvnw.cmd spring-boot:run
```
The server will start on `http://localhost:8080`.
- **Swagger / OpenAPI Documentation**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Schema**: `http://localhost:8080/v3/api-docs`

---

### 3. Run the Frontend

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```
Open your browser at `http://localhost:5173`.

---

## API & WebSocket Contract

### REST Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new account | No |
| `POST` | `/api/auth/login` | Login existing user | No |
| `POST` | `/api/auth/guest` | Issue ephemeral guest token (`Guest-XXXX`) | No |
| `GET` | `/api/users/me` | Current user profile | Bearer JWT |
| `GET` | `/api/conversations` | List saved conversations (paginated) | Registered User |
| `GET` | `/api/conversations/{id}` | Full conversation transcript | Registered User |
| `DELETE` | `/api/conversations/{id}` | Delete conversation from history | Registered User |
| `POST` | `/api/reports` | Submit abuse/harassment report | Bearer JWT |
| `POST` | `/api/blocks` | Block chat partner from rematching | Registered User |
| `GET` | `/api/stats/online` | Real-time online user count & active rooms | No |

### WebSocket (STOMP over SockJS)

- **Connect URL**: `/ws` (or raw WebSocket `/ws`)
- **STOMP Connect Header**: `Authorization: Bearer <JWT>`

#### Client Inbound Destinations (`/app`)
- `/app/queue.join`: Join random matchmaking queue.
- `/app/queue.leave`: Cancel queue wait.
- `/app/chat.send`: Send message payload `{"roomId": "...", "content": "..."}`.
- `/app/chat.typing`: Typing indicator `{"roomId": "...", "isTyping": true/false}`.
- `/app/chat.next`: End current room and immediately queue for next stranger.
- `/app/chat.leave`: Stop/leave current conversation.

#### Server Outbound User Destinations (`/user`)
- `/user/queue/messages`: Receives new chat messages:
  ```json
  {
    "id": "uuid",
    "roomId": "uuid",
    "senderLabel": "You | Stranger",
    "fromMe": true,
    "content": "Hello!",
    "timestamp": "2026-10-30T02:00:00Z"
  }
  ```
- `/user/queue/events`: Receives room lifecycle and partner events:
  ```json
  {
    "event": "MATCHED | WAITING_IN_QUEUE | PARTNER_TYPING | PARTNER_STOPPED_TYPING | PARTNER_LEFT | ROOM_ENDED | ERROR",
    "roomId": "uuid",
    "message": "Connected with a stranger. Say hi!",
    "payload": { "roomId": "...", "conversationId": "..." }
  }
  ```

---

## Verification & Testing

### Running Automated Tests
```bash
cd backend
.\mvnw.cmd test
```
Runs unit tests for `AuthService`, `InMemoryMatchmakingService`, `ProfanityFilterService`, and Spring context integration tests.

---

## Deployment Guide: Render & Vercel

StrangerChat is pre-configured for instant zero-configuration deployment:
- **Backend & PostgreSQL**: Hosted on [Render](https://render.com) using Docker and Managed PostgreSQL.
- **Frontend**: Hosted on [Vercel](https://vercel.com) using Vite SPA configuration.

---

### Step 1: Deploy Backend & Database to Render

#### Option A: One-Click Infrastructure as Code (Blueprint) - Recommended
1. Push your repository to GitHub or GitLab:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/<your-username>/strangerchat.git
   git push -u origin main
   ```
2. Log in to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** > **Blueprint**.
4. Connect your GitHub repository.
5. Render detects `render.yaml` automatically and creates:
   - **`strangerchat-db`**: PostgreSQL database.
   - **`strangerchat-backend`**: Docker Web Service linked to the database, with automated port binding, health check (`/api/health`), and a generated JWT secret.
6. Click **Apply**. Once deployed, copy your backend URL:
   `https://strangerchat-backend.onrender.com`

#### Option B: Manual Setup on Render
1. **Create PostgreSQL Database**:
   - In Render, click **New +** > **PostgreSQL**.
   - Name: `strangerchat-db`, Database: `strangerchat`, User: `postgres`.
   - Copy the **Internal Database URL** (or External Database URL).
2. **Create Web Service**:
   - Click **New +** > **Web Service** and connect your repository.
   - **Runtime**: `Docker`.
   - **Root Directory**: `backend` (or leave blank if pointing Dockerfile path to `backend/Dockerfile`).
   - **Dockerfile Path**: `Dockerfile` (or `backend/Dockerfile`).
   - **Health Check Path**: `/api/health`.
   - **Environment Variables**:
     - `PORT`: `8080`
     - `DATABASE_URL`: Paste your Render Postgres connection string (starts with `postgres://` or `postgresql://` — backend will parse it automatically).
     - `JWT_SECRET`: Random 64-character hex string (e.g., `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`).
     - `CORS_ALLOWED_ORIGINS`: `*` (or your Vercel frontend URL, e.g., `https://strangerchat.vercel.app`).

---

### Step 2: Deploy Frontend to Vercel

1. Log in to [Vercel](https://vercel.com).
2. Click **Add New...** > **Project** and import your repository.
3. Configure Project Settings:
   - **Framework Preset**: `Vite`
   - **Root Directory**: Click *Edit* and select `frontend` (or leave default if using root `vercel.json`).
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. Expand **Environment Variables** and add:
   - **Key**: `VITE_API_URL`
   - **Value**: Your Render backend URL (e.g. `https://strangerchat-backend.onrender.com` without trailing slash).
5. Click **Deploy**.
6. When Vercel finishes, your frontend is live with full Single-Page Application (SPA) routing, REST API communication, and WebSocket real-time chat!

---

## Environment Variables Reference

### Backend (Render)

| Variable | Required | Default | Description |
|---|---|---|---|
| `PORT` | No | `8080` | Port for the HTTP/WebSocket server (Render sets this dynamically). |
| `DATABASE_URL` | Yes (in Cloud) | - | Full connection URL (`postgres://...` or `postgresql://...`). |
| `DB_HOST` | No | `localhost` | Fallback if `DATABASE_URL` is omitted. |
| `DB_PORT` | No | `5432` | PostgreSQL port. |
| `DB_NAME` | No | `strangerchat` | Database name. |
| `DB_USER` | No | `postgres` | Database username. |
| `DB_PASS` | No | `1234` | Database password. |
| `JWT_SECRET` | Recommended | default dev key | Secret key for signing guest and user JWTs. |
| `CORS_ALLOWED_ORIGINS`| No | `*` | Comma-separated allowed origins or wildcard. |

### Frontend (Vercel)

| Variable | Required | Default | Description |
|---|---|---|---|
| `VITE_API_URL` | Yes (in Prod) | empty (uses Vite proxy) | URL of the backend web service (e.g. `https://strangerchat-backend.onrender.com`). |

---

## Suggested Next Steps
1. **Redis Integration**: Swap `InMemoryMatchmakingService` for Redis Pub/Sub + Sorted Sets for multi-node horizontally scaled deployments.
2. **WebRTC Video Chat**: Add peer-to-peer video streaming with STUN/TURN signaling over the existing STOMP broker.
3. **Interest-Based Matching**: Allow users to specify tags/interests (e.g. `coding`, `music`, `gaming`) to match users with shared interests first.
4. **Admin Moderation Dashboard**: Build an internal portal to review submitted reports, ban malicious IPs, and view platform metrics.
