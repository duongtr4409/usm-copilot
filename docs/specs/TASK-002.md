# Feature Spec: Academy Management System (AMS)
**Task ID**: TASK-002  
**Author**: @BA  
**Date**: 2026-04-21  
**Status**: DRAFT

---

## Overview
A concise specification for AMS covering hierarchical Organization Unit management (including special handling for `Lớp`/Class), Staff/Teacher records, role-based access (ADMIN / STUDENT), atomic student enrollment (profile + account + enrollment), news/training (rich text), and the Student Portal. This spec transforms the functional requirements in [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md#L1-L400) into testable user stories, acceptance criteria, implementation notes, and a PMO acceptance checklist.

## Scope and Assumptions
- Scope: Admin Portal (organization/unit management, staff, accounts, RBAC, news/training), Student Portal (login, profile, grades, news), transactional class enrollment flow.
- Assumptions:
  - `OrganizationUnit.type` is one of: Phòng, Ban, Văn Phòng, Khoa, Trung Tâm, Lớp.
  - Only `Lớp` units accept students (enrollment); other unit types are administrative.
  - RBAC includes `ADMIN` and `STUDENT` roles only for user-facing functionality. System-level staff records exist separately.
  - Student account delivery (email/SMS) is out-of-scope; creation and initial password generation are in-scope.
  - UI will present organization units as a Tree-view and forms described below.
- Reference: source requirement file [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md#L1-L400).

## Personas
- **ADMIN**: Manages org units, staff records, accounts, news, and performs student enrollment operations.
- **Staff / Giảng viên (Teacher)**: Has an HR record, belongs to one administrative unit, may be assigned as class-in-charge or to many classes.
- **Student**: Enrolled into classes; uses Student Portal to view profile, grades, and news.
- **PMO / Reviewer**: Validates the spec and marks TASK-002 complete.

---

## User Stories

### US-001: Manage Organization Units (Tree CRUD)
**As a** ADMIN,  
**I want** to create, edit, delete and view organization units in a tree (parent-child) structure,  
**so that** the academy's hierarchical structure is represented and maintained in the system.

**Priority**: HIGH  
**Estimate**: 3

#### Acceptance Criteria
```gherkin
Scenario: AC-1.1 - Create unit
  Given I am logged in as ADMIN
  When I create an OrganizationUnit with Name, Type and optional Parent
  Then the new unit is persisted and appears under Parent in the Tree-view

Scenario: AC-1.2 - Edit unit
  Given an existing OrganizationUnit
  When ADMIN updates name/type/parent
  Then the changes are saved and reflected in the Tree-view

Scenario: AC-1.3 - Delete unit without children or assignments
  Given an OrganizationUnit with no child units and no assigned Staff or Classes
  When ADMIN deletes the unit
  Then the unit is removed from the tree and DB

Scenario: AC-1.4 - Prevent unsafe delete
  Given an OrganizationUnit that has child units or assigned Staff/Classes
  When ADMIN attempts to delete
  Then the system rejects the delete with a clear error and HTTP 4xx
```

Suggested test scenarios:
- Create a 3-level subtree and verify Tree-view order.
- Attempt delete on node with children (expect failure).
- Move a node (change parent) and verify subtree preserved.

---

### US-002: Unit Types and Class Semantics
**As a** ADMIN,  
**I want** `Lớp` (Class) to have special behaviors and constraints,  
**so that** classes support student enrollment, class leaders, and class-in-charge assignments while administrative units enforce staff placement rules.

**Priority**: HIGH  
**Estimate**: 3

#### Acceptance Criteria
```gherkin
Scenario: AC-2.1 - Class accepts students
  Given an OrganizationUnit of Type 'Lớp'
  When ADMIN views class details
  Then the UI exposes "Add Student" action and student list for that class

Scenario: AC-2.2 - Ban cán sự selection constraint
  Given students in a class
  When ADMIN sets "Ban cán sự"
  Then only students belonging to that class can be selected

Scenario: AC-2.3 - Người phụ trách selection constraint
  Given Staff records exist
  When ADMIN assigns Người phụ trách for a class
  Then only Staff (not Students) can be selected; Staff may be assigned to multiple classes

Scenario: AC-2.4 - Staff.unit_id validation
  Given a Staff record
  When ADMIN assigns `unit_id`
  Then the system rejects assignment if the referenced OrganizationUnit.type == 'Lớp'
```

Suggested test scenarios:
- Verify Add Student is only available for units typed 'Lớp'.
- Try to set Staff.unit_id to a Class (expect validation error).
- Assign same teacher to two classes and verify both assignments.

---

### US-003: Manage Staff (Cán bộ & Giảng viên) CRUD
**As a** ADMIN,  
**I want** to create, update and move Staff records and their administrative unit assignment,  
**so that** staff HR data is maintained and staff are correctly associated with non-class units.

**Priority**: HIGH  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-3.1 - Create Staff in admin unit
  Given ADMIN creates Staff with required fields and an admin OrganizationUnit
  When save is submitted
  Then Staff is persisted and `unit_id` points to a non-'Lớp' unit

Scenario: AC-3.2 - Update staff unit (transfer)
  Given an existing Staff assigned to OrgUnit A
  When ADMIN updates Staff.unit_id to OrgUnit B (non-'Lớp')
  Then Staff record reflects the new `unit_id` and previous relationships are updated

Scenario: AC-3.3 - Prevent Staff assigned to Class
  Given ADMIN attempts to set Staff.unit_id to a unit where type == 'Lớp'
  When save is submitted
  Then the system rejects with validation error and HTTP 4xx
```

Suggested test scenarios:
- Create staff and assign to Khoa (success).
- Transfer staff to another admin unit (verify change history).
- Attempt to assign staff to Class (expect rejection).

---

### US-004: Accounts & RBAC (ADMIN / STUDENT)
**As a** ADMIN,  
**I want** system accounts and role-based access with `ADMIN` and `STUDENT` roles,  
**so that** access is limited to authorized actions and students only see their portal.

**Priority**: HIGH  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-4.1 - ADMIN full access
  Given an ADMIN account
  When authenticated
  Then ADMIN can manage org units, staff, news, and perform AddStudentToClass

Scenario: AC-4.2 - STUDENT limited access
  Given a STUDENT account
  When authenticated
  Then the user can only view personal profile, grades, and public news pages

Scenario: AC-4.3 - Auto-role assignment on enrollment
  Given a student added to a class via AddStudentToClass
  When the transaction succeeds
  Then a UserAccount with role STUDENT exists and is linked to the StudentProfile
```

Suggested test scenarios:
- Verify ADMIN can access admin-only routes (200) and student cannot (403).
- Verify newly enrolled student can login and sees student portal only.

---

### US-005: Add Student To Class (Atomic Enrollment)
**As a** ADMIN,  
**I want** the "Add Student to Class" action to atomically create the StudentProfile, the Student UserAccount, and the Enrollment record,  
**so that** partial data is never persisted and student's account is ready immediately.

**Priority**: HIGH  
**Estimate**: 5

#### Acceptance Criteria
```gherkin
Scenario: AC-5.1 - Successful atomic enrollment
  Given a valid Class and student data (including Username and initial Password)
  When ADMIN performs AddStudentToClass
  Then a DB transaction creates: UserAccount(username, role=STUDENT), StudentProfile linked to the account, and Enrollment linking StudentProfile to Class
  And all three records are committed together

Scenario: AC-5.2 - Rollback on failure
  Given a simulated failure during student profile creation (or any step)
  When AddStudentToClass is executed
  Then no UserAccount, StudentProfile or Enrollment are persisted (all steps rolled back)
  And an error is returned to the caller

Scenario: AC-5.3 - Username uniqueness
  Given Username already exists
  When ADMIN attempts AddStudentToClass with same Username
  Then operation fails with validation error; no partial entities exist
```

Suggested test scenarios:
- Happy path: Add student and verify account + profile + enrollment exist and linked.
- Induce failure at step 2 (e.g., DB constraint) and verify rollback.
- Attempt duplicate username (expect rejection, no side-effects).

UI requirement (form fields): `Username`, `Initial Password`, and student profile fields (name, dob, contact, etc.) must be present and validated.

---

### US-006: Class Roles — Ban cán sự & Người phụ trách
**As a** ADMIN,  
**I want** to assign Class Leader (Ban cán sự) from class students and set Người phụ trách from Staff,  
**so that** each class has clear leadership and a responsible teacher.

**Priority**: MEDIUM  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-6.1 - Assign Ban cán sự from class students
  Given a class with students
  When ADMIN assigns Ban cán sự
  Then selected user must be a StudentProfile belonging to that class

Scenario: AC-6.2 - Assign Người phụ trách from Staff
  Given Staff exist
  When ADMIN assigns Người phụ trách for a class
  Then the selected staff is linked to the class via ClassAssignment (many-to-many allowed)
```

Suggested test scenarios:
- Assign and unassign a class leader; verify constraint safeguards.
- Assign the same teacher to multiple classes.

---

### US-007: ClassAssignment (Staff ↔ Class) M:N Management
**As a** ADMIN,  
**I want** to assign Staff to multiple classes and maintain a ClassAssignment table,  
**so that** teachers can be responsible for multiple classes while staff keep a single administrative unit.

**Priority**: MEDIUM  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-7.1 - Create ClassAssignment
  Given a Staff and a Class
  When ADMIN assigns Staff to Class
  Then a ClassAssignment record (staff_id, class_unit_id) is created

Scenario: AC-7.2 - Prevent invalid assignments
  Given a Staff assigned to a Class where Staff.unit_id = NULL
  When ADMIN tries to assign Staff to Class
  Then system allows assignment (staff may be unassigned to admin unit) but Staff.unit_id must not point to a 'Lớp'
```

Suggested test scenarios:
- Create, list, and remove ClassAssignment entries.
- Verify staff can have multiple class assignments.

---

### US-008: News & Training (Rich Text Editor)
**As a** ADMIN,  
**I want** to create, edit, publish and manage news/training posts via an RTE,  
**so that** academy news and training content is available to students.

**Priority**: MEDIUM  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-8.1 - Create & publish news
  Given ADMIN creates a news post with title and body (RTE)
  When ADMIN publishes the post
  Then post is visible in Student Portal 'News' list and detail page

Scenario: AC-8.2 - Draft and unpublish
  Given a saved draft or published post
  When ADMIN unpublishes or saves a draft
  Then visibility is restricted accordingly
```

Suggested test scenarios:
- Create draft, publish, and verify student sees it.
- Edit published post and verify version/update timestamp.

---

### US-009: Student Portal — Profile, Grades, News
**As a** STUDENT,  
**I want** to log in and view my profile, grades, and the news feed,  
**so that** I can access personal records and academy announcements.

**Priority**: HIGH  
**Estimate**: 3

#### Acceptance Criteria
```gherkin
Scenario: AC-9.1 - Student login
  Given a Student account created by AddStudentToClass
  When the student logs in with provided credentials
  Then authentication succeeds and Student Portal is shown

Scenario: AC-9.2 - View profile & grades
  Given an authenticated STUDENT
  When the student navigates to Profile or Grades
  Then the system displays the student's profile page and personal grade list only

Scenario: AC-9.3 - Access news
  Given published news
  When student opens News page
  Then published posts are visible and readable
```

Suggested test scenarios:
- Login with sample student credentials and verify pages.
- Verify students cannot access ADMIN endpoints.

---

### US-010: UI — Tree-view and AddStudentToClass Form
**As a** ADMIN,  
**I want** a Tree-view UI for OrganizationUnit and a well-structured "Add Student to Class" form,  
**so that** administration tasks are fast and consistent.

**Priority**: MEDIUM  
**Estimate**: 2

#### Acceptance Criteria
```gherkin
Scenario: AC-10.1 - Tree-view UI
  Given OrganizationUnit data
  When ADMIN opens Organization page
  Then units render as a collapsible Tree-view and support Add/Edit/Delete actions contextual to node type

Scenario: AC-10.2 - AddStudentToClass form fields
  Given ADMIN opens AddStudentToClass modal for a Class
  Then form contains: Username (required), Initial Password (required), Full Name, DOB, Contact, and Submit/Cancel controls
```

Suggested test scenarios:
- Verify Tree-expand/collapse and contextual menu actions.
- Validate required fields and form client-side validations.

---

## Data Model (Conceptual)
High-level entities and relationships (conceptual):
- OrganizationUnit (id, name, type, parent_id -> OrganizationUnit)
  - type in {Phòng,Ban,Văn Phòng,Khoa,Trung Tâm,Lớp}
- Staff (id, name, staff_code, unit_id -> OrganizationUnit where type != 'Lớp')
- UserAccount (id, username, password_hash, role {ADMIN|STUDENT}, linked_staff_id?, linked_student_id?)
- StudentProfile (id, user_account_id -> UserAccount, full_name, dob, contact, other metadata)
- Enrollment (id, student_profile_id -> StudentProfile, class_unit_id -> OrganizationUnit(type='Lớp'), enrolled_at, status)
- ClassAssignment (id, staff_id -> Staff, class_unit_id -> OrganizationUnit(type='Lớp'))
- NewsPost (id, title, body, author_id -> UserAccount, status {draft,published}, published_at)
Relationships:
- OrganizationUnit self-references parent_id (tree).
- Staff.unit_id references OrganizationUnit restricted to admin types.
- StudentProfile ↔ UserAccount 1:1.
- Enrollment links StudentProfile to Class (OrganizationUnit with type 'Lớp') — one student can have many enrollments (history).
- ClassAssignment implements Staff-to-Class many-to-many.

---

## Non-Functional Requirements
- **Transactions**: `AddStudentToClass` must run inside a single DB transaction; all DB writes for account/profile/enrollment commit or rollback atomically.
- **Validation Rules**:
  - `Staff.unit_id` must reference an OrganizationUnit where type != 'Lớp'. Validation enforced at DB and application layers.
  - `OrganizationUnit.type` must be one of the enumerated set.
  - `UserAccount.username` unique; password minimum policy: 8 chars, at least one letter and one number (configurable).
- **RBAC**:
  - Enforce role checks at API and UI layers.
  - `ADMIN` can perform all management actions.
  - `STUDENT` role limited to read-only views of own data and public news.
- **UI Expectations**:
  - Tree-view for OrganizationUnit management with contextual actions (Add/Edit/Delete).
  - `AddStudentToClass` form must include `Username` and `Initial Password` fields and server-side validation.
  - Use accessible components (ARIA) and responsive layouts.
- **Performance & Scalability**:
  - Organization Tree operations must handle up to N units (N to be defined later) with lazy-loading for deep trees.
  - Enrollment transaction latency target: synchronous commit < 1s under typical load (TBD).
- **Audit & Logging**:
  - Record audit trail for create/update/delete operations on OrganizationUnit, Staff, Enrollment, and NewsPost (actor, timestamp, change).
- **Security**:
  - Passwords stored hashed (bcrypt/argon2).
  - Rate limiting for authentication endpoints.
  - Sensitive operations require ADMIN role and CSRF protection for web forms.

---

## Implementation Notes
Required/Recommended DB tables (minimal list):
- OrganizationUnit (id PK, name, type, parent_id FK, metadata)
- Staff (id PK, full_name, staff_code, unit_id FK (not 'Lớp'), contact info, active_flag)
- UserAccount (id PK, username, password_hash, role, staff_id FK nullable, student_profile_id FK nullable, created_at)
- StudentProfile (id PK, user_account_id FK, full_name, dob, contact, metadata)
- Enrollment (id PK, student_profile_id FK, class_unit_id FK (OrganizationUnit.type='Lớp'), enrolled_at, status)
- ClassAssignment (id PK, staff_id FK, class_unit_id FK)
- NewsPost (id PK, title, body_html, author_id FK, status, published_at)
- AuditLog (id PK, entity_type, entity_id, action, actor_id, timestamp, details)

Recommended constraints and indexes:
- Unique index on `UserAccount.username`.
- FK constraints for unit_id and parent_id.
- DB CHECK constraint or application-side enforcement for OrganizationUnit.type values.
- Constraint to prevent `Staff.unit_id` referencing a 'Lớp' (application-level + DB trigger/check).

Recommended workflow/sequence for AddStudentToClass (transactional)
1. Validate input (class_unit_id exists and type == 'Lớp'; username format/uniqueness).
2. Begin DB transaction.
3. Insert `UserAccount` with `username`, `password_hash`, `role = STUDENT`.
4. Insert `StudentProfile` linked to `UserAccount`.
5. Insert `Enrollment` linking `StudentProfile` to `class_unit_id` with enrolled_at.
6. (Optional) Update class roster cache, send provisioning event to async queue (email/SMS) outside transaction.
7. Commit transaction.
8. On any failure in steps 3–5, rollback and return failure details.

Notes:
- Generate initial password securely and allow ADMIN override.
- Email/SMS notification must be asynchronous (outside the enrollment transaction).
- For bulk student imports, pipeline should use batched transactions with idempotency keys.

---

## Open Questions
| # | Question | Owner | Status |
|---|---|---:|---|
| 1 | Who may trigger AddStudentToClass — ADMIN only or also self-registration/teacher? | @PMO | OPEN |
| 2 | Username generation rules and initial password delivery mechanism (email/SMS/manual)? | @PMO | OPEN |
| 3 | Delete semantics for OrganizationUnit with children — cascade or forbid? | @PMO | OPEN |
| 4 | Required retention for audit logs and PII policies? | @PMO | OPEN |

---

## Suggested Test Scenarios (cross-story)
- Full transactional test: start AddStudentToClass; simulate failure during Enrollment; verify no account/profile exist.
- RBAC matrix: attempt protected calls with STUDENT account (expect 403).
- UI integration: Tree-view create/move/delete lifecycle + form validations.
- News flow: create draft → publish → student visibility.

---

## Acceptance Checklist (for PMO)
- [ ] Spec file saved as `docs/specs/TASK-002.md`.
- [ ] All required user stories present and cover features in [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md#L1-L400).
- [ ] Each story has concrete, testable acceptance criteria (Gherkin) and suggested test scenarios.
- [ ] Implementation Notes list required DB tables and relationships.
- [ ] AddStudentToClass transactional workflow documented.
- [ ] Validation rules (Staff.unit_id not pointing to 'Lớp') and UI field requirements (Username, Initial Password) documented.
- [ ] Open questions noted and assigned to PMO for decisions.

---

## Next Steps (recommended)
- PMO to resolve Open Questions (username rules, who may enroll students, delete semantics).
- Tech Lead to review DB schema and add detailed ERD and migration plan.
- UX to provide Tree-view and AddStudentToClass form wireframes matching the UI expectations.

---
