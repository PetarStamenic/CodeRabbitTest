# CodeRabbitTest

A generic **Spring Boot 3.2** backend REST API that leverages **Project Loom virtual threads** (Java 21).

## Tech stack

| Concern | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Concurrency | Project Loom – virtual threads |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Error format | RFC 7807 `ProblemDetail` |

## Project Loom highlights

* `spring.threads.virtual.enabled=true` in `application.properties` switches **Tomcat's connector thread pool** to virtual threads automatically (Spring Boot 3.2+).
* `VirtualThreadConfig` exposes a named `Executor` bean (`virtualThreadExecutor`) backed by `Executors.newVirtualThreadPerTaskExecutor()` for fine-grained `@Async` usage.
* `UserService.findByIdAsync()` demonstrates `@Async("virtualThreadExecutor")` – work dispatched to a fresh virtual thread per call.

## REST API

Base path: `/api/users`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by id |
| `GET` | `/api/users/{id}/async` | Get user (async / virtual thread) |
| `POST` | `/api/users` | Create a new user |
| `PUT` | `/api/users/{id}` | Update an existing user |
| `DELETE` | `/api/users/{id}` | Delete a user |

### Sample request

```bash
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com"}'
```

### Sample response

```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "createdAt": "2024-04-01T10:00:00Z"
}
```

## Running locally

Requires **Java 21** and **Maven 3.6+**.

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.  
An H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:demodb`).

## Running tests

```bash
mvn test
```
