# Simplification Suggestions for SGC

Given the context of an intranet application for 5-10 simultaneous users, the current architecture presents significant areas of overengineering. The project guidelines strictly mandate simple, direct code, avoidance of unnecessary layers/patterns, minimization of DTO mapping, and maintenance of a cohesive monolith. Many of the conventions documented in the backend `README.md` (like strict Facades, heavy DTO usage, and complex security layers) directly contradict these simplification goals for a small-scale app.

Here are the key suggestions to reduce complexity and fragmentation, aligning the codebase with its actual usage scale:

## 1. Backend: Eliminate the "Facade" Pattern and Simplify Controllers

**Issue:** The backend `README.md` dictates a strict Facade pattern where Controllers only interact with Facades, which then orchestrate multiple specialized, package-private Services. This is classic overengineering for a CRUD-heavy intranet app. It creates unnecessary boilerplate and indirection.

**Suggestion:** Remove the Facade layer entirely. Controllers should interact directly with Services or, for simple reads/CRUD, directly with Spring Data Repositories.

*   **Action:** Deprecate and remove Facade classes (e.g., `ProcessoFacade`, `SubprocessoFacade`, `UsuarioFacade`).
*   **Action:** Refactor Controllers to inject necessary Services directly.
*   **Action:** For simple operations (e.g., fetching a list of active units, getting a user by ID), Controllers should inject the Repository directly and call its methods, bypassing empty Service pass-throughs.

## 2. Backend: Consolidate Fragmented Domain Services

**Issue:** The domain logic is excessively fragmented. The `README.md` lists `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoService`, and `SubprocessoContextoService` just for the `Subprocesso` domain. This extreme adherence to Single Responsibility Principle (SRP) makes navigating the codebase difficult and is unnecessary for a small team and simple domain.

**Suggestion:** Consolidate these micro-services back into single, cohesive domain services.

*   **Action:** Merge the fragmented `Subprocesso*Service` classes into a single `SubprocessoService`.
*   **Action:** Apply the same consolidation to the `Processo` domain (merging `ProcessoConsultaService`, workflow, etc., into `ProcessoService`).
*   **Action:** Keep logic procedural within these consolidated services rather than splitting it across multiple strategy or workflow classes.

## 3. Backend: Drastically Reduce DTO Mapping

**Issue:** The documentation explicitly forbids returning JPA entities from Controllers ("NUNCA expor entidades JPA diretamente"). This leads to massive amounts of boilerplate mapping code (DTOs and Mappers) that provide zero value when the API consumer (our own frontend) needs the exact same data structure as the database entity.

**Suggestion:** Return JPA Entities directly from Controllers for most read operations.

*   **Action:** Remove DTOs that are mere 1:1 copies of JPA entities.
*   **Action:** Refactor Controllers to return entities (e.g., `List<Processo>`, `Subprocesso`) directly.
*   **Action:** Reserve DTOs *only* for specific cases:
    *   Creating/Updating data where the input structure significantly differs from the entity.
    *   Aggregating data from multiple entities into a single view.
    *   Hiding sensitive fields (though `@JsonIgnore` or `@JsonView` on the entity is often simpler and preferable).

## 4. Backend: Simplify the Security Architecture

**Issue:** The documented "Security in Layers (3 Camadas)" with `AccessControlService`, `AccessPolicy<T>`, `HierarchyService`, and `AccessAuditService` is an enterprise-grade security framework applied to a 10-user intranet tool. This is highly complex to maintain and test.

**Suggestion:** Move security checks back to the standard, simpler Spring Security mechanisms, specifically method-level security (`@PreAuthorize`) relying on a unified `PermissionEvaluator`.

*   **Action:** Ensure all business rules for access control are encapsulated within `SgcPermissionEvaluator` (which implements Spring's `PermissionEvaluator`).
*   **Action:** Use `@PreAuthorize("hasPermission(#codigo, 'Entidade', 'ACAO')")` directly on Controller methods or Service methods.
*   **Action:** Deprecate and remove the complex `AccessControlService` and individual `AccessPolicy<T>` implementations, absorbing their logic into the `SgcPermissionEvaluator` if necessary, or simply relying on standard Spring Security roles where sufficient.

## 5. Frontend: Remove Pass-through Pinia Stores

**Issue:** The frontend uses Pinia stores (`processos.ts`, `mapas.ts`, `subprocessos.ts`) largely as pass-throughs to cache API data fetched by Services. This adds boilerplate and complexity without significant benefit, especially since the user base is small and data fetching is fast.

**Suggestion:** Eliminate Pinia stores that only proxy Service calls. Use Vue composables or simple `ref` variables in components to hold data fetched directly from Services.

*   **Action:** Deprecate and remove stores like `useProcessosStore`, `useMapasStore`, etc.
*   **Action:** Refactor components that rely on these stores to import the corresponding Service functions (e.g., `processoService.obterDetalhesProcesso`) and manage state locally using `ref` or a custom Composable like `useFetch`.
*   **Action:** Restrict Pinia usage strictly to global application state, such as Authentication (User session) and UI Notifications (`toast.ts`).

## 6. Frontend: Simplify Wrapper Components

**Issue:** There may be Vue components that exist solely to wrap other components (e.g., wrapping BootstrapVueNext components without adding significant business logic or styling), just passing props and emitting events.

**Suggestion:** Avoid wrapper components that only proxy props and events.

*   **Action:** Use BootstrapVueNext components directly in the views or feature components unless a custom behavior or complex composite structure is genuinely required.

## 7. General: Procedural Logic over Design Patterns

**Issue:** The use of Factories, Builders, and complex Facades suggests an enterprise-level architecture that is overkill.

**Suggestion:** Favor direct, procedural code in Services over complex object-oriented design patterns.

*   **Action:** Replace complex Builders with simple factory methods or procedural construction within Services.
*   **Action:** Avoid generic interfaces with single implementations; use concrete classes directly.
