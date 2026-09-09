# FlashSaleX — LLM Project Context

> **Purpose:** This file is the canonical context for LLM-assisted development of FlashSaleX.  
> **Last updated:** 2026-09-08  
> **Project status:** Early implementation — database and JPA domain model completed; Authentication is the current feature.

---

## 1. Project Identity

**Project:** FlashSaleX

**Primary objective:** Build a production-grade backend that simulates a high-traffic e-commerce flash-sale platform, with particular emphasis on concurrency, inventory correctness, scalability, resilience, observability, and production readiness.

This is intentionally **not a simple CRUD application**. The project is designed to demonstrate Senior Software Engineer-level backend engineering and distributed-system thinking.

The system should eventually be capable of simulating extreme traffic, including approximately **1 million concurrent users**, while ensuring that limited inventory is never oversold.

---

# 2. Problem Statement

During a flash sale, a limited quantity of a product becomes available to a very large number of users at almost the same time.

### Example

- Available inventory: **100**
- Incoming purchase attempts: **1,000,000**

The backend must ensure that:

1. **Inventory is never oversold.**
2. Requests remain low-latency under extreme load.
3. The system remains highly available.
4. Sudden traffic spikes do not bring down the application.
5. Application instances can scale horizontally.
6. Concurrent requests are handled correctly.
7. Failures are isolated and recoverable.
8. System behavior is observable through metrics, logs, and health checks.
9. Asynchronous processing can be introduced where synchronous processing would become a bottleneck.
10. The design remains maintainable as functionality grows.

### Core engineering challenge

The most important technical challenge is **concurrent inventory management**.

If many users attempt to buy the final few units simultaneously, the system must coordinate access to inventory so that successful orders never exceed the actual stock.

This makes FlashSaleX primarily a project about:

- concurrency control
- consistency
- scalability
- distributed systems
- caching
- asynchronous processing
- fault tolerance
- observability
- performance engineering

---

# 3. Technology Stack

## Backend

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA

## Database

- PostgreSQL
- Flyway

## Planned Infrastructure

- Redis — caching
- Kafka — asynchronous event processing
- Docker
- Docker Compose

## Planned Observability

- Micrometer
- Prometheus
- Grafana

## Planned Testing / Performance

- JUnit
- Testcontainers
- k6
- JMeter

---

# 4. Architectural Direction

The project follows a **feature-first package structure** rather than grouping the entire application only by technical layer.

Target structure:

```text
auth/
product/
inventory/
order/
common/
config/
```
Each major feature is expected to contain relevant components such as:
controller/
service/
repository/
entity/
dto/
mapper/
The exact package organization may evolve as the implementation grows, but feature ownership should remain clear.
5. Development Principles
The implementation should follow:
SOLID principles
Clean Architecture principles where practical
DTO-based APIs
Repository Pattern
Service Layer
Constructor Injection
Feature-first packaging
Production-grade exception handling
Validation
Clear separation of concerns
Testable business logic
API rule
Never expose JPA entities directly from REST APIs.
Use DTOs for request and response contracts.
Dependency injection
Prefer:
Constructor Injection
Avoid:
Field Injection
Controllers
Controllers should remain thin.
Avoid putting business logic inside controllers.
Business rules belong primarily in services/domain-oriented components.
6. Database Status
PostgreSQL is configured.
Flyway is configured and used for schema migrations.
Current schema tables:
users
refresh_tokens
product
inventory
orders
flyway_schema_history
Migrations
V1
Created:
users
refresh_tokens
V2
Created:
product
V3
Created:
inventory
V4
Created:
orders
7. JPA Domain Model — Completed
The following entities have been created:
User.java
Product.java
Inventory.java
Order.java
RefreshToken.java
Relationships
User
User
 └── OneToMany -> Orders
Order
Order
 ├── ManyToOne -> User
 └── ManyToOne -> Product
Product
Product
 ├── OneToMany -> Orders
 └── OneToOne -> Inventory
Inventory
Inventory
 └── OneToOne -> Product
