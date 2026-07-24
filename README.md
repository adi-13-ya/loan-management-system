# Multi-Branch Loan Management & Approval Workflow System

A full-stack banking domain application that digitizes the end-to-end loan lifecycle across multiple bank branches. Built with **Spring Boot 3** + **React 18** following **Test-Driven Development (TDD)**.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![React](https://img.shields.io/badge/React-18-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue) ![Docker](https://img.shields.io/badge/Docker-Compose-blue)

---

## Features

### Loan Lifecycle Workflow (State Machine)
```
DRAFT → SUBMITTED → UNDER_REVIEW → FORWARDED_TO_MANAGER → APPROVED → DISBURSED
                          ↓                    ↓
                      REJECTED             REJECTED
```

### Role-Based Access Control (RBAC)
| Role | Capabilities |
|------|-------------|
| **Customer** | Apply for loans, upload documents, track status, view EMI schedule, simulate repayments |
| **Branch Officer** | Review applications, verify documents, forward to manager or reject |
| **Branch Manager** | Final approval/rejection, triggers disbursement + EMI generation, branch portfolio view |
| **Admin** | Cross-branch visibility, manage branches & users, view audit logs, system analytics |

### Core Engineering Highlights
- **Approval state machine** — structurally prevents illegal transitions
- **Reducing-balance EMI calculation** — mathematically correct, handles edge cases (zero interest, 1-month tenure, 30-year home loans)
- **Audit trail** — every state change logged with actor, timestamp, and remarks
- **Branch-scoped security** — officers/managers only see their own branch's data
- **TDD discipline** — test suite as a deliverable

---

## Tech Stack

### Backend
- Java 17, Spring Boot 3.2
- Spring Security + JWT (access + refresh tokens)
- Spring Data JPA + PostgreSQL 15
- Flyway (database migrations & versioning)
- JUnit 5 + Mockito (unit tests)
- Lombok

### Frontend
- React 18 + Vite
- Tailwind CSS
- React Router (role-based routing)
- Axios with JWT interceptor
- Recharts (analytics dashboards)
- Lucide React (icons)

### Infrastructure
- Docker + Docker Compose (3 services: postgres, backend, frontend)

---

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Node.js 18+ (for local frontend development)

### Option 1: Docker Compose (Recommended)
```bash
git clone https://github.com/adi-13-ya/loan-management-system.git
cd loan-management-system
docker-compose up --build
```
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Option 2: Local Development

**1. Start PostgreSQL**
```bash
docker-compose up postgres
```

**2. Start Backend**
```bash
cd backend
./mvnw spring-boot:run
```

**3. Start Frontend**
```bash
cd frontend
npm install
npm run dev
```
- Frontend: http://localhost:5173

---

## Demo Accounts

All seeded accounts use password: `password123`

| Role | Email | Branch |
|------|-------|--------|
| Admin | admin@idfc.com | — |
| Manager | rajesh.manager@idfc.com | IDFC Koramangala |
| Manager | priya.manager@idfc.com | IDFC Whitefield |
| Manager | amit.manager@idfc.com | IDFC Indiranagar |
| Officer | sneha.officer@idfc.com | IDFC Koramangala |
| Officer | ananya.officer@idfc.com | IDFC Whitefield |
| Customer | rahul@gmail.com | — |
| Customer | deepa@gmail.com | — |

---

## API Endpoints

```
POST   /api/auth/register              — Customer registration
POST   /api/auth/login                 — Login (returns JWT)

POST   /api/loans                      — Create loan application
POST   /api/loans/{id}/submit          — Submit for review
GET    /api/loans/{id}                 — Get loan details
GET    /api/loans/my                   — Customer's applications
GET    /api/loans/branch/{id}/queue    — Branch review queue
POST   /api/loans/{id}/review          — Officer picks up for review
POST   /api/loans/{id}/forward         — Forward to manager
POST   /api/loans/{id}/approve         — Manager approves (→ disburse + EMI)
POST   /api/loans/{id}/reject          — Reject with reason

POST   /api/loans/{id}/documents       — Upload document
GET    /api/loans/{id}/documents       — List documents
GET    /api/loans/{id}/emi-schedule    — EMI schedule
POST   /api/emi/{emiId}/mark-paid      — Simulate repayment

GET    /api/admin/branches             — All branches
POST   /api/admin/branches             — Create branch
GET    /api/admin/users                — All users
PUT    /api/admin/users/{id}/toggle-active — Activate/deactivate
GET    /api/admin/loans                — All loans (filterable)
GET    /api/admin/audit-log            — Full audit trail
GET    /api/admin/analytics            — System-wide analytics
```

---

## Project Structure

```
loan-management-system/
├── docker-compose.yml
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/bankloan/
│       │   ├── config/          — Security, CORS configuration
│       │   ├── controller/      — REST API controllers
│       │   ├── dto/             — Request/Response DTOs
│       │   ├── exception/       — Global exception handling
│       │   ├── model/entity/    — JPA entities
│       │   ├── model/enums/     — Role, LoanStatus, DocType
│       │   ├── repository/      — Spring Data JPA repositories
│       │   ├── security/        — JWT utilities, auth filter
│       │   ├── service/         — Business logic services
│       │   └── util/            — EMI calculator, state validator
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/    — Flyway SQL migrations
│       └── test/                — JUnit 5 + Mockito tests
├── frontend/
│   ├── package.json
│   ├── Dockerfile
│   └── src/
│       ├── api/                 — Axios instance with JWT interceptor
│       ├── components/          — Shared UI components
│       ├── context/             — Auth context provider
│       └── pages/               — Role-specific dashboard pages
└── GUIDE.md
```

---

## Testing

```bash
cd backend
./mvnw test
```

### Test Coverage
- **EmiCalculatorTest** — EMI formula, edge cases, schedule sum verification
- **LoanStatusTransitionValidatorTest** — Valid/invalid transitions, terminal states
- **LoanApplicationServiceTest** — Full workflow with mocks
- **AuditLogServiceTest** — Audit record creation verification

---

## EMI Calculation

Uses the standard **reducing-balance** formula:
```
EMI = [P × R × (1+R)^N] / [(1+R)^N − 1]
```
Where P = Principal, R = Monthly rate, N = Tenure in months.

The last installment is automatically adjusted to zero out the outstanding balance exactly.

---

## License

This project is built as an internship demo for IDFC Bank.
