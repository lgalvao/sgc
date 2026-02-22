# Pragmatic Simplification Strategy for SGC

> **Context:** This system will be used by **at most 5-10 simultaneous users** inside an intranet.
> **Diagnosis:** The current architecture is designed for high-scale, distributed teams (Microservices patterns, Hexagonal/Clean Architecture, strict isolation), leading to excessive "glue code" managing non-existent complexity.
> **Goal:** This document outlines a strategy to aggressively simplify the codebase, reducing maintenance overhead and cognitive load, while maintaining application stability and security.

---

## 1. Eliminate Fragmentation in `subprocesso`

The `subprocesso` module (`backend/src/main/java/sgc/subprocesso`) is fragmented into unnecessarily small pieces.

**Current State:**
- **4 Controllers:** `SubprocessoCadastroController`, `SubprocessoCrudController`, `SubprocessoMapaController`, `SubprocessoValidacaoController`.
- **1 Facade:** `SubprocessoFacade` (injects 11 services just to delegate calls).
- **Multiple Workflow Services:** `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`.
- **Helper Services:** `SubprocessoBaseService`, `SubprocessoContextoService`, `SubprocessoFactory`, etc.

**Action Plan:**
1.  **Delete `SubprocessoFacade`:** Controllers should inject Services directly.
2.  **Merge Controllers:** Consolidate into a single `SubprocessoController` handling all Subprocess-related HTTP routing.
3.  **Merge Workflow Logic:** Combine `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, and `SubprocessoAdminWorkflowService` into a single cohesive `SubprocessoWorkflowService`.
4.  **Simplify CRUD:** Merge basic CRUD operations into `SubprocessoService`.

## 2. Replace Custom Access Control with Spring Security

The system currently implements a custom security framework in `sgc.seguranca.acesso` which mimics Spring Security ACLs using bespoke code.

**Current State:**
- **Custom Framework:** `AccessControlService`, `AccessAuditService`, `AbstractAccessPolicy`.
- **Policy Implementations:** `SubprocessoAccessPolicy` (complex rule map), `ProcessoAccessPolicy`, etc.
- **Manual Checks:** Code like `accessControlService.verificarPermissao(usuario, Acao.EDITAR, subprocesso)` scattered throughout services.

**Action Plan:**
1.  **Delete Custom Framework:** Remove `AccessControlService`, `AccessAuditService`, and all `*AccessPolicy` classes.
2.  **Use Standard Annotations:** Leverage Spring Security's `@PreAuthorize` with SpEL.
    - Example: `@PreAuthorize("@subprocessoSecurity.canEdit(#id, principal)")`
3.  **Centralize Logic:** Create a simple `SubprocessoSecurity` bean with straightforward methods (`canEdit`, `canView`) that implement the business rules without the overhead of a policy engine.

## 3. Remove Excessive Testing Infrastructure

For a small internal app, maintaining contract tests (Pact) adds significant overhead with little benefit since the frontend and backend are likely deployed together or by the same team.

**Action Plan:**
1.  **Remove Pact (Backend):** Remove `au.com.dius.pact` dependencies from `backend/build.gradle.kts`.
2.  **Remove Pact (Frontend):** Delete `frontend/pact/` directory and remove `@pact-foundation/pact` from `frontend/package.json`.
3.  **Focus on Integration Tests:** Rely on `@SpringBootTest` for backend logic and standard E2E tests (Playwright) for critical user flows.

## 4. Simplify Data Transport (DTOs & Mappers)

The frontend manually maps backend DTOs to internal models, duplicating logic and types.

**Current State:**
- **Manual Mappers:** `frontend/src/mappers/subprocessos.ts`, `frontend/src/mappers/mapas.ts`, etc.
- **Defensive Coding:** Extensive null checks in mappers for fields that should be guaranteed by the backend.

**Action Plan:**
1.  **Use Generated Types:** Configure `openapi-typescript` to generate strict TypeScript types from the backend OpenAPI definition.
2.  **Remove Manual Mappers:** Delete `frontend/src/mappers/*.ts` and use the generated types directly in Vue components.
3.  **Simplify Backend DTOs:** Use Java `record` types for DTOs to reduce boilerplate. Ensure DTOs structure matches UI needs to minimize frontend transformation logic.

## 5. Development Workflow Guidelines

- **Stop arbitrarily splitting logic:** If a feature belongs to "Subprocess Management", it goes in the primary Subprocess service. Avoid creating new classes for single methods.
- **Leverage Integration Tests:** Avoid writing 50 lines of Mockito setup to test a 3-line service passthrough method. Focus on `@SpringBootTest` or `@DataJpaTest` to verify the code actually works against a real (or in-memory) database schema.
