# System Architecture
**Owner**: @Tech-lead
**Version**: 0.1.0 (Initial Template)
**Last Updated**: 2026-04-21
**Status**: TEMPLATE — Will be populated when the first feature is designed

---

## Overview

> *This document will be updated by @Tech-lead after @BA completes each feature specification.*
> *It serves as the single source of truth for system design.*

---

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                               │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │             React 18 + TypeScript (Vite)                │     │
│  │   React Router | React Query | Zustand | shadcn/ui      │     │
│  └────────────────────────┬────────────────────────────────┘     │
└───────────────────────────┼──────────────────────────────────────┘
                            │ HTTPS / REST API
┌───────────────────────────┼──────────────────────────────────────┐
│                     API GATEWAY LAYER                             │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │                  Nginx (Reverse Proxy)                  │     │
│  │             SSL Termination | Rate Limiting             │     │
│  └────────────────────────┬────────────────────────────────┘     │
└───────────────────────────┼──────────────────────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────────────┐
│                    APPLICATION LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │            Spring Boot 3.2 + Java 21                    │     │
│  │  Spring Security | Spring Data JPA | Spring Validation  │     │
│  │  SpringDoc OpenAPI | MapStruct | Flyway                 │     │
│  └──────────┬──────────────────────────┬───────────────────┘     │
└─────────────┼──────────────────────────┼───────────────────────── ┘
              │                          │
┌─────────────┼──────┐     ┌────────────┼───────────────────────────┐
│  DATA LAYER │      │     │   CACHE    │                            │
│  ┌──────────▼─────┐│     │  ┌─────────▼──────┐                   │
│  │  PostgreSQL 15 ││     │  │   Redis 7.x    │                   │
│  │  Primary DB    ││     │  │  Sessions/JWT  │                   │
│  └────────────────┘│     │  └────────────────┘                   │
└────────────────────┘     └────────────────────────────────────────┘
```

---

## Technology Stack

| Layer | Technology | Version | Justification |
|---|---|---|---|
| Backend Language | Java | 21 (LTS) | Virtual threads, records, pattern matching |
| Backend Framework | Spring Boot | 3.2.x | Production-proven, rich ecosystem |
| Frontend Language | TypeScript | 5.x | Type safety, better DX |
| Frontend Framework | React | 18.x | Component model, concurrent features |
| Frontend Build | Vite | 5.x | Fast HMR, optimized builds |
| Primary Database | PostgreSQL | 15.x | ACID, JSONB, full-text search |
| Cache | Redis | 7.x | Fast in-memory store for sessions/tokens |
| DB Migration | Flyway | 10.x | Version-controlled schema changes |
| ORM | Hibernate / Spring Data JPA | 6.x | Standard JPA implementation |
| Auth | Spring Security + JWT | - | Stateless, scalable authentication |
| API Spec | OpenAPI 3.0 (SpringDoc) | - | Auto-generated, contract-first |
| Container | Docker | - | Consistent environments |
| CI/CD | GitHub Actions | - | Native GitHub integration |

---

## Module Map

> *Modules will be added here as features are designed by @Tech-lead.*

```
com.usm/
├── common/         # Cross-cutting: BaseEntity, GlobalExceptionHandler, ApiResponse
├── security/       # JWT, SecurityConfig, AuthFilter
├── user/           # User management module
└── {feature}/      # Each feature gets its own module package
```

---

## Database Schema Overview

> *Tables will be documented here as @DB-Admin creates migrations.*

### Naming Conventions
- All tables use `snake_case` (plural form)
- All tables include standard audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`
- Primary keys are `UUID` type generated with `gen_random_uuid()`

---

## Security Architecture

