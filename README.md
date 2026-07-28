# Flash Sale X



## Directory Meaning

1. Controller
    - Exposes REST APIs
    - Contains almost no business logic
2. Service
    - Contains all business logic
3. Mapper
    - Converts one object into another
4. Entity
    - Represents the database tables
5. Repository
    - Only responsible for database access
6. DTO (Data Transfer Object)
    - Objects used only for communications

## Target Code Structure

```
com.flashsalex
├── FlashSaleApplication.java
│
├── config
│
├── common
│   ├── constants
│   ├── exception
│   ├── response
│   ├── util
│   └── mapper
│
├── security
│
├── product
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   └── mapper
│
├── inventory
│
├── order
│
├── payment
│
├── kafka
│
├── redis
│
├── metrics
│
└── scheduler
```