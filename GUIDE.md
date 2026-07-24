# Multi-Branch Loan Management & Approval Workflow System

**Project type:** Full-stack banking domain application (no AI/ML)
**Purpose:** Internship project demo for IDFC Bank — showcases role-based workflows, EMI/finance logic, approval state machines, and TDD discipline
**Timeline:** 3–4 day MVP build
**Approach:** Test-Driven Development (TDD) — write failing tests first, then implement

---

## 1. Project Overview

A system that digitizes the end-to-end loan lifecycle across multiple bank branches:

1. A **Customer** applies for a loan online, uploading supporting documents.
2. A **Branch Officer** reviews the application, verifies documents, and either forwards it or rejects it.
3. A **Branch Manager** gives final approval or rejection (multi-level approval).
4. On approval, the loan is **disbursed**, and a **reducing-balance EMI schedule** is auto-generated.
5. An **Admin** has cross-branch visibility into all loans, branches, and users.

The core engineering showcase is:
- A clean **approval state machine** (no illegal transitions allowed)
- **Reducing-balance EMI math**, calculated correctly and tested rigorously
- **Role-based access control** (RBAC) — each role sees and can do only what it should
- **Audit trail** — every state change is logged with who/when/why
- Everything built **TDD-first**, so the test suite itself is a deliverable to show interviewers

---

## 2. Roles & Personas

| Role | Description | Key Actions |
|---|---|---|
| **Customer** | Applies for loans, tracks status, views EMI schedule, uploads documents | Apply, upload docs, view status, view EMI schedule, make repayments (simulated) |
| **Branch Officer** | First-level reviewer at a specific branch | View assigned applications, verify documents, add remarks, forward to manager or reject |
| **Branch Manager** | Final approver for their branch | View forwarded applications, approve/reject, trigger disbursement |
| **Admin** | Cross-branch oversight | View all branches, all loans, all users; manage branch/employee records; view system-wide audit logs and analytics |

---

## 3. Core Use Cases

### Customer
- Register/login
- Fill loan application (loan type, amount, tenure, purpose, income details)
- Upload documents (ID proof, income proof — PDF/JPG/PNG)
- Track application status in real time (Draft → Submitted → Under Review → Approved/Rejected → Disbursed)
- View full EMI schedule once disbursed (month-by-month: principal, interest, balance)
- View repayment history (simulate marking an EMI as "paid")

### Branch Officer
- View queue of applications assigned to their branch
- Open an application: view customer details + uploaded documents
- Add verification remarks
- Forward to Branch Manager **or** Reject with reason (customer sees rejection reason)

### Branch Manager
- View queue of officer-forwarded applications for their branch
- Approve (triggers disbursement + EMI schedule generation) or Reject with reason
- View branch-level loan portfolio (total disbursed, active loans, default risk overview — simple aggregates, no ML)

### Admin
- View/manage all branches (create branch, assign officers/managers to branch)
- View/manage all users (activate/deactivate accounts)
- View all loans across all branches, filterable by status/branch/date
- View system-wide audit log (every approval, rejection, disbursement — who did what, when)

---

## 4. Loan Application Workflow (State Machine)

```
DRAFT → SUBMITTED → UNDER_REVIEW → FORWARDED_TO_MANAGER → APPROVED → DISBURSED
                          ↓                    ↓
                      REJECTED             REJECTED
```

**Rules to enforce (and test):**
- A loan can only move forward, never skip a state (e.g. `DRAFT` cannot go directly to `APPROVED`).
- Once `REJECTED` or `DISBURSED`, the loan is terminal — no further transitions allowed.
- Only a Branch Officer can move `SUBMITTED` → `UNDER_REVIEW` → `FORWARDED_TO_MANAGER`/`REJECTED`.
- Only a Branch Manager can move `FORWARDED_TO_MANAGER` → `APPROVED`/`REJECTED`.
- Disbursement (`APPROVED` → `DISBURSED`) auto-generates the EMI schedule as part of the same transaction.
- Every transition is recorded in an **audit log** table (loan_id, from_state, to_state, actor_id, timestamp, remarks).

