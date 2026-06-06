# FinTrack — Personal Finance Tracker

A production-grade microservices-based personal finance platform built with Java Spring Boot, Angular, and Apache Kafka.

## Architecture
Angular Frontend (4200)
│
API Gateway (8080) — JWT Auth, Routing, Rate Limiting
│
├── User Service (8081)
├── Transaction Service (8082)
└── Analytics Service (8083)
Infrastructure: MySQL · Kafka · Redis · Docker

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17, Angular Material, TypeScript |
| Backend | Java 21, Spring Boot, Spring Security |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka |
| Database | MySQL 8.0 |
| Cache | Redis |
| Auth | JWT (JJWT), BCrypt |
| Testing | JUnit 5, Mockito, Testcontainers |
| DevOps | Docker, Docker Compose |

## Services

### User Service
Handles authentication and identity management.
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control

### Transaction Service
Core financial data management.
- Income and expense tracking
- Category management
- IDOR protection — users access only their own data
- Publishes Kafka events on transaction creation

### Analytics Service
Real-time financial insights via event-driven architecture.
- Consumes Kafka events from Transaction Service
- Pre-aggregated monthly summaries
- Eventual consistency model

### API Gateway
Single entry point for all client requests.
- Centralized JWT validation
- Request routing
- Rate limiting via Redis token bucket

## Key Design Decisions

- **Database per service** — loose coupling, independent scaling
- **Event-driven architecture** — Kafka decouples Transaction and Analytics services
- **Stateless auth** — JWT enables horizontal scaling without shared session state
- **CQRS pattern** — Transaction Service handles writes, Analytics Service handles reads
- **Multi-stage Docker builds** — minimal production images

## Running Locally

### Prerequisites
- Java 21
- Node 18+
- Docker Desktop

### Start Infrastructure
```bash
docker-compose up -d mysql kafka redis
```

### Start Services
```bash
# Terminal 1
cd user-service && ./mvnw spring-boot:run

# Terminal 2
cd transaction-service && ./mvnw spring-boot:run

# Terminal 3
cd analytics-service && ./mvnw spring-boot:run

# Terminal 4
cd api-gateway && ./mvnw spring-boot:run

# Terminal 5
cd fintrack-frontend && ng serve
```

### Or start everything with Docker Compose
```bash
docker-compose up
```

Open `http://localhost:4200`

## API Endpoints

### Auth (Public)
POST /api/auth/register   — create account
POST /api/auth/login      — get JWT token

### Transactions (Protected)
GET    /api/transactions        — list all transactions
POST   /api/transactions        — create transaction
GET    /api/transactions/{id}   — get single transaction
DELETE /api/transactions/{id}   — delete transaction

### Categories (Protected)
GET    /api/categories      — list categories
POST   /api/categories      — create category
DELETE /api/categories/{id} — delete category

### Analytics (Protected)
GET /api/analytics/summary           — all monthly summaries
GET /api/analytics/summary/{year}/{month} — specific month

## System Design Highlights

- Handles horizontal scaling via stateless JWT authentication
- Kafka consumer groups enable parallel analytics processing
- Pre-aggregated monthly summaries for O(1) dashboard reads
- Circuit breaker ready via Resilience4j integration points
- Rate limiting prevents API abuse — 10 req/sec per user

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify
```

Test coverage includes unit tests with Mockito and integration 
tests with Testcontainers running real MySQL instances.

## Author

Pranav Nandurkar — [LinkedIn] https://www.linkedin.com/in/pranav-nandurkar-9501b219a/