### Authentication Flow
```
Client → POST /api/v1/auth/login
       → Spring Security → UserDetailsService
       → BCrypt password verification
       → JWT token generated (15 min access + 7 day refresh)
       → Return {accessToken, refreshToken}

Client → GET /api/v1/resource (Authorization: Bearer {accessToken})
       → JwtAuthFilter → validate → SecurityContextHolder
       ---ARCHITECTURE.md---
       # System Architecture: AMS (Academic Management System)
       **Task ID**: TASK-002  
       **Author**: @Tech-lead  
       **Version**: 1.0.0  
       **Date**: 2026-04-21

       ---

       ## Architecture Diagram
       ```mermaid
       flowchart LR
              Client[React SPA] -->|HTTPS| APIGW[API Gateway / LB (NGINX / Spring Cloud Gateway)]
              APIGW --> Backend[Backend Cluster (Spring Boot 3.2.x)]
              Backend -->|JDBC| DB[(PostgreSQL 15)]
              Backend -->|Cache| Redis[(Redis 7)]
              Backend -->|Outbox / Streams| Broker[(Redis Streams / Kafka)]
              Broker -->|consume| Notifier[Notification Worker (Spring Boot)]
              Notifier -->|email/push| 3rdParty[SMTP / FCM / Webhooks]
       ```

       ---

       ## Technology Stack
       | Layer | Technology | Version | Justification |
       |---|---:|---|---|
       | Backend | Spring Boot | 3.2.x | Standard, mature ecosystem; integrates with Spring Security, Data, Transaction management |
       | Frontend | React | 18.x | SPA for admin/student consoles |
       | Database | PostgreSQL | 15.x | ACID, JSONB, extensions (ltree), strong indexing |
       | Cache | Redis | 7.x | fast reads, session/refresh token store, short-lived caches |
       | Messaging | Redis Streams / Kafka | - | Outbox pattern and async notifications |
       | Auth | Spring Security + JWT | - | Stateless API auth with role claims |
       | Migrations | Flyway | - | Versioned DB migrations |

       ---

       ## High-level Summary
       - Client: React SPA (Admin, Staff, Student views). Communicates via HTTPS to API Gateway.
       - API Gateway: TLS termination, routing, rate-limiting, basic auth checks, request size limits.
       - Backend: Spring Boot services structured into domain modules (see Module Map). Services expose REST API /api/v1 and are stateless; scale horizontally.
       - DB: Postgres holds canonical data. Use uuid PKs, JSONB for flexible contact/metadata.
       - Cache & Messaging: Redis for caching & refresh tokens; Redis Streams or Kafka used with an Outbox table for reliable async processing (notifications, audit sync).
       - Notifications: Notification workers consume outbox/stream and deliver email/push/webhooks.

       ---

       ## Module Map (backend modules & responsibilities)
       - org
              - Responsibilities: OrganizationUnit tree management, tree queries (materialized path / ltree), org-level scoping.
              - Exposes: CRUD for org-units, org tree endpoints, org-scoped queries.
       - staff
              - Responsibilities: staff records, staff <-> account mapping, staff role assignments.
              - Exposes: staff CRUD, staff search, staff-org assignment.
       - auth
              - Responsibilities: login, token issuance, refresh tokens, password policy, account locks, role management.
              - Exposes: /auth/login, token refresh, account management hooks.
       - student
              - Responsibilities: student profiles, student account lifecycle, guardians, student-specific queries.
              - Exposes: profile CRUD, import/export hooks.
       - class
              - Responsibilities: classes (Class entity), class capacity, enrollment lifecycle (atomic AddStudentToClass).
              - Exposes: class CRUD, class rosters, enrollment endpoints.
       - news
              - Responsibilities: news posts, scoping to org-units, pub/preview/publish workflow.
              - Exposes: news CRUD, publish/unpublish, visibility rules.
       - common
              - Responsibilities: shared DTOs, error handling, audit, outbox, utilities, security helpers.
              - Exposes: reusable components, event/outbox table definition.

       ---

       ## Component Design (brief)
       - API Layer (controllers)
              - Validate input, return standardized error shapes, map DTO -> service.
       - Service Layer (transactional)
              - Business rules, orchestration, transactions.
              - Use Spring `@Transactional` for atomic workflows.
       - DAO / Repository
              - JPA/Hibernate with explicit native queries for tree operations; use DTO projections for read-heavy endpoints.
       - Outbox/Event
              - Write domain events to an `outbox` table inside the same transaction as domain changes; worker reads and publishes to Redis Streams/Kafka.
       - Workers
              - Notification worker, integration worker — idempotent consumers that mark events as processed.

       ---

       ## Data Model Summary / ERD Notes
       - OrganizationUnit
              - id: UUID PK
              - name: varchar
              - code: varchar (uniq within parent)
              - parent_id: UUID FK -> OrganizationUnit.id (nullable)
              - path: text / ltree (indexed) — materialized path for fast subtree queries
              - metadata: JSONB
              - created_at, updated_at
              - Indexes: (path), (parent_id), (name)
              - Notes: Self-referential tree; prefer materialized path or Postgres ltree for subtree queries.

       - UserAccount
              - id: UUID PK
              - username: varchar UNIQUE
              - email: varchar UNIQUE
              - password_hash: varchar
              - roles: text[] or join table user_roles
              - status: enum (ACTIVE, INACTIVE, LOCKED)
              - last_login, created_at, updated_at
              - Notes: Passwords hashed with Argon2/Bcrypt; username uniqueness must be enforced at DB to return 409 on conflict.

       - Staff
              - id: UUID PK
              - account_id: UUID FK -> UserAccount.id
              - staff_number: varchar UNIQUE
              - org_unit_id: UUID FK -> OrganizationUnit.id
              - position: varchar
              - contact: JSONB
              - active: boolean
              - created_at, updated_at

       - StudentProfile
              - id: UUID PK
              - account_id: UUID FK -> UserAccount.id
              - student_number: varchar UNIQUE
              - first_name, last_name, dob, grade
              - guardian_info: JSONB
              - created_at, updated_at

       - Class
              - id: UUID PK
              - code: varchar UNIQUE (or unique per org)
              - title: varchar
              - org_unit_id: UUID FK -> OrganizationUnit.id
              - capacity: integer
              - teacher_staff_id: UUID FK -> Staff.id
              - term: varchar
              - created_at, updated_at

       - Enrollment
              - id: UUID PK
              - class_id: UUID FK -> Class.id
              - student_profile_id: UUID FK -> StudentProfile.id
              - status: enum (ENROLLED, PENDING, DROPPED)
              - enrolled_at, enrolled_by (staff id)
              - Constraints: unique (class_id, student_profile_id)
              - Indexes: (class_id), (student_profile_id)

       - NewsPost
              - id: UUID PK
              - title: varchar
              - summary: text
              - content: text
              - author_account_id: UUID FK -> UserAccount.id
              - scope_org_unit_id: UUID FK -> OrganizationUnit.id (nullable)
              - published_at: timestamp
              - is_published: boolean
              - created_at, updated_at

       Notes on relationships:
       - UserAccount is the authentication anchor. Staff and StudentProfile reference it (1:1).
       - Enrollment links StudentProfile <-> Class.
       - OrganizationUnit scopes many entities; access control checks often include an org_unit_id match or ancestor check.

       ---

       ## Sequence (textual) — AddStudentToClass (atomic workflow)
       Goal: create user account (if new), student profile, and enrollment in a single atomic operation; publish notification post-commit.

       1. Client (ADMIN/CLASS_ADMIN) sends POST /api/v1/classes/{classId}/students with payload:
               - username, initialPassword, profile {firstName,lastName,dob,...}
       2. Controller validates input and forwards to ClassService.addStudentToClass(...)
       3. Service begins DB transaction:
               a. SELECT class FOR UPDATE: verify capacity and status.
               b. SELECT user_account WHERE username = :username.
                            - If exists: abort with 409 CONFLICT (username already exists).
               c. INSERT into user_account (hash password) -> user_id.
               d. INSERT into student_profile (account_id = user_id) -> student_profile_id.
               e. INSERT into enrollment (class_id, student_profile_id) with status=ENROLLED.
               f. Increment class occupancy (or recalc with count).
               g. INSERT into outbox table (payload: EnrollmentCreated event + links) — outbox row written within same transaction.
       4. Commit DB transaction (all domain writes + outbox row commit together).
       5. Post-commit: Outbox worker polls/consumes outbox -> publish to Broker (Redis Streams/Kafka) and mark outbox row processed.
       6. Notification worker consumes the published EnrollmentCreated event and sends email/push to student/guardian and optional webhook to external systems (async).
       7. Controller returns 201 Created with student_profile_id and HATEOAS links.

       Key guarantees:
       - Uniqueness enforced at DB to ensure 409 when username pre-exists.
       - Outbox pattern ensures notifications are only published after commit (no lost notifications).
       - Transaction rollback on any DB error prevents partial state.

       Mermaid sequence (textual):
       ```mermaid
       sequenceDiagram
              participant Client
              participant API as API Controller
              participant Service
              participant DB
              participant OutboxWorker
              participant Notifier
              Client->>API: POST /classes/{id}/students
              API->>Service: addStudentToClass(payload)
              Service->>DB: BEGIN TX
              Service->>DB: SELECT class FOR UPDATE
              Service->>DB: CHECK username uniqueness
              alt username exists
                     DB-->>Service: conflict -> rollback
                     Service-->>API: 409 Conflict
              else create flow
                     DB->>DB: INSERT user_account
                     DB->>DB: INSERT student_profile
                     DB->>DB: INSERT enrollment
                     DB->>DB: INSERT outbox(event)
                     DB->>Service: COMMIT
                     Service-->>API: 201 Created
                     OutboxWorker->>DB: poll outbox
                     OutboxWorker->>Broker: publish event
                     Notifier->>Broker: consume EnrollmentCreated
                     Notifier->>3rdParty: send email/push
              end
       ```

       ---

       ## Migration Plan (Flyway)
       Initial migrations (ordered):
       - V1__extensions_and_base.sql
              - Enable extensions: pgcrypto, ltree (if used), and create schema.
       - V2__user_accounts_and_roles.sql
              - Create `user_account`, `roles`, `user_roles` tables; password, status fields; unique constraints and indexes.
       - V3__organization_units.sql
              - Create `organization_unit` table, indexes on `path` or ltree.
       - V4__staff.sql
              - Create `staff` table, staff <-> account FK, staff_number index.
       - V5__student_profiles.sql
              - Create `student_profile` table and constraints for account FK.
       - V6__classes_enrollments.sql
              - Create `class`, `enrollment` tables, unique constraints (class_id, student_profile_id), triggers if needed.
       - V7__news_posts.sql
              - Create `news_post` table, indexes on published_at, scope_org_unit_id.
       - V8__outbox.sql
              - Create `outbox` table (id, aggregate_type, aggregate_id, event_type, payload JSONB, published boolean, created_at); index for polling.
       - V9__initial_seed_roles_and_admin.sql
              - Insert system roles (ADMIN, STAFF, STUDENT, CLASS_ADMIN) and a seeded admin account (password must be rotated on first login).
       - V10__routines_constraints_indexes.sql (optional)
              - Materialized views, triggers, stored procedures for occupancy counters, and additional indexes.

       Migration notes:
       - Keep small focused files for readability and rollback clarity.
       - Backfill indexes or materialized views in separate migrations to avoid long locks.
       - Migrations should include idempotent checks where possible.

       ---

       ## Security: RBAC & JWT (TASK-013)
       **Task ID**: TASK-013
       - Overview
              - Use JWT-based authentication (RS256 keypair) and Role-Based Access Control (RBAC). Keep the model minimal and compatible with existing `roles` and `user_accounts` tables.
       - Role / Authority Model
              - Roles: `ADMIN`, `STAFF`, `STUDENT`, `CLASS_ADMIN` (scoped). Roles live in `roles` + `user_roles` join table. For scoped assignments (e.g., class-level CLASS_ADMIN), a small scoped mapping table will be used (see DB migration V10).
              - Authorities (Spring): map roles -> `ROLE_<NAME>` and also expose fine-grained permissions via authorities when needed.
       - JWT Claims (access token)
              - `sub` (UUID): user account id
              - `preferred_username` / `username`
              - `roles`: array of role names (e.g., ["ADMIN","CLASS_ADMIN"]) for quick checks
              - `org_unit_id` (optional): primary org scope for the user (UUID)
              - `iss`, `aud`, `iat`, `exp`, `jti`
              - `scp` / `scope` (optional): additional scopes if required
       - Token lifecycle & storage
              - Access token: RS256-signed JWT, TTL ≈ 15 minutes.
              - Refresh token: opaque random token (or JWT with `jti`) stored server-side (Redis) with TTL ≈ 7 days. Refresh tokens are rotated on use; old token is revoked at rotation.
              - Revocation: maintain a revocation/blacklist store (Redis) keyed by `jti` for access token revocation where necessary (rare) and by refresh token id for logout/rotation.
       - Key management
              - Use an RS256 keypair. Private key stored in a secrets manager (Vault, AWS Secrets Manager, Azure Key Vault) for production.
              - For local development, allow loading keys from `.env` (`JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`) or `./secrets/` files. Documented regeneration script should be used.
              - Public key is used by the service to verify signatures; rotate keys with key identifiers (kid) if needed.
       - Scoped roles (design)
              - Add `user_role_scopes` table (migration V10) to express assignments with `scope_type` (e.g., ORG, CLASS) and `scope_id` (UUID). This keeps existing `user_roles` intact for global roles.
              - Example: grant `CLASS_ADMIN` for class `1234-...` by inserting into `user_role_scopes`.
       - Enforcement strategy
              - API-level: use Spring Security method annotations for coarse checks, e.g. `@PreAuthorize("hasRole('ADMIN') or @aclService.isClassAdmin(principal, #classId)")` or `@PreAuthorize("hasAnyRole('ADMIN','CLASS_ADMIN')")` where acceptable.
              - Service-level: authoritative checks that validate scoped mappings and resource ownership (always query DB for sensitive decisions).
              - Frontend: hide/disable UI actions not permitted by user's roles; enforcement must not rely solely on frontend.
       - JWT verification & filters
              - `JwtAuthenticationFilter` (once per request) extracts `Authorization` header, validates signature (RS256), verifies `exp`, `aud`, `iss`, and loads `Authentication` with `GrantedAuthorities` based on `roles` claim or by resolving roles from DB if needed.
              - On token validation success, populate `SecurityContextHolder` for downstream method security.
       - Refresh flow
              - `POST /api/v1/auth/refresh` accepts a refresh token, validates it in Redis, rotates it (issue new refresh token, revoke previous), and returns new access token + refresh token.
              - On logout, remove refresh token(s) from Redis and optionally add access `jti` to short-lived blacklist.
       - Local dev vs production guidance
              - Local: use `.env` with keys and rotate tokens frequently. A helper script `scripts/generate-jwt-keys.sh` (suggested) to bootstrap keys.
              - Production: use managed secrets, enable automated rotation, do not commit keys to repo.
       - Example access token payload (JSON)
              - {
                    "sub": "550e8400-e29b-41d4-a716-446655440000",
                    "username": "alice",
                    "roles": ["CLASS_ADMIN"],
                    "org_unit_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                    "iat": 1710000000,
                    "exp": 1710000900,
                    "jti": "..."
                }
       - Example enforcement note
              - `POST /api/v1/classes/{classId}/students` MUST be protected so that callers are either global `ADMIN` or are `CLASS_ADMIN` scoped to the target `classId`. The controller should use `@PreAuthorize` for a quick guard and call a service-level ACL check to assert class-scoped membership before performing the transaction.

       ---

       ---

       ## Non-Functional Architecture & Operational Notes
       - Caching
              - Cache org tree and frequently-read static lists in Redis with TTL (e.g., 5-15m).
              - Invalidate cache on org-unit modifications.
       - Concurrency
              - Use SELECT ... FOR UPDATE when checking/updating class occupancy.
              - Rely on DB unique constraints to enforce uniqueness (username, student_number).
       - Observability
              - Centralized logging (structured JSON), metrics (Prometheus), distributed tracing (OpenTelemetry).
       - Testing
              - Unit tests for services; integration tests using Testcontainers (Postgres, Redis).
       - CI/CD
              - Migrations executed in deployment pipeline; careful zero-downtime migration strategies for large tables (create new table + backfill).

       ---

       ## ADRs (selected)
       ### ADR-001: Authentication Strategy — JWT Access + Refresh tokens
       - **Status**: ACCEPTED
       - **Context**: Need stateless API tokens; integrated with SPA.
       - **Decision**: Use JWT for access tokens (RS256) with short TTL and refresh tokens stored server-side (Redis).
       - **Consequences**: Simpler horizontal scaling; requires secure key management and refresh token handling.

       ### ADR-002: Reliable Async — Outbox Pattern
       - **Status**: ACCEPTED
       - **Context**: Must guarantee notifications are delivered after DB commit.
       - **Decision**: Write domain events to an `outbox` table inside the same DB transaction, and a worker publishes to Redis Streams/Kafka.
       - **Consequences**: Adds outbox table and worker but ensures event reliability.

       ### ADR-003: Organization Tree Implementation
       - **Status**: ACCEPTED
       - **Context**: Need efficient subtree queries and low maintenance.
       - **Decision**: Use materialized path (text + ltree extension) with indexed `path` column.
       - **Consequences**: Simple ancestor/descendant queries; requires careful path updates on moves.

       ---

       ## Next steps for implementation
       - Create Flyway migrations V1..V9 as outlined.
       - Scaffold backend modules and basic controllers for endpoints listed in API_SPEC.yaml.
       - Implement `AddStudentToClass` service with DB transaction + outbox write and unit/integration tests.
       - Implement short-term caching for org tree read endpoints.

       ---API_SPEC.yaml---
