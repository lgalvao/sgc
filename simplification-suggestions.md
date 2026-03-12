# Simplification Suggestions for SGC

Given the context of an intranet application for 5-10 simultaneous users, the current architecture presents several areas of overengineering. Following the strict project guidelines, we must prioritize simple, direct code, avoid unnecessary layers/patterns, minimize DTO mapping, maintain a cohesive monolith, and simplify the frontend state management.

Here are the key suggestions to reduce complexity and fragmentation:

## 1. Backend: Direct Repository Access from Controllers for CRUD

**Issue:** Many controllers delegate simple CRUD operations to Facades and Services, adding unnecessary boilerplate. For example, `UnidadeController` likely uses `UnidadeService` or a Facade just to call `UnidadeRepo.findAll()`. The `ProcessoController` heavily relies on `ProcessoFacade` which then calls multiple services (`ProcessoConsultaService`, `ProcessoManutencaoService`, etc.).

**Suggestion:** For simple reads and basic CRUD operations without complex business logic, Controllers should inject Spring Data Repositories directly.
*   **Action:** Review controllers like `UnidadeController`, `ProcessoController` and remove intermediary Facade/Service layers for methods that simply proxy to `Repo.findById()` or `Repo.findAll()`.
*   **Action:** Refactor `ProcessoFacade`. It currently acts as a massive proxy. Methods like `listarAtivos()` that just call `processoConsultaService.processosAndamento()` should be handled directly in the Controller or a single, cohesive Service if logic is needed.

## 2. Backend: Consolidation of Fragmented Services

**Issue:** Domain logic is fragmented into many small services. For example, the `Processo` domain is split into `ProcessoWorkflowService`, `ProcessoManutencaoService`, `ProcessoConsultaService`, `ProcessoValidacaoService`, and `ProcessoNotificacaoService`. While this follows Single Responsibility to an extreme, it makes the codebase harder to navigate and understand for a small application.

**Suggestion:** Consolidate these micro-services back into a single, cohesive `ProcessoService`.
*   **Action:** Merge `ProcessoWorkflowService`, `ProcessoManutencaoService`, `ProcessoConsultaService`, and `ProcessoValidacaoService` into a single `ProcessoService`. The logic is procedural and tightly coupled to the `Processo` entity.
*   **Action:** Evaluate other domains (e.g., `Mapa`, `Organizacao`) for similar fragmentation and consolidate their services.

## 3. Backend: Minimize DTO Mapping

**Issue:** The system uses DTOs even when entities could be returned directly for reads, leading to boilerplate mapping code. For instance, `ProcessoDetalheDto` and its nested `UnidadeParticipanteDto`.

**Suggestion:** Return JPA entities directly from Controllers for simple read operations where the API consumer needs the full object.
*   **Action:** Use `@JsonView` (which is already present in some places like `ProcessoViews.Publica`) to control serialization instead of creating dedicated DTOs for every view.
*   **Action:** Remove DTOs that have a 1:1 mapping with entities unless they are strictly necessary to hide sensitive data or aggregate complex data not easily achieved via projections.

## 4. Frontend: Removal of Pass-through Pinia Stores

**Issue:** The frontend uses Pinia stores (`processos.ts`, `mapas.ts`, `subprocessos.ts`) largely as pass-throughs to cache API data fetched by Services. This adds boilerplate and complexity without significant benefit, especially since the user base is small and data fetching is fast.

**Suggestion:** Eliminate Pinia stores that only proxy Service calls. Use Vue Composables or simple `ref` variables in components to hold data fetched directly from Services.
*   **Action:** Deprecate and remove `useProcessosStore`, `useMapasStore`, etc.
*   **Action:** Refactor components that rely on these stores to import the corresponding Service functions (e.g., `processoService.obterDetalhesProcesso`) and manage state locally using `ref` or a custom Composable like `useFetchProcesso`.
*   **Action:** Restrict Pinia usage strictly to global application state, such as Authentication (User Session), UI Notifications (`toast.ts`), and potentially global configuration.

## 5. Frontend: Simplify Wrapper Components

**Issue:** There may be Vue components that exist solely to wrap other components (e.g., wrapping BootstrapVueNext components without adding significant business logic or styling), just passing props and emitting events.

**Suggestion:** Avoid wrapper components that only proxy props and events.
*   **Action:** Use BootstrapVueNext components directly in the views or feature components unless a custom behavior or complex composite structure is genuinely required.

## 6. General: Procedural Logic over Design Patterns

**Issue:** The use of Factories (e.g., `PdfFactory`), Builders (`ProcessoDetalheBuilder`), and complex Facades (`ProcessoFacade`, `PainelFacade`) suggests an enterprise-level architecture that is overkill for a 5-10 user intranet app.

**Suggestion:** Favor direct, procedural code in Services over complex object-oriented design patterns.
*   **Action:** Refactor `ProcessoDetalheBuilder` into a procedural method within the consolidated `ProcessoService`.
*   **Action:** Simplify or remove `Facade` classes. If a controller needs to orchestrate a few operations, it can do so directly, or a central Service can handle the orchestration procedurally.
