# Test Scenarios
**Owner**: @QA-Tester (preparation)
**Source**: `docs/specs/TASK-XXX.md` (Acceptance Criteria by @BA)

This directory holds test scenario documents prepared **before** development begins.

## Naming Convention
```
TASK-{number}-scenarios.md
```

## Contents Per File
Each scenario file maps every Acceptance Criteria from the BA spec to concrete test cases:
- Happy path
- Edge cases
- Negative/failure cases
- Security test cases (input injection, auth bypass attempts)

## Test Case ID Format
```
TC-{task_number}-{sequential_number}
TC-001-01: Register with valid data (Happy Path)
TC-001-02: Register with duplicate email (Negative)
TC-001-03: Register with weak password (Validation)
```

## Status Tags
`⏳ PENDING` | `✅ PASS` | `❌ FAIL` | `⏭️ SKIPPED`
