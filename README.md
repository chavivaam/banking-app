# Banking Transaction Application

Full-stack banking portal — Spring Boot 3 backend + Angular 16 frontend.

## Requirements

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (the only thing you need to install)

## Run

```bash
docker-compose up --build
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost           |
| Backend  | http://localhost:8080      |

First startup takes a few minutes while Docker downloads base images and builds the project.

## Default accounts (seeded on first run)

| Username | Password  | Role  |
|----------|-----------|-------|
| admin    | admin123  | ADMIN |
| user     | user123   | USER  |

## What each role can do

**USER** — create transactions, view their own transaction history  
**ADMIN** — view all users' transactions, create new user accounts

## Development (without Docker)

**Backend** — requires Java 19+ and Maven 3.8+:
```bash
cd backend
mvn spring-boot:run -pl app
```
> Also requires MySQL on localhost:3306 with database `banking_db`, user `root`, password `password`.

**Frontend** — requires Node 16+:
```bash
cd frontend
npm install --legacy-peer-deps
npm start          # proxies API calls to localhost:8080
```

## Project structure

```
backend/           Maven multi-module Spring Boot project
  domain/          Entities, DTOs, enums
  repository-api/  Repository interfaces (Spring Data JPA)
  repository-impl/ Repository module (empty — Spring Data auto-implements)
  service-api/     Service interfaces
  service-impl/    Service implementations
  app/             Controllers, security config, entry point

frontend/          Angular 16 standalone SPA
  src/app/
    core/          Interceptors, guards, services, models
    features/      Login, Dashboard (transaction list, create transaction, add user)
```
