# Pragmatic Simplification Strategy for SGC

> **Context:** This system will be used by **at most 5-10 simultaneous users** inside an intranet.
> **Diagnosis:** The current architecture is designed for high-scale, distributed teams (Microservices patterns, Hexagonal/Clean Architecture, strict isolation), leading to excessive "glue code" managing non-existent complexity.
> **Goal:** This document outlines a strategy to aggressively simplify the codebase, reducing maintenance overhead and cognitive load, while maintaining application stability and security.

---

## 1. Eliminate Fragmentation in `subprocesso`

The `subprocesso` module (`backend/src/main/java/sgc/subprocesso`) is fragmented into unnecessarily small pieces, mirroring a microservices architecture within a monolith.

**Current State:**
- **4 Controllers:** `SubprocessoCadastroController`, `SubprocessoCrudController`, `SubprocessoMapaController`, `SubprocessoValidacaoController`.
- **1 Facade:** `SubprocessoFacade` (injects 8+ services just to delegate calls).
- **Multiple Workflow Services:** `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`.
- **Helper Services:** `SubprocessoAjusteMapaService`, `SubprocessoAtividadeService`, `SubprocessoContextoService`.
- **Factory:** `SubprocessoFactory` (contains business logic for creation that belongs in a service).

**Action Plan:**
1.  **Delete `SubprocessoFacade`:** Controllers should inject Services directly. The facade adds no value.
2.  **Merge Controllers:** Consolidate all 4 controllers into a single `SubprocessoController` handling all Subprocess-related HTTP routing.
3.  **Merge Workflow Logic:** Combine `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`, and `SubprocessoFactory` into a single cohesive `SubprocessoWorkflowService` (or just `SubprocessoService`).
4.  **Simplify CRUD:** Merge basic CRUD operations into `SubprocessoService`.
5.  **Remove Helper Services:** If a helper service has only 1-2 methods used by the main service, merge it back into the main service unless it has a distinct, reusable responsibility.

## 2. Replace Custom Access Control with Spring Security

The system currently implements a custom security framework in `sgc.seguranca.acesso` which mimics Spring Security ACLs using bespoke code. While `SubprocessoSecurity` has been modernized, the rest of the application lags behind.

**Current State:**
- **Legacy Framework:** `AccessControlService`, `AccessAuditService`, `AbstractAccessPolicy`, `ProcessoAccessPolicy`, `MapaAccessPolicy`, `AtividadeAccessPolicy`.
- **Modern Implementation:** `SubprocessoSecurity` (good direction, but check for over-complexity in rule maps).

**Action Plan:**
1.  **Delete Custom Framework:** Remove `AccessControlService`, `AccessAuditService`, and all `*AccessPolicy` classes in `sgc.seguranca.acesso`.
2.  **Standardize on `*Security` Beans:** Create simple security beans (e.g., `ProcessoSecurity`, `MapaSecurity`) similar to `SubprocessoSecurity` but simpler.
3.  **Use Standard Annotations:** Leverage Spring Security's `@PreAuthorize` with SpEL.
    - Example: `@PreAuthorize("@subprocessoSecurity.canEdit(#id)")`
4.  **Simplify Rules:** For 5-10 users, complex state-based rules might be necessary for business logic, but ensure the *implementation* is simple. Avoid "Rule Engine" patterns if a simple `if/else` block suffices.

## 3. Remove Excessive Testing Infrastructure

For a small internal app, maintaining contract tests (Pact) adds significant overhead with little benefit since the frontend and backend are likely deployed together or by the same team.

**Action Plan:**
1.  **Remove Pact (Backend):** Remove `au.com.dius.pact` dependencies from `backend/build.gradle.kts` and delete any Pact test classes.
2.  **Remove Pact (Frontend):** Delete `frontend/pact/` directory and remove `@pact-foundation/pact` from `frontend/package.json`.
3.  **Focus on Integration Tests:** Rely on `@SpringBootTest` for backend logic and standard E2E tests (Playwright) for critical user flows. Delete complex unit tests that just mock 10 dependencies to test a pass-through method.

## 4. Simplify Data Transport (DTOs & Mappers)

The frontend manually maps backend DTOs to internal models, duplicating logic and types. This is a high-maintenance area.

**Current State:**
- **Manual Mappers:** `frontend/src/mappers/subprocessos.ts`, `frontend/src/mappers/mapas.ts`, etc.
- **Defensive Coding:** Extensive null checks in mappers (e.g., `dto.subprocesso || dto || {}`) suggest uncertainty about the API contract.
- **Fragmented Services:** `subprocessoService.ts` and `mapaService.ts` share responsibilities and endpoints.

**Action Plan:**
1.  **Use Generated Types:** Configure `openapi-typescript` to generate strict TypeScript types from the backend OpenAPI definition.
2.  **Remove Manual Mappers:** Delete `frontend/src/mappers/*.ts` and use the generated types directly in Vue components.
3.  **Merge Frontend Services:** Combine `subprocessoService.ts` and `mapaService.ts` into a single `processoService.ts` (or `workflowService.ts`) to match the backend simplification.
4.  **Backend-for-Frontend (BFF):** If the UI needs data in a specific format, shape the DTO in the backend to match the UI, rather than reshaping it in the frontend.

## 5. Development Workflow Guidelines

- **Stop arbitrarily splitting logic:** If a feature belongs to "Subprocess Management", it goes in the primary Subprocess service. Avoid creating new classes for single methods.
- **Leverage Integration Tests:** Avoid writing 50 lines of Mockito setup to test a 3-line service passthrough method. Focus on `@SpringBootTest` or `@DataJpaTest` to verify the code actually works against a real (or in-memory) database schema.
- **YAGNI (You Ain't Gonna Need It):** Do not implement caching, complex async queues, or distributed tracing until performance monitoring proves it is necessary. A simple synchronous method call is almost always faster and more reliable for this scale.
