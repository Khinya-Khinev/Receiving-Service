# Receiving Service

[![Receiving Service CI](https://github.com/kamen-kamen/Receiving-Service/actions/workflows/ci.yaml/badge.svg)](https://github.com/kamen-kamen/Receiving-Service/actions/workflows/ci.yaml)

Backend service for warehouse goods receiving: verifies scanned items against incoming shipments (ASN), report discrepancies, and triggers putaway once a handling unit is fully received.

## Purpose of project

To get hands-on experience, encounter complexity and problems to solve and as a result grow. Sandbox to try out different interesting things.

## Workflow Diagram


<p align="center">
  <img src="docs/receiving-process-diagram.png" alt="Receiving Process Diagram" width=350/>
</p>

## Architecture & Hexagonal Structure

The service follows pragmatic Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) to isolate core business logic from frameworks, databases, and external messaging systems.

```mermaid
flowchart LR
    classDef client fill:#1e1e2f,stroke:#7b2cbf,stroke-width:2px,color:#fff
    classDef api fill:#2d1b4e,stroke:#9d4edd,stroke-width:2px,color:#fff
    classDef app fill:#1b263b,stroke:#415a77,stroke-width:2px,color:#fff
    classDef domain fill:#0d1b2a,stroke:#00b4d8,stroke-width:2px,color:#fff
    classDef infra fill:#1f2421,stroke:#2a9d8f,stroke-width:2px,color:#fff
    classDef db fill:#14110f,stroke:#e76f51,stroke-width:2px,color:#fff

    Client["REST Client"]
    Controller["Controller<br/>"]
    Service["Application Service<br/>orchestrates domain"]
    Domain["Domain<br/>invariants and business actions"]
    Port["Port<br/>interfaces owned by application"]
    Repository["Repository Adapter<br/>"]
    Database[("Database")]

    Client --> Controller
    Controller --> Service
    Service -.orchestrates.-> Domain
    Service -.calls.-> Port
    Port -.implements.-> Repository
    Repository --> Database

    class Client client
    class Controller api
    class Service app
    class Domain,Port domain
    class Repository infra
    class Database db
```


## What's cool about it?

- **Hexagonal Architecture & DDD**: Strict separation into Domain models in receiving_process package (`GoodsReceipt`, `WorkerReceivingSession`), Application Ports, and Infrastructure Adapters.
- **Built-in Idempotency Control**: Idempotency interceptor layer backed by Redis to prevent duplicate scans or double processing during operations. 
- **RFC 9457 Problem Details**: Standardized global exception handling with domain-specific error codes (`AsnErrorCode`, `ReceivingErrorCode`, `DatabaseErrorCode`) and a fluent `AppException` API for attaching debug context (`AppException.of(RECEIPT_NOT_FOUND).with("receipt_id", receipt.id())`).
- **JWT-based Authentication**: Stateless auth with access/refresh token rotation.

## Tech Stack

Java 25, Spring Boot 4, PostgreSQL, Apache Kafka, Apache Maven, Redis

## Getting Started

### Prerequisites

- **JDK 25** or later
- **Apache Maven**
- **Docker**

### Environment Variables

Key configuration variables are in `.env`(copy from .env.example):
- `JWT_SECRET`: Secret key for JWT token signing.
- `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION` - Token expiration time  in milliseconds.
- `DB_NAME`, `DB_USER`, `DB_PASSWORD`: PostgreSQL connection credentials.
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap address (`kafka:9092` inside Docker, `localhost:9092` externally).

### Running the Application
**Start Infrastructure & Service with Docker Compose:**
   ```sh
   docker compose up --build
   ```

   This will spin up the following containers:
   - **PostgreSQL** (`5432`) - Relational database for domain persistence and idempotency store.
   - **Apache Kafka** (`9092` / `9093`, KRaft mode) - Event broker for putaway tasks and discrepancy events.
   - **Redis** (`6379`) - Idempotency control layer.
   - **Receiving Service** (`8080`) - Spring Boot backend application.


## Swagger: API Overview 

List of endpoints and request/response schemas: `http://localhost:8080/swagger-ui/index.html`
Pass access JWT token from login endpoint in Authorize to automatically send token with requests.

## Project Structure 

```
src/main/java/com/waregang/receiving_service/
├── advanced_shipping_notice/       ### ASN domain (ASNs, expected handling units, contents, arrival timelines)
│   ├── api/                        # REST Controllers & DTOs for managing ASNs
│   ├── application/                # Application services & mapping logic
│   ├── domain/                     # Domain models (AdvancedShippingNotice, HandlingUnit, Content)
│   └── infrastructure/             # Database adapters & JPA repositories
├── receiving_process/              ### Core goods receiving execution engine
│   ├── api/                        # Scanning endpoints, receiving session controllers & DTOs
│   ├── application/                # Receiving process and Goods Receipt services 
│   ├── domain/                     # Domain models (GoodsReceipt, WorkerReceivingSession, ReceivedUnit)
│   └── infrastructure/             # Persistence adapters & JPA repositories
├── integration/                    ### Event-driven external system integrations
│   ├── discrepancies_report/       # Kafka adapter and service for discrepancy reports
│   └── putaway/                    # Kafka adapter and service for putaway notifying
├── security/                       ### Security configuration, JWT authentication, user management
└── common/                         ### Cross-cutting concerns (idempotency interceptors, global exception handling, custom validation annotations, app-side UUID generation)
```