---

## 5. EMI Calculation Logic (Reducing Balance Method)

Use the standard reducing-balance EMI formula:

```
EMI = [P × R × (1+R)^N] / [(1+R)^N − 1]

Where:
P = Principal loan amount
R = Monthly interest rate (annual rate / 12 / 100)
N = Number of monthly installments (tenure in months)
```

**Schedule generation:** for each month, compute:
- Interest component = outstanding balance × R
- Principal component = EMI − interest component
- New outstanding balance = old balance − principal component

**Edge cases to test:**
- Very short tenure (1–2 months)
- Zero-interest edge case (R = 0, EMI = P/N)
- Rounding: final month's principal component should exactly zero out the balance (adjust last EMI by a few paise if needed due to rounding)
- Large principal / long tenure (30-year home-loan-scale numbers) for numerical stability

---

## 6. Data Model (Core Entities)

- **User**: id, name, email, password_hash, role (CUSTOMER/OFFICER/MANAGER/ADMIN), branch_id (nullable for customer/admin), active
- **Branch**: id, name, code, city, manager_id
- **LoanApplication**: id, customer_id, branch_id, loan_type, principal_amount, annual_interest_rate, tenure_months, purpose, status, current_officer_id, current_manager_id, created_at, updated_at
- **Document**: id, loan_application_id, doc_type (ID_PROOF/INCOME_PROOF/OTHER), file_path, uploaded_at
- **ApprovalAuditLog**: id, loan_application_id, actor_id, from_status, to_status, remarks, timestamp
- **EmiSchedule**: id, loan_application_id, installment_number, due_date, emi_amount, principal_component, interest_component, outstanding_balance, is_paid
- **LoanType** (seed/reference data): id, name (e.g. Home Loan, Personal Loan, Auto Loan, Education Loan), default_interest_rate_range, max_tenure_months

---

## 7. Sample / Seed Dataset