Inventory uses a shared-primary-key style relationship with:
@MapsId
Inventory also uses:
@Version
for optimistic locking.
8. JPA / Coding Practices Already Applied
The domain implementation currently uses:
@Entity
@Table
@Id
@GeneratedValue where applicable
@Enumerated(EnumType.STRING) for enums
LocalDateTime
Lazy loading where appropriate
Builder Pattern
Lombok
BCrypt through PasswordEncoder
DTO-first API direction
Optimistic locking on inventory
@MapsId for the inventory/product shared-key mapping
9. Current Implementation Status
Completed
Database
PostgreSQL configured
Flyway configured
Initial migration scripts created
Core tables created
Domain Layer
User entity created
Product entity created
Inventory entity created
Order entity created
RefreshToken entity created
Entity relationships created
Inventory shared-primary-key mapping implemented
Inventory optimistic locking added
Recent Issues Already Resolved
The implementation has already addressed issues involving:
table-name mismatches
entity relationship mismatches
Hibernate validation problems
inventory primary-key mapping
shared primary-key mapping using @MapsId
10. Current Feature — Authentication
Goal
Implement JWT-based authentication.
Authentication is the current active feature.
Required functionality
User registration
User login
JWT generation
JWT validation
JWT authentication filter
Refresh tokens
Spring Security configuration
Role-based authorization
11. Authentication Work Remaining
The following authentication components are currently not started:
DTOs
Mappers
Controllers
Services
The next implementation should therefore begin with the Authentication feature rather than jumping ahead to Product, Inventory, Redis, or Kafka.
A likely implementation progression is:
Auth DTOs
    ↓
Auth Mapper
    ↓
Auth Service
    ↓
JWT Service / Token Service
    ↓
Refresh Token Service
    ↓
Auth Controller
    ↓
JWT Authentication Filter
    ↓
Spring Security Configuration
    ↓
Role-Based Authorization
    ↓
Authentication Tests
The exact class names and internal design should be chosen based on the existing project structure rather than blindly introducing duplicate abstractions.
12. Future Roadmap
Phase 1 — Authentication
Implement:
Register API
Login API
JWT access token
JWT validation
JWT security filter
Refresh token flow
Spring Security configuration
Role-based authorization
Phase 2 — Product APIs
Implement:
Create Product
Update Product
Product Listing
Potential future additions:
Product details
Pagination
Filtering
Sorting
Search
Phase 3 — Inventory
Implement:
Inventory reservation
Inventory release
Optimistic locking
Correct concurrent stock handling
This phase should establish the correctness model that the flash-sale workflow will rely on.
Phase 4 — Flash Sale
Implement:
Purchase API
Inventory reservation
Order creation
Overselling prevention
Concurrent purchase handling
This is the central feature of the project.
The implementation should eventually be tested under significant concurrency rather than being considered complete merely because sequential functional tests pass.
Phase 5 — Redis
Introduce Redis for:
Product caching
Inventory-related caching where appropriate
Reducing repeated database reads
Supporting high-read traffic
Caching decisions must preserve inventory correctness. Redis should not be introduced in a way that allows stale cache state to become the source of truth for stock correctness.
Phase 6 — Kafka
Introduce Kafka for asynchronous event processing.
Planned event categories:
Order events
Payment events
Notification events
Potential architectural direction:
Purchase Request
      ↓
Order / Reservation
      ↓
Event
      ↓
Kafka
 ┌────┼───────────┐
 ↓    ↓           ↓
Payment  Notification  Other Consumers
The exact event contracts and delivery guarantees should be designed when this phase begins.
Phase 7 — Performance Engineering
Focus on:
Database indexing
Query optimization
Connection pooling
Transaction boundaries
Locking strategy
Cache effectiveness
Kafka throughput
Application-level bottlenecks
JVM/runtime behavior where relevant
Performance work should be driven by measurements rather than assumptions.
Phase 8 — Production Readiness
Implement:
Docker
Docker Compose
Health checks
Metrics
Structured logging
Global exception handling
Validation
Configuration management
Operational visibility
Planned monitoring stack:
Application
    ↓
Micrometer
    ↓
Prometheus
    ↓
