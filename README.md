# Banking Transaction Application

Full-stack banking portal — Spring Boot 3 (Java) backend + Angular 16 frontend.

---

## How to run

There are two ways to run this project. Choose the one that fits your setup.

---

### Option 1 — Docker (recommended, MySQL, production-like)

**What you need:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

**Steps:**
```bash
git clone <repo-url>
cd <cloned-folder-name>
docker compose up --build
```

> **Note:** the folder name after `cd` matches whatever the repository is named on GitHub (e.g. `Banking-App`).  
> If `docker compose` isn't recognised, try the older spelling: `docker-compose up --build`.

Docker will:
1. Pull MySQL 8 and wait for it to be healthy
2. Build and start the Spring Boot backend (auto-creates the schema and seeds default accounts)
3. Build the Angular app and serve it via nginx

| | URL |
|---|---|
| App | http://localhost |
| Backend API | http://localhost:8080 |

> First run takes **5–10 minutes** — Docker downloads base images and builds everything from source.  
> Subsequent runs start in under a minute (layers are cached).  
> Data persists in a Docker volume (`mysql_data`) between restarts.

---

### Option 2 — Local dev with H2 (no Docker, no MySQL)

Use this if Docker is unavailable (e.g. virtualisation disabled on a corporate machine).  
H2 is a lightweight embedded database — no installation required.  
Data is stored in `backend/data/banking_db.mv.db` and persists between restarts.

**What you need:**

| Tool | Version | Download |
|---|---|---|
| Java JDK | 19 or later | https://adoptium.net |
| Maven | 3.8 or later | https://maven.apache.org/download.cgi |
| Node.js | 16 or later | https://nodejs.org |

> Check versions: `java -version` · `mvn -version` · `node -version`

**Step 1 — Build the backend** (once, or after any code change)

```bash
cd "Banking App/backend"
mvn install -DskipTests
```

**Step 2 — Run the backend**

```bash
java -jar app/target/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

> On Windows, use the full JDK path if `java` is not on PATH:  
> `"C:\Users\<you>\.jdks\openjdk-19.0.1\bin\java.exe" -jar app/target/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`

Wait for:
```
Started BankingApplication in X.XXX seconds
```

**Step 3 — Start the frontend** (new terminal)

```bash
cd "Banking App/frontend"
npm install --legacy-peer-deps
npm start
```

Wait for:
```
** Angular Live Development Server is listening on localhost:4200
```

Open **http://localhost:4200** in your browser.

> The frontend proxies all API calls to `localhost:8080` automatically — no CORS configuration needed.

---

## Default accounts

Seeded automatically on first run:

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| user | user123 | USER |

## Role permissions

| Feature | USER | ADMIN |
|---|---|---|
| View own transactions | ✓ | — |
| Create a transaction | ✓ | — |
| View all users' transactions | — | ✓ |
| Create new users | — | ✓ |

---

## Project structure

```
Banking App/
├── docker-compose.yml          # Orchestrates MySQL + backend + frontend
├── backend/                    # Spring Boot — Maven multi-module
│   ├── Dockerfile
│   ├── domain/                 # Entities, DTOs, enums
│   ├── repository-api/         # Repository interfaces (Spring Data JPA)
│   ├── repository-impl/        # Repository module (Spring Data auto-implements)
│   ├── service-api/            # Service interfaces
│   ├── service-impl/           # Service implementations
│   └── app/                    # Controllers, security, entry point
│       └── src/main/resources/
│           ├── application.properties        # MySQL (used by Docker)
│           └── application-dev.properties    # H2 (used for local dev)
└── frontend/                   # Angular 16 standalone SPA
    ├── Dockerfile
    ├── nginx.conf              # SPA routing + API proxy (Docker)
    ├── proxy.conf.json         # API proxy for local dev server
    └── src/app/
        ├── core/               # Interceptors, guards, services, models
        └── features/           # Login, Dashboard, TransactionList, AddUser
```

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.3, Spring Security 6, Spring Data JPA, Hibernate |
| Database | MySQL 8 (Docker) / H2 file-based (local dev) |
| Frontend | Angular 16, Angular Material, Reactive Forms, Signals |
| Auth | Session-based (Spring Security form login + CSRF) |
| Container | Docker Compose, nginx |
