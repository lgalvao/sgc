# Simplification suggestions for SGC

Given the context of an intranet application for 5-10 simultaneous users, the current architecture presents several areas of overengineering. Following the strict project guidelines, we must prioritize simple, direct code, avoid unnecessary layers/patterns, minimize DTO mapping, maintain a cohesive monolith, and simplify the frontend state management.

Here are the key suggestions to reduce complexity and fragmentation, updated based on the current state of the codebase:

## 1. Backend: Direct repository access from Controllers for CRUD

**Issue:** Many controllers delegate simple CRUD operations to Facades and Services, adding unnecessary boilerplate.

**Suggestion:** For simple reads and basic CRUD operations without complex business logic, Controllers should inject Spring Data Repositories directly.
*   **Action:** Review controllers like `UnidadeController`, `ProcessoController` and remove intermediary Facade/Service layers for methods that simply proxy to `Repo.findById()` or `Repo.findAll()`.

## 2. Backend: Consolidation of Fragmented services and elimination of Facades

**Current State & Issue:** The domain logic was previously fragmented, and complex Facades were used to orchestrate logic.
*   **Resolved:** The fragmentation in the `Processo` domain (e.g., `ProcessoWorkflowService`, `ProcessoManutencaoService`, etc.) and the `ProcessoFacade` have already been successfully consolidated into a single, cohesive `ProcessoService.java`.
*   **Remaining Issue:** Other domains still employ unnecessary Facades that add indirection without substantial business value for a small application. For instance, `PainelFacade` and `UsuarioFacade`.

**Suggestion:** Eliminate remaining Facade classes and consolidate logic into cohesive domain services or handle simple orchestration directly in controllers.
*   **Action:** Refactor `PainelFacade` (in `sgc.processo.painel`) into a simpler `PainelService` or move its logic directly to `PainelController`.
*   **Action:** Refactor `UsuarioFacade` (in `sgc.organizacao`) by moving its logic into `UsuarioService` or `UsuarioController` directly.

## 3. Backend: Minimize DTO Mapping

**Issue:** The system uses DTOs even when entities could be returned directly for reads, leading to boilerplate mapping code.

**Suggestion:** Return JPA entities directly from Controllers for simple read operations where the API consumer needs the full object.
*   **Action:** Use `@JsonView` (which is already present in some places like `ProcessoViews.Publica`) to control serialization instead of creating dedicated DTOs for every view.
*   **Action:** Remove DTOs that have a 1:1 mapping with entities unless they are strictly necessary to hide sensitive data or aggregate complex data not easily achieved via projections.
*   **Action:** The `ProcessoDetalheBuilder` was previously flagged for removal. Its logic should be verified to ensure it is handled procedurally within `ProcessoService`.

## 4. Frontend: Removal of Pass-through Pinia stores

**Issue:** The frontend uses Pinia stores (`processos.ts`, `mapas.ts`, `subprocessos.ts` in `frontend/src/stores/`) largely as pass-throughs to cache API data fetched by Services. This adds boilerplate and complexity without significant benefit, especially since the user base is small and data fetching is fast.

**Suggestion:** Eliminate pinia stores that only proxy Service calls. Use vue composables or simple `ref` variables in components to hold data fetched directly from Services.
*   **Action:** Deprecate and remove `useProcessosStore`, `useMapasStore`, and `useSubprocessosStore`.
*   **Action:** Refactor components that rely on these stores to import the corresponding Service functions directly or use local Composables (e.g., `useProcessos.ts` as outlined in `plano-simplificacao.md`).
*   **Action:** Restrict pinia usage strictly to global application state, such as Authentication (User session), UI Notifications (`feedback.ts` / `toast.ts`), and potentially global configuration.

## 5. Frontend: Simplify wrapper components

**Issue:** There may be Vue components that exist solely to wrap other components (e.g., wrapping BootstrapVueNext components without adding significant business logic or styling), just passing props and emitting events.

**Suggestion:** Avoid wrapper components that only proxy props and events.
*   **Action:** Use BootstrapVueNext components directly in the views or feature components unless a custom behavior or complex composite structure is genuinely required.

## 6. General: Procedural logic over Design patterns

**Issue:** The use of Factories, Builders, and complex Facades suggests an enterprise-level architecture that is overkill for a 5-10 user intranet app.

**Suggestion:** Favor direct, procedural code in Services over complex object-oriented design patterns.
*   **Action:** Ensure that new features avoid Builders and Factories for simple objects.
