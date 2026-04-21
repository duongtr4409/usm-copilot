# API Contract Files
**Owner**: @Tech-lead (design) + @Java-BE (implementation)
**Consumer**: @React-FE

This directory holds modular API specification files for individual features.
The root `API_SPEC.yaml` in the repository root is the **master aggregated spec**.

## Naming Convention
```
{feature}-api-v{N}.yaml
```

Examples:
- `auth-api-v1.yaml`
- `user-api-v1.yaml`
- `order-api-v1.yaml`

## OpenAPI Version
All files use **OpenAPI 3.0.3**.

## Workflow
1. `@Tech-lead` creates `{feature}-api-v1.yaml` in this directory.
2. `@Java-BE` implements accordingly and updates path details if needed.
3. `@React-FE` reads this file to generate TypeScript types via `openapi-typescript`.
4. `@Tech-lead` merges the module spec into root `API_SPEC.yaml`.

## Code Generation (@React-FE)
```bash
# Generate TypeScript types from spec
npx openapi-typescript ./docs/api/auth-api-v1.yaml -o src/features/auth/types/api.gen.ts
```
