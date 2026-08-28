# FlashSaleX

A production-grade backend system simulating an e-commerce flash sale platform — built to handle extreme traffic spikes (e.g. 1,000,000 requests against 100 units of inventory) without overselling, while staying observable, fault-tolerant, and horizontally scalable.

This isn't a CRUD demo. It's an exercise in senior-level backend engineering: distributed system design, concurrency control, caching, event-driven architecture, and production readiness — built incrementally, following real SDLC practices.

---

## Problem Statement

During a flash sale, millions of users attempt to purchase a limited number of units simultaneously. The system must:

- Never oversell inventory
- Maintain low latency under load
- Remain highly available
- Absorb sudden traffic spikes
- Scale horizontally
- Stay observable and fault tolerant

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 4 |
| Security | Spring Security, JWT (JJWT) |
| Persistence | PostgreSQL, Spring Data JPA, Flyway |
| Caching | Redis |
| Messaging | Kafka |
| Deployment | Docker, Docker Compose |
| Monitoring (planned) | Micrometer, Prometheus, Grafana |
| Testing (planned) | JUnit, Testcontainers, k6, JMeter |

---

## Architecture

Feature-based package structure — each domain owns its full vertical slice:

```
com.devangdayal.flashsale
├── FlashsaleApplication.java
│
├── config              # Security config, global exception handling
├── common              # Shared constants, exceptions, response wrappers, mappers
├── properties           # Typed configuration properties (Kafka, Redis)
│
├── auth
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service / service.impl
│
├── user
│   ├── entity
│   ├── enums
│   ├── mapper
│   ├── repository
│   └── service
│
├── product
├── inventory
└── order
```

**Layering rules:**
- **Controller** — exposes REST APIs, no business logic
- **Service** — owns all business logic
- **Mapper** — converts entities ↔ DTOs
- **Entity** — maps to database tables, never returned by an API
- **Repository** — database access only
- **DTO** — the only objects that cross the API boundary

---

## Data Model

Managed via Flyway migrations (`V1`–`V4`):

- `users`, `refresh_tokens`
- `product`
- `inventory`
- `orders`

**Relationships**
- `User` 1—* `Order`
- `Order` *—1 `User`, *—1 `Product`
- `Product` 1—1 `Inventory` (shared primary key via `@MapsId`)
- `Inventory` uses **optimistic locking** (`@Version`) to prevent overselling under concurrent writes

---

## Current Status

### ✅ Completed — Authentication

- User registration & login with BCrypt password hashing
- JWT access token generation
- Refresh token issuance, verification, expiry and revocation handling
- Stateless session configuration via Spring Security
- Database schema, entities, and relationships for auth/user/product/inventory/order

### 🚧 In Progress

- **JWT request-time validation** — tokens are issued but not yet enforced on protected endpoints via a security filter
- **Global exception handling** — currently a stub; service-layer errors aren't yet mapped to proper HTTP responses
- **Role-based authorization** — `UserRole` exists on the entity but isn't wired into route rules yet
- **Automated test coverage** — only the default Spring Boot context test exists today

### ⏭️ Not Started

- Product, Inventory, and Order APIs (entities/repos exist; service/controller logic pending)
- Redis caching layer
- Kafka event publishing (order, payment, notification events)
- Load testing (k6, JMeter) and performance tuning
- Observability stack (Prometheus/Grafana dashboards)

---

## Roadmap

| Phase | Scope |
|---|---|
| 1 | Authentication — register, login, JWT, refresh tokens, RBAC |
| 2 | Product APIs — create, update, list |
| 3 | Inventory — reserve/release with optimistic locking |
| 4 | Flash Sale core — purchase flow, reservation, oversell prevention |
| 5 | Redis — product & inventory caching |
| 6 | Kafka — order/payment/notification events |
| 7 | Performance — indexing, query tuning, connection pooling |
| 8 | Production readiness — health checks, metrics, structured logging, exception handling |
| 9 | Load testing — k6, JMeter, targeting 1M simulated concurrent users |

---

## Getting Started

### Prerequisites
- Java 21
- Maven (or the included wrapper, `./mvnw`)
- PostgreSQL, Redis, Kafka (or run via `docker/docker-compose.yml`)

### Configuration

Copy the example config and fill in your local values:

```bash
cp flashsale/src/main/resources/application.yml.example flashsale/src/main/resources/application.yml
```

Set `jwt.secret` via an environment variable rather than committing it directly.

### Run

```bash
cd flashsale
./mvnw spring-boot:run
```

### Docker

```bash
docker compose -f docker/docker-compose.yml up
```

---

## Coding Standards

**Follow:** SOLID, Clean Architecture, DTO-based APIs, Repository pattern, Service layer, constructor injection, feature-first packaging.

**Avoid:** field injection, returning JPA entities from controllers, business logic inside controllers.

---

## Docs

Further design detail lives under [`docs/`](./docs):
- [`architecture.md`](./docs/architecture.md)
- [`system-design.md`](./docs/system-design.md)
- [`capacity-planning.md`](./docs/capacity-planning.md)
- [`api.md`](./docs/api.md)
- [`deployment.md`](./docs/deployment.md)