# Simplification Suggestions for SGC

This document outlines areas of overengineering, excessive complexity, and fragmentation in the SGC codebase, with concrete suggestions for simplification tailored to a small intranet application (5-10 users).

## Guiding Principle
For a system with minimal concurrency and a small user base, **simplicity and maintainability are paramount**. Patterns designed for large-scale distributed systems (microservices, complex event sourcing, strict layered architecture with DTO/Model separation) often introduce unnecessary overhead and cognitive load without providing their intended benefits.

## Backend Analysis (`sgc.subprocesso` as a Case Study)

The `subprocesso` module exhibits significant overengineering:

### 1. Excessive Service Fragmentation
**Observation:** Logic is scattered across multiple services: `SubprocessoCrudService`, `SubprocessoValidacaoService`, `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`, `SubprocessoAjusteMapaService`, `SubprocessoAtividadeService`, `SubprocessoContextoService`, `SubprocessoPermissaoCalculator`.
**Impact:** Exploring a simple feature requires jumping between 5-10 files. It’s hard to understand the full lifecycle of a subprocess.
**Suggestion:**
-   **Consolidate into `SubprocessoService`**: Merge `Crud`, `Validacao`, `Contexto`, and `Atividade` logic into a single service.
-   **Unify Workflow Logic**: Combine `CadastroWorkflow`, `MapaWorkflow`, and `AdminWorkflow` into a single `SubprocessoWorkflowService` (or even into the main service if the logic is just status updates).
-   **Remove `SubprocessoPermissaoCalculator`**: Move permission logic into the service methods themselves or use simple `@PreAuthorize` annotations.

### 2. Unnecessary Facade Pattern
**Observation:** `SubprocessoFacade` merely delegates calls to other services without adding significant logic.
**Impact:** Adds an extra layer of indirection, making code navigation harder and increasing boilerplate.
**Suggestion:**
-   **Remove `SubprocessoFacade`**: Inject the consolidated `SubprocessoService` directly into the Controller.

### 3. Complex Security Implementation
**Observation:** The `sgc.seguranca.acesso` package implements a complex Policy-based access control system (`AccessControlService`, `*AccessPolicy`, `AccessAuditService`).
**Impact:** Adding a new permission check requires creating a Policy class, updating the Service, and potentially the Audit service.
**Suggestion:**
-   **Simplify to RBAC in Service**: Use standard Spring Security `@PreAuthorize("hasRole('ADMIN')")` or simple checks within the service method (e.g., `if (!user.isAdmin() && !subprocesso.isOwner(user)) throw new AccessDeniedException()`).
-   **Remove separate Audit Service**: Standard logging or a simple aspect is sufficient for auditing 5-10 users.

### 4. Redundant DTO/Entity Mappings
**Observation:** Extensive mapping between Entities and DTOs, often with identical structures.
**Impact:** Adding a field requires updating the Entity, DTO, and Mapper.
**Suggestion:**
-   **Use Entities directly for simple CRUD**: For a small internal app, exposing entities (carefully, avoiding circular references with `@JsonIgnore`) is often acceptable and drastically reduces boilerplate.
-   **Use DTOs only when necessary**: Only create DTOs for complex aggregations or specific API contracts that differ significantly from the domain model.

## Frontend Analysis

### 1. Fragmented State Management (Pinia Stores)
**Observation:** Stores are highly granular: `useProcessosStore`, `useSubprocessosStore`, `useMapasStore`, `useAtividadesStore`.
**Impact:** Managing related state (e.g., a Process containing Subprocesses) becomes difficult, leading to synchronization issues.
**Suggestion:**
-   **Consolidate by Feature**: Create a `useProcessoFeatureStore` that manages the state for a Process and its related Subprocesses and Activities. This reflects how the user interacts with the data (viewing a process and its details).

### 2. Redundant Mappers
**Observation:** `frontend/src/mappers/` contains files that manually map API responses (DTOs) to internal "Models" that are often identical.
**Impact:** Triples the work when adding a field (DTO type, Model type, Mapper function).
**Suggestion:**
-   **Remove Mappers**: Use the API response types (DTOs) directly in the components.
-   **Use `zod` for validation (optional)**: If runtime validation is needed, use `zod` schemas that infer the TypeScript type, replacing manual interface definitions.

### 3. Service Layer Wrappers
**Observation:** `frontend/src/services/` often just wraps `axios.get/post` calls.
**Impact:** Adds boilerplate.
**Suggestion:**
-   **Call Axios directly in Stores**: For simple CRUD, calling `api.get('/subprocessos')` directly in the Pinia action is often cleaner than importing a service function.
-   **Keep Services for Complex Logic**: Only use service files for logic that is reused across multiple stores or involves complex data transformation *before* the store.

## General Architectural Recommendations

1.  **Vertical Slices over Layers**: Instead of `Controller -> Facade -> Service -> Repository`, organize code by feature (e.g., `features/subprocesso`).
2.  **KISS (Keep It Simple, Stupid)**: Avoid patterns like CQRS, Event Sourcing, or intricate Design Patterns (Strategy, Factory, Facade) unless the problem *strictly* demands it. A simple `if/else` is often better than a Strategy pattern for 3 cases.
3.  **Monolithic Service**: Don't be afraid of a "God Service" for a domain if it's cohesive. A 500-line `SubprocessoService` is easier to read than 10 files of 50 lines each.

## Immediate Action Items

1.  Delete `SubprocessoFacade` and refactor `SubprocessoCadastroController` to use `SubprocessoCrudService` (or the consolidated service) directly.
2.  Merge `SubprocessoValidacaoService`, `SubprocessoContextoService`, and `SubprocessoAtividadeService` into `SubprocessoService`.
3.  Remove `frontend/src/mappers/` and update stores to use DTO types directly.