Seed the database with:
- **3 branches**: e.g. "IDFC Koramangala", "IDFC Whitefield", "IDFC Indiranagar" (Bengaluru-themed since that's realistic)
- **4 loan types**: Home Loan (8.5–9.5%), Personal Loan (11–14%), Auto Loan (9–11%), Education Loan (9–10%)
- **1 Admin**, **3 Branch Managers** (one per branch), **6 Branch Officers** (2 per branch)
- **10–15 sample customers** with varied loan applications across different states (some still Draft, some Under Review, a few Approved/Disbursed with generated EMI schedules, a couple Rejected) — this makes the demo look "alive" immediately without manually creating everything live in front of interviewers.

---

## 8. Tech Stack

**Backend:**
- Java 17+, Spring Boot 3.x
- Spring Web (REST APIs)
- Spring Security + JWT (authentication/authorization, role-based endpoint guarding)
- Spring Data JPA + PostgreSQL
- Flyway (DB migrations/versioning)
- Spring State Machine *or* a hand-rolled state transition validator (see note below)
- JUnit 5 + Mockito (unit tests) + Testcontainers (integration tests against real Postgres)
- Lombok (optional, reduces boilerplate)

**Frontend:**
- React 18 + Vite
- Tailwind CSS + shadcn/ui components
- React Router (role-based routes)
- Axios (API calls) with an interceptor for JWT attach + refresh
- React Query (or simple custom hooks) for data fetching/caching
- Recharts (simple aggregate charts for Manager/Admin dashboards)

**Infra:**
- Docker + docker-compose (postgres, backend, frontend as 3 services)
- `.env` files for config (DB creds, JWT secret)

> **Note on Spring State Machine:** For a 3–4 day MVP, prefer a **hand-rolled state transition validator** (a simple `Map<Status, Set<Status>>` of allowed transitions + a service method that throws `IllegalStateTransitionException` on violation). It's fully unit-testable in isolation and far faster to build than wiring the actual Spring State Machine library. Mention in the interview that you evaluated Spring State Machine but chose a simpler explicit approach for clarity and speed — that's a legitimate engineering trade-off story.

---

## 9. Backend Project Structure

```
src/main/java/com/bankloan/
├── config/          → SecurityConfig, JwtConfig, CorsConfig
├── controller/       → AuthController, LoanController, DocumentController,
│                       BranchController, AdminController, EmiController
├── service/          → LoanApplicationService, EmiCalculationService,
│                       ApprovalWorkflowService, DocumentService, AuditLogService
├── repository/       → JPA repositories per entity
├── model/entity/     → User, Branch, LoanApplication, Document, EmiSchedule, ApprovalAuditLog
├── model/enums/      → LoanStatus, Role, DocType
├── dto/              → Request/response DTOs (never expose entities directly)
├── security/         → JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
├── exception/        → GlobalExceptionHandler, custom exceptions
└── util/             → EmiCalculator (pure function, heavily unit tested), Clock bean (for testable dates)
```

---

## 10. Key REST API Endpoints (high level)

```
POST   /api/auth/register
POST   /api/auth/login

POST   /api/loans                          (customer: create draft application)
POST   /api/loans/{id}/submit              (customer: submit for review)
GET    /api/loans/{id}                     (all roles, RBAC-scoped)
GET    /api/loans/my                       (customer: their own applications)
GET    /api/loans/branch/{branchId}/queue  (officer/manager: their review queue)
POST   /api/loans/{id}/forward             (officer: move to manager)
POST   /api/loans/{id}/reject              (officer/manager: reject + reason)
POST   /api/loans/{id}/approve             (manager: approve → triggers disbursement + EMI generation)

POST   /api/loans/{id}/documents           (customer: upload document)
GET    /api/loans/{id}/documents           (officer/manager: view documents)

GET    /api/loans/{id}/emi-schedule        (customer/officer/manager: view schedule)
POST   /api/emi/{emiId}/mark-paid          (customer: simulate repayment)

GET    /api/admin/branches                 (admin: CRUD branches)
GET    /api/admin/users                    (admin: manage users)
GET    /api/admin/loans                    (admin: all loans, filterable)
GET    /api/admin/audit-log                (admin: full audit trail)
```

---

## 11. Dashboards (Frontend)

### Customer Dashboard
- "My Applications" list with status badges (color-coded)
- "Apply for New Loan" form (loan type dropdown, amount, tenure slider, purpose text)
- Document upload widget per application
- EMI schedule table (once disbursed) + simple progress bar (paid vs remaining)

### Branch Officer Dashboard
- Review queue (table: customer name, loan type, amount, submitted date)
- Application detail view: customer info + documents (viewable inline) + remarks field + Forward/Reject buttons

### Branch Manager Dashboard
- Forwarded-applications queue
- Application detail view with officer's remarks visible + Approve/Reject buttons
- Branch portfolio summary: total loans, total disbursed amount, active vs closed, simple bar chart by loan type (Recharts)

### Admin Dashboard
- Branch management (list/create branches, assign managers)
- User management (list/activate/deactivate)
- Cross-branch loan table (filterable by branch/status/date range)
- Audit log viewer (searchable table: actor, action, timestamp)
- System-wide summary charts (total disbursed by branch, loan type distribution)

---

## 12. TDD Plan — Suggested Order of Test-First Development

Write these test files **before** their implementation, in this order:

1. **`EmiCalculatorTest`** — pure function tests, no Spring context needed
   - Standard case (e.g. ₹10,00,000 @ 9% for 60 months → assert exact EMI value)
   - Zero-interest edge case
   - Full schedule generation sums to principal exactly (no rounding drift)
   - 1-month tenure edge case
2. **`LoanStatusTransitionValidatorTest`** — pure logic, no Spring context
   - Valid transitions succeed
   - Invalid/skipped transitions throw exception
   - Terminal states (REJECTED, DISBURSED) reject any further transition
3. **`LoanApplicationServiceTest`** (Mockito-mocked repository)
   - Submit moves DRAFT → SUBMITTED
   - Officer forward moves UNDER_REVIEW → FORWARDED_TO_MANAGER
   - Manager approve triggers EMI schedule generation (mock `EmiCalculationService`, verify it's called with correct params)
   - Reject at any stage records reason and stops workflow
4. **`ApprovalAuditLogTest`** — verify every transition writes exactly one audit record with correct actor/timestamp
5. **`AuthControllerIntegrationTest`** (Testcontainers + real Postgres) — register/login, JWT issued, protected endpoint rejects missing/invalid token
6. **`LoanControllerIntegrationTest`** (Testcontainers) — full flow: customer creates → submits → officer forwards → manager approves → EMI schedule exists in DB
7. **RBAC tests** — a Customer token cannot hit `/api/loans/branch/{id}/queue`; an Officer token cannot call `/approve`; etc.

This order lets you demo a growing, meaningful test suite from day one — great to screen-share in an interview.

---

## 13. Security Considerations
- Passwords hashed with BCrypt
- JWT with short-lived access token + refresh token
- Every loan/document endpoint checks not just role but **ownership/branch-scoping** (an Officer from Branch A must not see Branch B's queue — test this explicitly)
- File upload validation: restrict file types (PDF/JPG/PNG) and size (e.g. max 5MB)
- Store uploaded files outside the web root; serve via an authenticated endpoint, never a static public path

---

## 14. Suggested 4-Day Build Plan

**Day 1 — Backend core + TDD foundation**
- Set up Spring Boot project, Docker Compose (Postgres), Flyway migrations for schema
- Build `EmiCalculatorTest` + `EmiCalculator` (pure logic, fully tested)
- Build `LoanStatusTransitionValidatorTest` + validator
- Set up JWT auth (register/login) with tests

**Day 2 — Workflow + persistence**
- `LoanApplicationService` with full TDD cycle (submit/forward/reject/approve)
- Audit log wiring
- Document upload endpoint + storage
- Integration tests with Testcontainers for the full loan lifecycle

**Day 3 — Frontend**
- React app scaffold (Vite + Tailwind + shadcn/ui), auth pages, role-based routing
- Customer dashboard: apply form, application list, document upload, EMI schedule view
- Officer & Manager dashboards: queues, detail view, approve/reject/forward actions

**Day 4 — Admin dashboard, polish, seed data, demo prep**
- Admin dashboard: branches, users, all-loans view, audit log viewer
- Seed script with realistic sample data (Section 7)
- Polish UI states (loading, empty states, error toasts)
- Write a clean README with setup instructions + architecture diagram
- Do a full dry-run demo end-to-end (customer applies → officer forwards → manager approves → EMI schedule appears) and time it

---

## 15. What to Explicitly Call Out in the Interview
- The **state machine design** and why illegal transitions are structurally impossible, not just "checked" — walk through the validator test cases.
- The **EMI math correctness** — show the rounding-edge-case test, it signals attention to real financial detail.
- **RBAC + branch-scoping tests** — proves you thought about a multi-tenant-like access model, which is exactly how core banking systems segment data.
- The **audit log** — mention this maps to real regulatory/compliance requirements (every decision must be traceable).
- Your **TDD discipline** — show the git history or test-first commits if possible, and be ready to write one small new test live if asked.

---

## 16. Stretch Goals (only if Day 4 finishes early)
- Email/SMS "notification" simulation (just log to console/DB, no real provider) on status change
- Simple PDF export of the sanctioned loan letter
- Pagination + search/filter on all list views
- Dark mode (shadcn/ui makes this nearly free)

---

## Instructions for Claude Code

Read this entire document before writing any code. Follow strictly test-driven development: for every piece of business logic (EMI calculation, state transitions, RBAC checks), write the failing test first, then implement the minimum code to pass it, then refactor. Set up the Docker Compose environment first so Postgres is available for integration tests from day one. Build in the order specified in Section 14. Ask clarifying questions if any requirement in this spec is ambiguous before making assumptions that would require a rebuild later.