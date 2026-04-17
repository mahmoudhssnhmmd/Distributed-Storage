# Distributed Storage API (Spring Boot)

Hi, I am Mahmoud.

This project is my backend file-storage API built with Spring Boot. I wanted it to be practical, not just a demo, so it includes authentication, file operations, tests, Docker setup, and a distributed-ready runtime using Redis + Nginx.

## What This Project Does

- JWT authentication (`register` / `login`)
- File upload, list (with pagination), rename, download, and delete
- Spring Security protection for private routes
- Swagger OpenAPI docs with Bearer token support
- Centralized error handling and validation
- Integration tests for auth and file flow

## Tech Stack

- Java 17+
- Spring Boot 3.5.x
- Spring Security + JWT (`jjwt`)
- Spring Data JPA + PostgreSQL
- Redis cache
- Nginx (reverse proxy / load balancer)
- H2 for tests
- Maven

## Quick Start

### 1) Run locally

```zsh
cd springbootProject
# export JAVA_HOME=/path/to/your/jdk
# export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run
```

### 2) Or use Makefile shortcuts

```zsh
cd springbootProject
make help
make test
make run
```

### 3) Run full Docker stack

```zsh
cd springbootProject
cp .env.example .env
# edit .env with real credentials/secrets
docker compose up --build
```

### 4) Run with scaling (distributed mode)

```zsh
cd springbootProject
docker compose up --build --scale app=2
```

Nginx listens on `http://localhost:8080` and forwards requests across app replicas.

## Environment Variables

Configured in `.env` (template available in `.env.example`):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `APP_STORAGE_DIR`

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Main Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/files/upload`
- `GET /api/files?page=0&size=10&sortBy=uploadedAt&direction=desc`
- `PATCH /api/files/{id}/rename`
- `GET /api/files/download/{id}`
- `DELETE /api/files/{id}`

## Internal Architecture (Package Layout)

The internal code structure is layered and simple:

- `src/main/java/springbootproject/config`
  - Security, JWT filter, Swagger configuration
- `src/main/java/springbootproject/controller`
  - API endpoints (`AuthController`, `FileController`)
- `src/main/java/springbootproject/dto`
  - Request/response payload models
- `src/main/java/springbootproject/service`
  - Business logic (`UserService`, `FileService`, `JwtService`)
- `src/main/java/springbootproject/repository`
  - JPA repositories for DB access
- `src/main/java/springbootproject/entity`
  - Database entities (`User`, `FileMetadata`)
- `src/main/java/springbootproject/exception`
  - Global exception handling and error response model

### Request Flow

1. Request enters `controller`
2. DTO validation runs
3. `service` handles business logic
4. `repository` talks to database
5. response returns from `controller`
6. errors are handled centrally by `GlobalExceptionHandler`

## API Client (Postman)

Import from `postman/`:

- `postman/Distributed-Storage.postman_collection.json`
- `postman/Distributed-Storage.local.postman_environment.json`

Recommended order:

1. `Auth -> Register`
2. `Auth -> Login` (stores `token` automatically)
3. `Files -> Upload` (stores `fileId` automatically)
4. Use list/rename/download/delete

## Tests

Tests run on `test` profile (H2 + isolated test storage path).

```zsh
cd springbootProject
# export JAVA_HOME=/path/to/your/jdk
# export PATH="$JAVA_HOME/bin:$PATH"
./mvnw test
```

## Notes

- This project is currently a strong monolith.
- Next planned evolution is modularization, then microservices/event-driven architecture.

