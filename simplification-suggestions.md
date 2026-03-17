# Simplification & De-overengineering Suggestions

Based on the constraint that this is an intranet application for at most 5-10 simultaneous users, many patterns suited for highly-scalable, multi-layered, and super-modularized enterprise applications are unnecessary and represent overengineering. Below are suggestions to simplify the codebase, reduce fragmentation, and improve maintainability.

## 1. Backend: Eliminate Facade Layers
**Issue:** The project currently uses Facades (`PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, `AtividadeFacade`) as an intermediary layer between Controllers and Services.
**Suggestion:**
- Remove the Facade layer entirely. Domain logic should be consolidated directly into Services (e.g., `ProcessoService`, `PainelService`, `UsuarioService`).
- Controllers can call Services directly, or for simple CRUD operations, Controllers should be allowed to inject and call Spring Data Repositories directly. This eliminates boilerplate and pass-through code.

## 2. Frontend: Remove Pass-Through Pinia Stores
**Issue:** Several Pinia stores (`mapas.ts`, `processos.ts`, `subprocessos.ts`, `atividades.ts`, `usuarios.ts`) act as unnecessary pass-throughs that merely fetch and cache API data for single views.
**Suggestion:**
- Remove these stores. Use standard Vue `ref`s, `reactive`, or composables (like `useAsyncAction`) directly within components to hold page-level state and call `services/` directly.
- Reserve Pinia strictly for true global state that is shared across the entire application, such as Authentication/Profile (`perfil.ts`) and UI Feedback/Notifications (`feedback.ts`, `toast.ts`).

## 3. Backend: Reduce Service Fragmentation
**Issue:** Domain logic is heavily fragmented into micro-services within the monolith. For example:
- `Subprocesso` has `SubprocessoService`, `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`.
- `Mapa` has `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService`, `CopiaMapaService`.
**Suggestion:**
- Consolidate these fragmented classes into cohesive, domain-centric services (e.g., a single `SubprocessoService` and a single `MapaService`). Given the application's scale, procedural code in a single service is easier to trace and maintain than navigating through multiple dispersed classes and injected dependencies.

## 4. Backend: Minimize DTO Mapping for Simple Reads
**Issue:** There is a proliferation of DTOs for simple read operations where the data closely matches the underlying entity.
**Suggestion:**
- Return JPA entities directly from Controllers for simple read operations. Eliminate DTOs (and their mapping boilerplate) unless there is a strict need to hide sensitive fields, prevent recursive serialization (e.g., bidirectional relationships), or aggregate data from multiple entities that cannot be simply solved by Jackson annotations (like `@JsonIgnore` or `@JsonView`).

## 5. Frontend: Eliminate Unnecessary Wrapper Components
**Issue:** Applying strict modularization often leads to creating wrapper components that do nothing but proxy props and events down to a library component.
**Suggestion:**
- Avoid creating Vue components that only wrap base UI components (like BootstrapVueNext components) without adding substantial domain logic or distinct visual structure. Use the base components directly to avoid prop-drilling and event-bubbling overhead.

## 6. General Architectural Philosophy
- **Interfaces:** Avoid single-implementation interfaces. If an interface only has an `Impl` class, remove the interface and use the concrete class directly. (Note: A quick scan showed no `Impl` suffix files, which is good, but keep this in mind).
- **Patterns:** Avoid complex design patterns (Builders for simple objects, Factories, Hexagonal/Onion architectures). Use Spring Boot and Hibernate features directly and simply.
