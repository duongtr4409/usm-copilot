# Test Execution Reports
**Owner**: @QA-Tester
**Trigger**: Produced after `@Code-Review` issues `LGTM`

This directory holds QA test execution reports for each completed feature.

## Naming Convention
```
TASK-{number}-report.md
```

## Report Sections
- **Executive Summary**: Verdict (`ALL_TESTS_PASSED` / `TESTS_FAILED`)
- **Coverage table**: Backend unit, backend integration, frontend unit, E2E
- **Failed test list**: File, error, root cause, assigned fix
- **Performance baseline**: API response times recorded during testing

## Verdict → Gate Action

| Verdict | Next Action |
|---|---|
| `ALL_TESTS_PASSED` | @PMO → marks DONE, triggers @DevOps-Engine |
| `TESTS_FAILED` | @PMO → returns to @Java-BE or @React-FE to fix |
