# CodeRabbitTest

# Project Loom – Spring Boot REST API Demo

A minimal Spring Boot 3.2 application that demonstrates how a REST API works with **Project Loom** (virtual threads) introduced as a stable feature in Java 21 ([JEP 444](https://openjdk.org/jeps/444)).

## What is Project Loom?

Project Loom brings *virtual threads* to the JVM. Unlike platform (OS) threads, virtual threads are extremely lightweight: you can create millions of them. When a virtual thread blocks (e.g. on a database call or HTTP request), the JVM *unmounts* it from its carrier OS thread so the carrier can run other virtual threads. This dramatically improves throughput for I/O-bound workloads without changing the familiar thread-per-request programming model.

## How virtual threads are enabled

A single property in `application.properties` switches Tomcat's request executor to use one virtual thread per request:

```properties
spring.threads.virtual.enabled=true
```

Spring Boot 3.2 + Tomcat 10.1 handle everything else automatically.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/threads/platform` | Runs 10 simulated I/O tasks on platform threads (fixed thread pool) |
| GET | `/api/threads/virtual`  | Runs the same 10 tasks on virtual threads (`newVirtualThreadPerTaskExecutor`) |
| GET | `/api/threads/info`     | Returns metadata about the thread handling this HTTP request |

### Sample response – `/api/threads/virtual`

```json
{
  "threadType": "virtual",
  "taskCount": 10,
  "elapsedMs": 105,
  "results": [
    "Task 1 completed on virtual thread [virtual=true, name=]",
    "Task 2 completed on virtual thread [virtual=true, name=]",
    ...
  ]
}
```

### Sample response – `/api/threads/info`

```json
{
  "threadName": "",
  "isVirtual": true,
  "threadId": 42
}
```

`isVirtual: true` confirms that the HTTP request itself was handled by a virtual thread.

## Prerequisites

- **Java 21** or later (virtual threads are a stable feature since Java 21)
- **Maven 3.6+**

## Running the application

```bash
mvn spring-boot:run
```

Then open your browser or use curl:

```bash
curl http://localhost:8080/api/threads/info
curl http://localhost:8080/api/threads/platform
curl http://localhost:8080/api/threads/virtual
```

## Running the tests

```bash
mvn test
```
