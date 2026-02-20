# Simplification Suggestions for SGC

This document outlines areas of overengineering, excessive complexity, and fragmentation in the SGC codebase, with concrete suggestions for simplification tailored to a small intranet application (5-10 concurrent users).

## Guiding Principle: "YAGNI" (You Ain't Gonna Need It)
For a system with minimal concurrency and a small user base, **simplicity and maintainability are paramount**. Patterns designed for large-scale distributed systems (microservices, complex event sourcing, strict layered architecture with heavy DTO separation) often introduce unnecessary overhead and cognitive load without providing their intended benefits (scalability, decoupling).

## Backend Analysis (`sgc.subprocesso` as a Case Study)

The `subprocesso` module exhibits significant overengineering for a CRUD-heavy application:

### 1. Excessive Fragmentation (Dependencies Explosion)
**Observation:** Logic is scattered across multiple services and controllers. The `subprocesso` package alone has:
-   **4 Controllers:** `SubprocessoCadastroController`, `SubprocessoCrudController`, `SubprocessoMapaController`, `SubprocessoValidacaoController`.
-   **10+ Services:** `SubprocessoCrudService`, `SubprocessoValidacaoService`, `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`, `SubprocessoAjusteMapaService`, `SubprocessoAtividadeService`, `SubprocessoContextoService`, `SubprocessoPermissaoCalculator`.
-   **1 Facade:** `SubprocessoFacade` acts as a middleman, delegating calls to these services.

**Impact:** Exploring a simple feature requires jumping between 5-10 files. It’s hard to understand the full lifecycle of a subprocess. Circular dependencies are likely (e.g., Service A needs Service B, which needs Service A for validation).

**Suggestion:**
-   **Consolidate into `SubprocessoService`**: Merge `Crud`, `Validacao`, `Contexto`, and `Atividade` logic into a single service. A single 1000-line service is easier to follow than ten 100-line services with circular references.
-   **Merge Controllers**: A single `SubprocessoController` is sufficient for this domain. Separation by "phase" (Cadastro vs Mapa) is artificial at this scale.
-   **Remove `SubprocessoFacade`**: Inject the consolidated `SubprocessoService` directly into the Controller. The Facade pattern here adds no value as it just delegates 1-to-1.

### 2. Over-engineered Access Control (`sgc.seguranca.acesso`)
**Observation:** The system uses a custom Policy-based access control system (`AccessControlService`, `AbstractAccessPolicy`, `AccessAuditService`, `SubprocessoAccessPolicy`, etc.) that mimics complex frameworks like Spring Security ACL or OPA.
**Impact:** Adding a new permission check requires creating a Policy class, updating the Service, and potentially the Audit service. It adds significant cognitive load to understand *why* a user can/cannot access a resource.
**Suggestion:**
-   **Simplify to Service-Layer Checks**: Use standard Spring Security `@PreAuthorize("hasRole('ADMIN')")` or simple checks within the service method (e.g., `if (!user.isAdmin() && !subprocesso.isOwner(user)) throw new AccessDeniedException()`).
-   **Remove separate Audit Service**: Standard logging or a simple aspect is sufficient for auditing 5-10 users. The complexity of reflective policy auditing (`access-control-report.md`) is overkill.

### 3. Redundant DTO/Entity Mappings
**Observation:** Extensive mapping between Entities and DTOs, often with identical structures. The `SubprocessoDetalheResponse` flattens the `Subprocesso` entity unnecessarily, requiring complex mapping logic.
**Impact:** Adding a field requires updating the Entity, DTO, Mapper, and Frontend Mapper.
**Suggestion:**
-   **Use Entities directly for simple Read/Write**: For a small internal app, exposing entities (carefully, avoiding circular references with `@JsonIgnore` or `@JsonManagedReference`) is often acceptable and drastically reduces boilerplate.
-   **Use DTOs only when necessary**: Only create DTOs for complex aggregations or specific API contracts that differ significantly from the domain model.

## Frontend Analysis

### 1. Manual Mappers (`frontend/src/mappers/`)
**Observation:** The frontend contains files like `subprocessos.ts` that manually map API responses (DTOs) to internal "Models" that are often identical. It includes fallback logic (e.g., `unidade: dto.unidade || { nome: 'N/I' }`) that hides backend data issues.
**Impact:** Triples the work when adding a field (DTO type, Model type, Mapper function). It creates a false sense of security by "fixing" data on the client.
**Suggestion:**
-   **Remove Mappers**: Use the API response types (DTOs) directly in the components. If the backend returns `null`, handle it in the UI (e.g., `v-if="subprocesso.unidade"`), don't mask it in a mapper.
-   **Trust the Contract**: The backend should guarantee the data shape. If `unidade` is mandatory, the backend must ensure it's not null.

### 2. Fragmented State Management (Pinia Stores)
**Observation:** Stores are highly granular (e.g., separate stores for Processos, Subprocessos, Mapas, Atividades).
**Impact:** Managing related state (e.g., a Process containing Subprocesses) becomes difficult, leading to synchronization issues.
**Suggestion:**
-   **Consolidate by Feature**: Create a `useSubprocessoFeatureStore` that manages the state for a Subprocess and its related Map and Activities. This reflects how the user interacts with the data (viewing a subprocess and its details).

## General Architectural Recommendations

1.  **Vertical Slices over Layers**: Instead of `Controller -> Facade -> Service -> Repository`, organize code by feature (e.g., `features/subprocesso`).
2.  **KISS (Keep It Simple, Stupid)**: Avoid patterns like CQRS, Event Sourcing, or intricate Design Patterns (Strategy, Factory, Facade) unless the problem *strictly* demands it. A simple `if/else` is often better than a Strategy pattern for 3 cases.
3.  **Monolithic Service**: Don't be afraid of a "God Service" for a domain if it's cohesive. A single service handling all Subprocess logic is perfectly fine for this scale.

## Immediate Action Plan

1.  **Delete `SubprocessoFacade`**: Refactor controllers to use services directly.
2.  **Merge Services**: Combine `SubprocessoCrudService`, `SubprocessoValidacaoService`, and `SubprocessoContextoService` into `SubprocessoService`.
3.  **Simplify Workflow**: Move workflow logic (currently in `*WorkflowService`) into `SubprocessoService` or a single `SubprocessoStateService`.
4.  **Remove Frontend Mappers**: Delete `frontend/src/mappers/subprocessos.ts` and update components to use the API types.