Grafana
Phase 9 — Load Testing
Use:
k6
JMeter
Target:
~1,000,000 concurrent users (simulated)
The target is a simulation goal, not an assumption that the application will automatically achieve this throughput.
Load testing should progressively increase traffic and identify:
throughput limits
latency degradation
database saturation
connection pool saturation
lock contention
cache bottlenecks
Kafka bottlenecks
CPU/memory pressure
failure behavior
13. Important Non-Functional Requirements
The system should ultimately demonstrate:
Correctness
Never oversell inventory.
Maintain consistent order/inventory state.
Handle concurrent requests safely.
Performance
Low latency for normal operations.
Graceful behavior during traffic spikes.
Efficient database access.
Avoid unnecessary synchronous work.
Scalability
Horizontally scalable application instances.
Stateless authentication/access-token validation where practical.
Redis and Kafka used where they provide meaningful scalability benefits.
Availability
Application should tolerate individual component failures where practical.
Health checks should expose service health.
Failure handling should avoid cascading failures.
Observability
Expose enough telemetry to answer:
How many requests are arriving?
What is the request latency?
How many requests fail?
Where are errors occurring?
Is the database saturated?
Is inventory contention increasing?
Are Kafka consumers lagging?
Is Redis unavailable?
14. Critical Design Challenge — Inventory Correctness
Inventory is the most important correctness boundary in the system.
Example:
Stock = 1

User A ──┐
User B ──┼──> Purchase
User C ──┘
Only one request should successfully reserve the final unit.
The implementation must account for race conditions such as:
Read stock = 1
Read stock = 1
Decrease stock
Decrease stock
A naive read-modify-write implementation can result in overselling.
The current model already includes optimistic locking through:
@Version
However, this should be treated as the current design direction, not as proof that the final flash-sale architecture is solved.
As concurrency requirements increase, evaluate:
optimistic locking
pessimistic locking
atomic database updates
reservation semantics
transaction boundaries
Redis atomic operations
queue-based admission
partitioning/sharding if eventually required
Any change should be justified using correctness and measured performance.
15. Important Architectural Rule
Do not prematurely introduce Redis, Kafka, microservices, or complex distributed patterns merely because the project is intended to scale.
The preferred progression is:
Correctness
    ↓
Clean domain/service design
    ↓
Database correctness
    ↓
Concurrency correctness
    ↓
Performance measurement
    ↓
Caching / messaging
    ↓
Horizontal scalability
    ↓
Production hardening
Every infrastructure component should solve a demonstrated problem.
16. Expected Engineering Quality
This project should be reviewed as if it were being evaluated for a:
Senior Software Engineer backend role
Reviews should focus on:
Scalability
Can the design handle high concurrency?
What becomes the first bottleneck?
Can application instances scale horizontally?
Does the database become the bottleneck?
Performance
Are queries efficient?
Are indexes appropriate?
Are transactions too large?
Is locking creating contention?
Is connection pooling configured appropriately?
Maintainability
Are responsibilities separated?
Are abstractions justified?
Is business logic testable?
Are APIs represented through DTOs?
Is package ownership clear?
Reliability
What happens when PostgreSQL fails?
What happens when Redis fails?
What happens when Kafka is unavailable?
Can requests be retried safely?
Are operations idempotent where required?
Security
Passwords must never be stored in plaintext.
JWT validation must be secure.
Refresh tokens require careful lifecycle management.
Authorization must be enforced server-side.
Sensitive information must not be exposed in API responses or logs.
17. API Design Direction
The application should expose REST APIs using DTO contracts.
Do not return:
JPA Entity -> Controller -> JSON
Prefer:
Request
  ↓
Request DTO
  ↓
Controller
  ↓
Service
  ↓
Repository / Domain
  ↓
Response DTO
  ↓
JSON
Validation should be performed at appropriate boundaries.
Global exception handling should eventually provide consistent API error responses.
18. Testing Strategy
Testing should evolve with each feature.
Unit tests
Test:
Services
Business rules
Token logic
Inventory reservation logic
Validation behavior
Integration tests
Test:
PostgreSQL interactions
Flyway migrations
JPA mappings
Spring Security integration
REST endpoints
Testcontainers
Use Testcontainers for realistic integration environments where appropriate.
Concurrency tests
Inventory and flash-sale logic must eventually include concurrent tests.
The goal is to verify:
successful purchases <= available inventory
under concurrent load.
Load tests
Use:
k6
JMeter
to measure behavior under progressively increasing load.
19. Current State vs Future State
Current state
PostgreSQL
   │
Flyway
   │
