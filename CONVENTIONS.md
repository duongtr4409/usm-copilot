# Coding Conventions & Standards
**Owner**: @Code-Review (enforcer)
**Maintained by**: @Tech-lead (updates)
**Version**: 1.0.0
**Last Updated**: 2026-04-21

> **IMPORTANT**: This document is the law. `@Code-Review` uses it as the sole reference
> to accept or reject code. Any violation is a valid reason to reject a Pull Request.

---

## Table of Contents

1. [General Principles](#1-general-principles)
2. [Java Backend Conventions](#2-java-backend-conventions)
3. [React Frontend Conventions](#3-react-frontend-conventions)
4. [Database Conventions](#4-database-conventions)
5. [API Design Conventions](#5-api-design-conventions)
6. [Git & Branch Conventions](#6-git--branch-conventions)
7. [Testing Conventions](#7-testing-conventions)
8. [Security Conventions](#8-security-conventions)
9. [Documentation Conventions](#9-documentation-conventions)

---

## 1. General Principles

### 1.1 Code Philosophy
- **SOLID**: Every class has one reason to change.
- **DRY**: Never duplicate logic. Extract shared code into utilities.
- **KISS**: Simple solutions beat clever ones.
- **YAGNI**: Do not build features that are not in the current AC.

### 1.2 Language
- All **code, comments, variable names, function names, class names** must be in **English**.
- All **API fields, database columns, JSON keys** must be in English.
- User-facing strings (UI labels, error messages) may be in Vietnamese if the product requires it, but they must come from an i18n file — never hardcoded.

### 1.3 Formatting
- Backend: Use **Google Java Style Guide** enforced by Checkstyle.
- Frontend: Use **Prettier** + **ESLint** (Airbnb config).
- Max line length: **120 characters**.
- No trailing whitespace. Files end with a single newline.

---

## 2. Java Backend Conventions

### 2.1 Naming

| Element | Convention | Example |
|---|---|---|
| Class | `PascalCase` | `UserService`, `OrderController` |
| Interface | `PascalCase` (no `I` prefix) | `UserService` (interface), `UserServiceImpl` (impl) |
| Method | `camelCase`, verb-first | `createUser()`, `findUserById()`, `isEmailExists()` |
| Variable | `camelCase`, descriptive | `userRepository`, `jwtSecret` |
| Constant | `SCREAMING_SNAKE_CASE` | `MAX_PAGE_SIZE`, `JWT_EXPIRY_SECONDS` |
| Package | `lowercase`, no underscores | `com.usm.user.service` |
| Test class | `{ClassName}Test` | `UserServiceTest` |
| Test method | `should{Expected}When{Condition}` | `shouldReturnUserWhenIdExists()` |

### 2.2 Package Structure

```
com.usm.{module}/
├── controller/     # @RestController — HTTP layer only, no business logic
├── service/        # Interfaces + Impl — all business logic here
├── repository/     # @Repository — data access only
├── entity/         # @Entity — JPA mapping
├── dto/
│   ├── request/    # Input objects (Java Records preferred)
│   └── response/   # Output objects (Java Records preferred)
├── mapper/         # MapStruct mappers only
├── exception/      # Domain-specific exceptions
└── config/         # Module-level Spring config beans
```

### 2.3 Controller Rules
```java
// ✅ CORRECT
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService; // inject by interface, not impl

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}

// ❌ WRONG — business logic in controller
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
    User user = userRepository.findById(id).orElseThrow(...); // never access repo from controller
    user.setLastAccessed(LocalDateTime.now()); // never modify entity from controller
    return ResponseEntity.ok(mapper.toResponse(user));
}
```

### 2.4 Service Rules
```java
// ✅ CORRECT — service is transactional, uses interface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // default: read-only for safety
public class UserServiceImpl implements UserService {

    @Override
    @Transactional // override to write when needed
    public UserResponse create(CreateUserRequest request) {
        validateEmailNotTaken(request.email()); // private validation
        User entity = userMapper.toEntity(request);
        User saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
```

### 2.5 Exception Handling
```java
// ✅ Standard exception hierarchy
public abstract class UsмBaseException extends RuntimeException {
    private final String errorCode;
    // ...
}
public class ResourceNotFoundException extends UsmBaseException { ... }
public class BusinessRuleViolationException extends UsmBaseException { ... }

// ✅ Global handler — never let raw exceptions reach the client
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse("NOT_FOUND", ex.getMessage());
    }
}
```

### 2.6 Forbidden Patterns

```java
// ❌ FORBIDDEN: Magic numbers
if (user.getAge() > 18) { ... }       // use: if (user.getAge() > MINIMUM_LEGAL_AGE)

// ❌ FORBIDDEN: Catching Exception/Throwable
catch (Exception e) { e.printStackTrace(); }   // use specific exception + log properly

// ❌ FORBIDDEN: System.out in production code
System.out.println("User created");   // use: log.info("User created: {}", userId)

// ❌ FORBIDDEN: Optional.get() without isPresent()
Optional<User> user = repo.findById(id);
user.get(); // use: user.orElseThrow(() -> new ResourceNotFoundException(...))

// ❌ FORBIDDEN: @Autowired field injection
@Autowired UserService service;       // use: constructor injection via @RequiredArgsConstructor
```

---

## 3. React Frontend Conventions

### 3.1 Naming

| Element | Convention | Example |
|---|---|---|
| Component | `PascalCase` | `UserProfileCard`, `LoginForm` |
| Hook | `use` + `PascalCase` | `useAuthStore`, `useGetUsers` |
| File (component) | `PascalCase.tsx` | `UserProfileCard.tsx` |
| File (hook/util) | `camelCase.ts` | `useDebounce.ts`, `formatDate.ts` |
| File (api) | `camelCase.api.ts` | `user.api.ts` |
| Type/Interface | `PascalCase` | `UserResponse`, `CreateUserInput` |
| Zod schema | `PascalCase` + `Schema` suffix | `UserResponseSchema` |
| Store | `use` + `Name` + `Store` | `useAuthStore` |
| CSS class | `kebab-case` | `user-profile-card` |

### 3.2 Component Rules

```tsx
// ✅ CORRECT — functional component, typed props, early returns
interface UserCardProps {
  userId: string;
  onDelete?: (id: string) => void;
}

export function UserCard({ userId, onDelete }: UserCardProps) {
  const { data: user, isLoading, isError } = useGetUser(userId);

  if (isLoading) return <Skeleton />;
  if (isError) return <ErrorMessage />;
  if (!user) return null; // early return, not nested conditions

  return (
    <Card>
      <p>{user.firstName} {user.lastName}</p>
      {onDelete && <Button onClick={() => onDelete(user.id)}>Delete</Button>}
    </Card>
  );
}

// ❌ WRONG — anonymous export, no type, nested logic
export default function({ userId }) { ... }
```

### 3.3 State Management Rules

```typescript
// ✅ Server state (API data) → React Query
const { data } = useQuery({ queryKey: ['users'], queryFn: fetchUsers });

// ✅ UI/Global state → Zustand
const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  setUser: (user) => set({ user }),
  logout: () => set({ user: null, token: null }),
}));

// ❌ WRONG — direct useState for server data
const [users, setUsers] = useState([]);
useEffect(() => { fetch('/api/...').then(r => r.json()).then(setUsers) }, []);
```

### 3.4 Forbidden Patterns

```tsx
// ❌ FORBIDDEN: any type
const handleClick = (e: any) => { ... }      // use proper event type: MouseEvent<HTMLButtonElement>

// ❌ FORBIDDEN: Direct DOM manipulation
document.getElementById('myEl').style.color = 'red';   // use state + className

// ❌ FORBIDDEN: Index as key in lists that change
users.map((u, i) => <UserCard key={i} />)    // use: key={u.id}

// ❌ FORBIDDEN: Hardcoded strings in components
<h1>Welcome to USM System</h1>              // use: i18n t('welcome.title') or a constant

// ❌ FORBIDDEN: console.log in committed code
console.log('debug:', user);                // remove before committing
```

---

## 4. Database Conventions

### 4.1 Naming
- Tables: `snake_case`, **plural**: `users`, `user_roles`, `order_items`
- Columns: `snake_case`: `first_name`, `created_at`, `is_active`
- Primary Keys: `pk_{table}` → `pk_users`
- Foreign Keys: `fk_{table}_{referenced_table}` → `fk_orders_users`
- Indexes: `idx_{table}_{column}` → `idx_users_email`
- Unique Constraints: `uq_{table}_{column}` → `uq_users_email`
- Check Constraints: `ck_{table}_{description}` → `ck_users_status`

### 4.2 Mandatory Rules
- All tables MUST have `id UUID DEFAULT gen_random_uuid()` as PK.
- All tables MUST have audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`.
- All migrations are **append-only** — never modify already-merged migration files.
- `NOT NULL` by default. Only allow `NULL` when the domain explicitly requires it and it is documented.
- Use `VARCHAR(n)` with explicit length. Never use unbounded `TEXT` for identifiers/codes.

### 4.3 Forbidden SQL
```sql
-- ❌ FORBIDDEN: SELECT *
SELECT * FROM users;                        -- specify exact columns

-- ❌ FORBIDDEN: Unbounded LIKE with leading wildcard (kills index)
WHERE email LIKE '%@gmail.com';             -- use pg_trgm or full-text search

-- ❌ FORBIDDEN: INSERT without column list
INSERT INTO users VALUES (...);             -- always list columns explicitly
```

---

## 5. API Design Conventions

### 5.1 URL Structure
```
Method   URL Pattern                     Description
GET      /api/v1/{resources}             List (paginated)
GET      /api/v1/{resources}/{id}        Get by ID
POST     /api/v1/{resources}             Create
PUT      /api/v1/{resources}/{id}        Full update
PATCH    /api/v1/{resources}/{id}        Partial update
DELETE   /api/v1/{resources}/{id}        Soft delete (set status=DELETED)
```

### 5.2 HTTP Status Codes
| Code | Usage |
|---|---|
| `200 OK` | Successful GET, PUT, PATCH |
| `201 Created` | Successful POST |
| `204 No Content` | Successful DELETE |
| `400 Bad Request` | Validation failure |
| `401 Unauthorized` | Missing/invalid token |
| `403 Forbidden` | Valid token, insufficient role |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate resource |
| `500 Internal Server Error` | Unhandled exception (log + alert) |

### 5.3 Response Envelope
All non-paginated responses return the **resource directly** (not wrapped):
```json
// ✅ Single resource
{ "id": "...", "name": "..." }

// ✅ Paginated list
{ "content": [...], "metadata": { "page": 0, "size": 20 } }

// ✅ Error (always use ErrorResponse schema)
{ "code": "NOT_FOUND", "message": "...", "timestamp": "..." }
```

---

## 6. Git & Branch Conventions

### 6.1 Branch Naming
```
feature/TASK-001-user-authentication
bugfix/TASK-042-fix-token-expiry
hotfix/TASK-099-security-patch
chore/update-dependencies
```

### 6.2 Commit Message Format (Conventional Commits)
```
<type>(<scope>): <short summary>

[optional body]
[optional footer: TASK-XXX]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `security`

```
feat(auth): implement JWT refresh token rotation
fix(user): prevent duplicate email registration
test(auth): add integration tests for login flow
docs(api): update API_SPEC.yaml with refresh endpoint
```

### 6.3 Pull Request Rules
- Title must match: `[TASK-XXX] Brief description`
- Must have: description, test evidence, link to TASK in TASK_BOARD
- Requires: `@Code-Review: LGTM` before merge
- Requires: All CI checks green
- Squash merge to main

---

## 7. Testing Conventions

### 7.1 Test Coverage Minimums
| Layer | Minimum |
|---|---|
| Backend (Service unit) | 80% |
| Backend (Controller integration) | All AC scenarios |
| Frontend (Component) | All interactive + form validation |
| E2E (Playwright) | All critical user journeys |

### 7.2 Test Naming
```java
// Java: should{Result}When{Condition}
@Test void shouldCreateUserWhenInputIsValid() { }
@Test void shouldThrowNotFoundWhenUserDoesNotExist() { }
@Test void shouldReturnUnauthorizedWhenTokenIsMissing() { }
```

```typescript
// TypeScript: describe + it/test
describe('LoginForm', () => {
  it('should display error when credentials are invalid', async () => { });
  it('should redirect to dashboard on successful login', async () => { });
});
```

---

## 8. Security Conventions

- **Never log sensitive data**: passwords, tokens, PII, card numbers.
- **Always validate and sanitize input** before processing.
- **Use parameterized queries** — never build SQL strings with `+` concatenation.
- **Secrets in environment variables only** — never in source code or config files committed to git.
- **Rate limiting** on all public endpoints (especially auth).
- **CORS**: Allow only specific origins in production.
- **Headers**: Set `X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`.
- **Tokens**: Short-lived access tokens (≤15 min). Refresh tokens stored in Redis, revocable.

---

## 9. Documentation Conventions

- Every public API method (Controller) must have `@Operation(summary = "...")`.
- Every Entity field that is non-obvious must have `// comment explaining business meaning`.
- README.md must be current and accurate.
- Significant architectural decisions → ADR in `ARCHITECTURE.md`.
- Complex business rules → comment in the **service** layer, not the controller.

---

*Violations found by `@Code-Review` will be filed as issues in the review report.*
*Repeated violations by the same agent → @PMO will note it in DASHBOARD.md.*
