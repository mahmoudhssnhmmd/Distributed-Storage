# Distributed Storage System (under development)
 
A production-grade distributed file storage system built with **Java 17** and **Spring Boot 3.5**, modeled after core Google Drive functionality. Supports secure file upload, download, rename, and deletion with JWT-based authentication and Redis caching.
 
---
 
## Features
 
- **JWT Authentication** — Stateless, token-based auth using Spring Security. No sessions.
- **File Management** — Upload, download, rename, and delete files per authenticated user.
- **Redis Caching** — Paginated file listings are cached and automatically invalidated on write operations.
- **Pagination & Sorting** — Query files with configurable page size, sort field, and direction.
- **Input Validation** — Request validation via Spring `@Valid` with global exception handling.
- **API Documentation** — Interactive Swagger UI via SpringDoc OpenAPI.
- **Integration Tests** — Auth and file operation flows tested with Spring Boot Test + H2 in-memory DB.
---
 
## Tech Stack
 
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | PostgreSQL + Spring Data JPA |
| Cache | Redis (Spring Cache) |
| Build | Maven |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Spring Boot Test, H2 |
| Utilities | Lombok |
 
---
## Project Structure
 
```
src/main/java/springbootproject/
├── config/
│   ├── JwtAuthenticationFilter.java   # Intercepts requests and validates JWT tokens
│   ├── SecurityConfig.java            # Security rules and filter chain configuration
│   └── SwaggerConfig.java             # OpenAPI documentation setup
├── controller/
│   ├── AuthController.java            # /api/auth/register and /api/auth/login
│   └── FileController.java            # /api/files — upload, list, rename, delete, download
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── RenameFileRequest.java
├── entity/
│   ├── User.java                      # Implements UserDetails for Spring Security
│   └── FileMetadata.java              # File record stored in PostgreSQL
├── exception/
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java    # Centralized error handling
├── repository/
│   ├── UserRepository.java
│   └── FileMetadataRepository.java
└── service/
    ├── JwtService.java                # Token generation, validation, claims extraction
    ├── UserService.java               # Registration logic and user creation
    ├── CustomUserDetailsService.java  # Loads user from DB for Spring Security
    └── FileService.java               # Core file operations with caching
```
 
---
 
## Getting Started
 
### Prerequisites
 
- Java 17+
- PostgreSQL
- Redis
- Maven
### 1. Clone the repository
 
```bash
git clone https://github.com/mahmoudhssnhmmd/Distributed-Storage.git
cd Distributed-Storage
```
 
### 2. Configure environment

The project reads sensitive values from environment variables.

Create a local `.env` file from the template:

```bash
cp .env.example .env
```

Then edit `.env` with your own values:

```dotenv
# Database credentials
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_real_password

# JWT secret
JWT_SECRET=replace_with_a_long_random_secret

# Optional: storage dir (local run)
APP_STORAGE_DIR=uploads
```

If needed, you can also override the datasource URL (default: `jdbc:postgresql://localhost:5432/distributed-storage`) via `SPRING_DATASOURCE_URL`.
 
### 3. Run the application
 
```bash
./mvnw spring-boot:run
```
 
### 4. Access Swagger UI
 
```
http://localhost:8080/swagger-ui.html
```
 
---
 
## API Endpoints
 
### Auth
 
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive a JWT token |
 
### Files (require `Authorization: Bearer <token>`)
 
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/files/upload` | Upload a file |
| GET | `/api/files` | List files (paginated) |
| GET | `/api/files/download/{id}` | Download a file |
| PATCH | `/api/files/{id}/rename` | Rename a file |
| DELETE | `/api/files/{id}` | Delete a file |
 
**Pagination query params for GET `/api/files`:**
 
| Param | Default | Description |
|---|---|---|
| `page` | `0` | Page number |
| `size` | `10` | Items per page |
| `sortBy` | `uploadedAt` | Field to sort by |
| `direction` | `desc` | `asc` or `desc` |
 
---
 
## How Authentication Works
 
1. User registers and logs in via `/api/auth/login`
2. Server returns a signed JWT token
3. Client sends the token in every request: `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` intercepts the request, validates the token, and loads the user from the database
5. Spring Security grants access based on the authenticated user
---
 
## Running Tests
 
```bash
./mvnw test
```

Make sure your database credentials are valid in `.env` because Spring context tests require a working datasource.
 
---
 
## Planned Features (Phase 2)
 
- [ ] Apache Kafka integration for async file event processing
- [ ] Docker & Docker Compose setup for containerized deployment
- [ ] Jenkins CI/CD pipeline
- [ ] File sharing between users
- [ ] apply design principles (SOLID, Design Patterns)
---
 
## Author
 
**Mahmoud Hammad**
[GitHub](https://github.com/mahmoudhssnhmmd) · [LinkedIn](https://linkedin.com)
 
