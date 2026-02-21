# Radical Simplification Suggestions for SGC

> **Verified Context (v3.0.0):** This document is based on an analysis of the codebase at `backend/src/main/java/sgc`. The fragmentation and complexity issues are confirmed, though some specific implementation details (like interface usage) were previously misstated.
> **Constraint:** This system will be used by **at most 5-10 simultaneous users** inside an intranet.
> **Diagnosis:** The current architecture is designed for high-scale, distributed teams (Microservices patterns, Hexagonal/Clean Architecture, strict isolation).
> **Verdict:** 80% of the code is "glue code" managing complexity that doesn't exist.

This document outlines a strategy to aggressively simplify the codebase, reducing maintenance overhead and cognitive load.

## 1. The "One Service, One Controller" Rule (Backend)

The `subprocesso` module (`backend/src/main/java/sgc/subprocesso`) is the prime example of fragmentation. It currently has:
- **4 Controllers:** `SubprocessoCadastroController`, `SubprocessoCrudController`, `SubprocessoMapaController`, `SubprocessoValidacaoController`.
- **1 Facade:** `SubprocessoFacade` (which injects 11 other services).
- **10+ Services:** `SubprocessoCrudService`, `SubprocessoValidacaoService`, `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`, `SubprocessoAjusteMapaService`, `SubprocessoAtividadeService`, `SubprocessoContextoService`, `SubprocessoPermissaoCalculator`, `SubprocessoFactory`, etc.

**Action:** Merge them.
- **Single Controller:** `SubprocessoController`. It handles all HTTP requests related to a Subprocess.
- **Single Service:** `SubprocessoService`. It contains all business logic. Yes, it might be 2000 lines long. *That is fine.* A single file is easier to navigate than 15 files with circular dependencies and injection overhead.
- **Delete the Facade:** It adds no value. Inject the Repository directly into the Service.

## 2. Eliminate the Custom "Access Control Framework"

The system implements a custom security framework in `sgc.seguranca.acesso` (`AccessControlService`, `AccessPolicy`, `AccessAuditService`, `AbstractAccessPolicy`).
This mimics Spring Security ACLs but with custom code.

**Action:** Use Standard Spring Security.
- **Delete** `AccessControlService`, `AccessPolicy`, and all `*AccessPolicy` classes (`SubprocessoAccessPolicy`, `ProcessoAccessPolicy`, etc.).
- **Use Annotations:** `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("@subprocessoSecurity.canEdit(#id, principal)")`.
- **Simple Logic:** Implement a single `SecurityService` with methods like `canEdit(user, entityId)`.
- **Trust the Framework:** Spring Security handles authentication and authorization efficiently. We don't need a custom audit layer for 5 users; standard logs are sufficient.

## 3. Pragmatic Data Access (The "Anti-DTO" Pattern)

The system currently enforces a strict `Entity -> DTO -> Response` mapping, often with 3-4 layers of object copying.

**Action:** Use Entities for Read/Write where possible.
- **Return Entities:** For internal intranet apps, returning the JPA Entity (with `@JsonIgnore` on recursive fields) is perfectly acceptable and saves writing hundreds of DTOs.
- **MapStruct is Optional:** If you need a DTO, write a simple constructor or a static factory method. You don't need a complex Mapper interface and generated implementation for every single object.
- **Repo in Controller:** For simple "Get by ID" or "List All" endpoints, injecting the `Repository` directly into the `Controller` is acceptable. The "Service Layer" is for *business logic*, not for passthrough.

## 4. Frontend: Trust the Backend Contract

The frontend currently re-validates and re-maps everything (`frontend/src/mappers/` contains files like `subprocessos.ts`, `mapas.ts`, `alertas.ts`).

**Action:** Delete the Mappers.
- **Use the API Types:** The backend DTOs (or Entities) are the source of truth.
- **Fail Fast:** If the backend sends `null` where a string is expected, let the UI break or show a generic error. Writing defensive code for every single field ("defensive programming") is expensive and hides bugs.
- **Consolidate Stores:** A single `useSubprocessoStore` is enough. You don't need `useSubprocessoMapaStore`, `useSubprocessoAtividadeStore`, etc. State fragmentation leads to sync bugs.

## 5. Specific Targets for Deletion

| Component | Reason | Replacement |
| :--- | :--- | :--- |
| `SubprocessoFacade` | Passthrough layer | `SubprocessoService` |
| `Subprocesso*Controller` (x4) | Arbitrary splitting | `SubprocessoController` |
| `*AccessPolicy` classes | Overengineered framework | `@PreAuthorize` + Simple Service Check |
| `AccessAuditService` | Redundant logging | Standard `slf4j` logs |
| `frontend/src/mappers/*.ts` | Redundant mapping | Direct API types |
| `ComumRepo` | Custom Wrapper | Standard `JpaRepository` |
| `ADR-001` (Facades) | Premature optimization | Direct Service Injection |
| `ADR-005` (Split Controllers) | Premature optimization | Cohesive Controllers |

## 6. Development Workflow Changes

- **Consolidate Logic:** Stop creating new service classes for every small feature (e.g., `SubprocessoAjusteMapaService` for just one method). Keep logic related to the Subprocess entity in `SubprocessoService`.
- **Stop mocking everything:** For a small app, integration tests (`@SpringBootTest`) are more valuable and easier to write than mocking 15 dependencies.
