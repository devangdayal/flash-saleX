# FlashSaleX - Project Context

## Overview

FlashSaleX is a production-grade backend system that simulates an e-commerce flash sale platform capable of handling extremely high traffic while preventing overselling.

The goal of this project is not simply CRUD development but demonstrating Senior Software Engineer-level backend engineering, scalability, production readiness, and distributed system design.

The project is being built incrementally following real software development lifecycle practices.

---

# Problem Statement

During a flash sale, millions of users attempt to purchase a limited number of products simultaneously.

Example:

- Product Inventory = 100
- Incoming Requests = 1,000,000

The system must:

- Never oversell inventory
- Maintain low latency
- Remain highly available
- Handle sudden traffic spikes
- Scale horizontally
- Remain observable and fault tolerant

---

# Tech Stack

Backend
- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA

Database
- PostgreSQL
- Flyway

Caching
- Redis

Messaging
- Kafka

Deployment
- Docker
- Docker Compose

Monitoring (planned)
- Micrometer
- Prometheus
- Grafana

Testing (planned)
- JUnit
- Testcontainers
- k6
- JMeter

---

# Architecture

Feature-based package structure.

Example

auth/
product/
inventory/
order/
common/
config/

Each feature contains

- controller
- service
- repository
- entity
- dto
- mapper

---

# Development Principles

Follow:

- SOLID
- Clean Architecture
- DTO-based APIs
- Repository Pattern
- Service Layer
- Constructor Injection
- Production-grade code
- Feature-first packaging

Never expose JPA entities through APIs.

---

# Database

Tables

- users
- refresh_tokens
- product
- inventory
- orders

Managed using Flyway.

---

# Completed

## Database

✔ PostgreSQL configured

✔ Flyway configured

✔ Initial migration scripts created

V1
- users
- refresh_tokens

V2
- product

V3
- inventory

V4
- orders

---

## JPA

Created entities

- User
- Product
- Inventory
- Order
- RefreshToken

Relationships added

User
- OneToMany -> Orders

Order
- ManyToOne -> User
- ManyToOne -> Product

Product
- OneToMany -> Orders
- OneToOne -> Inventory

Inventory
- OneToOne -> Product
- @MapsId
- Optimistic Locking using @Version

---

## Best Practices Applied

- LocalDateTime
- EnumType.STRING
- PasswordEncoder (BCrypt)
- LAZY loading
- DTO-first approach
- Builder Pattern
- Lombok

---

# Current Status

Project compiles close to successfully.

Recently fixed

- Table name mismatches
- Entity relationships
- Hibernate validation issues
- Inventory primary key mapping
- Shared primary key using @MapsId

Current work

Authentication module.

---

# Upcoming Roadmap

Phase 1

Authentication

- Register
- Login
- JWT
- Refresh Token
- Spring Security
- Role Based Authorization

Phase 2

Product APIs

- Create Product
- Update Product
- Product Listing

Phase 3

Inventory

- Reserve inventory
- Release inventory
- Optimistic Locking

Phase 4

Flash Sale

- Purchase API
- Inventory Reservation
- Prevent Overselling

Phase 5

Redis

- Product Cache
- Inventory Cache

Phase 6

Kafka

- Order Events
- Payment Events
- Notification Events

Phase 7

Performance

- Indexing
- Query Optimization
- Connection Pooling

Phase 8

Production Readiness

- Docker
- Health Checks
- Metrics
- Logging
- Exception Handling

Phase 9

Load Testing

- k6
- JMeter

Target

1 Million concurrent users (simulated)

---

# Coding Standards

Use

- Constructor Injection
- DTOs
- MapStruct (planned)
- Validation
- Global Exception Handler

Avoid

- Field Injection
- Returning Entities
- Business Logic inside Controllers

---

# Expected Review Style

Review this project as if it were being evaluated for:

Senior Software Engineer

Focus on

- Production readiness
- Scalability
- Maintainability
- Performance
- Design decisions

Challenge design decisions when better alternatives exist.