JPA Entities
   │
Domain Relationships
   │
Authentication ← CURRENT FEATURE
Authentication is currently being implemented.
Target state
                    ┌───────────────┐
                    │    Clients    │
                    └───────┬───────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Spring Boot APIs  │
                  └───────┬───────────┘
                          │
             ┌────────────┼────────────┐
             ▼            ▼            ▼
           Auth        Product      Order/Flash Sale
             │            │            │
             │            ▼            ▼
             │          Redis      Inventory
             │                         │
             └────────────┬────────────┘
                          ▼
                     PostgreSQL
                          │
                          ▼
                        Kafka
                     ┌────┼────┐
                     ▼    ▼    ▼
                  Payment Notification
                    /     Other Events
This diagram is conceptual. The final architecture should be determined incrementally as requirements and performance measurements emerge.
20. LLM Instructions for Future Development
When working on FlashSaleX, an LLM should:
Read this file before proposing implementation changes.
Treat the Current Implementation Status as the source of truth for project progress.
Do not claim future functionality is already implemented.
Do not skip directly to future phases unless explicitly requested.
Preserve existing architectural decisions unless there is a strong engineering reason to change them.
If proposing a change to an existing design, explain:
current approach
problem with it
proposed approach
trade-offs
migration impact
Prefer production-grade solutions over toy implementations.
Avoid unnecessary abstractions.
Do not expose JPA entities directly through APIs.
Prefer constructor injection.
Keep controllers thin.
Put business logic in appropriate service/domain components.
Use transactions deliberately.
Treat inventory correctness as a critical invariant.
Consider concurrency explicitly whenever modifying inventory/order behavior.
Do not assume optimistic locking alone solves all flash-sale scalability problems.
Add tests alongside feature implementation.
Do not introduce Redis/Kafka solely for architectural aesthetics.
Measure before optimizing where practical.
Challenge existing decisions when a materially better production design exists.
21. Immediate Next Task
Authentication implementation
The next task is to implement the authentication module.
Required sequence
1. Define Authentication DTOs
2. Implement mapping
3. Implement authentication service
4. Implement password handling
5. Implement JWT generation/validation
6. Implement refresh-token handling
7. Implement authentication controller
8. Implement JWT security filter
9. Configure Spring Security
10. Add role-based authorization
11. Add tests
12. Verify the complete authentication flow
Do not begin Product APIs until the authentication feature has reached a stable, tested state unless explicitly instructed otherwise.
22. Definition of Done
A feature should not be considered complete merely because it compiles.
For each feature, aim for:
Implementation
    +
Validation
    +
Error Handling
    +
Security
    +
Unit Tests
    +
Integration Tests where appropriate
    +
Documentation
For high-concurrency features such as flash-sale purchasing:
Functional Correctness
    +
Concurrency Correctness
    +
Load Testing
    +
Performance Analysis
23. Project Evolution Strategy
FlashSaleX should evolve through measurable engineering milestones:
Foundation
   ↓
Authentication
   ↓
Product
   ↓
Inventory
   ↓
Flash Sale
   ↓
Concurrency Validation
   ↓
Redis
   ↓
Kafka
   ↓
Observability
   ↓
Load Testing
   ↓
Performance Optimization
   ↓
Production Hardening
The end goal is not merely a working e-commerce API.
The end goal is a demonstrable backend engineering system showing how to design, implement, test, measure, and evolve a system under extreme traffic and concurrency.
24. One-Paragraph Project Summary
FlashSaleX is a production-oriented Spring Boot backend that simulates an e-commerce flash-sale system where a limited inventory must be sold correctly under extreme concurrent demand. The project uses Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL, and Flyway, with Redis, Kafka, Docker, observability tooling, and load-testing infrastructure planned for later phases. The database schema and JPA domain model are already implemented, including User, Product, Inventory, Order, and RefreshToken entities, their relationships, shared-primary-key inventory mapping, and optimistic locking. The current active feature is JWT-based authentication, with DTOs, mappers, controllers, services, JWT validation, refresh tokens, security configuration, and role-based authorization still to be implemented. Future phases will build product APIs, concurrency-safe inventory reservation, flash-sale purchasing, caching, asynchronous events, performance optimization, production observability, and large-scale load testing.