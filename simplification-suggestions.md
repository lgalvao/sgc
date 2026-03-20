# Simplification & De-overengineering Suggestions

**CRITICAL SYSTEM DESIGN CONSTRAINT:** This system is an intranet tool designed to be used by **at most 5-10 simultaneous users**.

Because of this extremely limited scale, many architectural guidelines accrued from training on highly-scalable, multi-layered, super-modularized enterprise applications **DO NOT APPLY HERE**. Applying such patterns introduces excessive complexity, fragmentation, and overhead without any tangible benefit to this specific project.

Below are concrete suggestions to simplify the codebase, reduce fragmentation, and improve maintainability by removing overengineered solutions.

## 1. Backend: Eliminate Facade Layers
**Issue:** The project currently uses Facades (`PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, `AtividadeFacade`) as an unnecessary intermediary layer between Controllers and Services. In a system of this size, this is pure overengineering.
**Suggestion:**
- Remove the Facade layer entirely. Domain logic should be consolidated directly into Services (e.g., `ProcessoService`, `PainelService`, `UsuarioService`).
- Controllers MUST call Services directly.
- **For simple CRUD operations, Controllers are strictly allowed to inject and call Spring Data Repositories directly.** This eliminates boilerplate pass-through code.

## 2. Frontend: Remove Pass-Through Pinia Stores
**Issue:** Several Pinia stores (`mapas.ts`, `processos.ts`, `subprocessos.ts`, `configuracoes.ts`) act as unnecessary pass-throughs that merely fetch and cache API data for single views, introducing unwanted state-synchronization complexity.
**Suggestion:**
- Remove these stores. Use standard Vue `ref`s, `reactive`, or composables (like `useAsyncAction`) directly within components to hold page-level state and call `services/` directly.
- Reserve Pinia strictly for true global state that is shared across the entire application, such as Authentication/Profile (`perfil.ts`) and UI Feedback/Notifications (`toast.ts`, `feedback.ts`).

## 3. Backend: Reduce Service Fragmentation
**Issue:** Domain logic is heavily fragmented into micro-services within the monolith. For example:
- `Subprocesso` has `SubprocessoService`, `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`.
- `Mapa` has `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService`, `CopiaMapaService`.
**Suggestion:**
- Consolidate these fragmented classes into cohesive, domain-centric services (e.g., a single `SubprocessoService` and a single `MapaService`). Given the application's scale, procedural code in a single service is easier to trace, understand, and maintain than navigating through multiple dispersed classes and injected dependencies.

## 4. Backend: Minimize DTO Mapping for Simple Reads
**Issue:** There is a proliferation of DTOs for simple read operations where the data closely matches the underlying entity. For example, `ProcessoDetalheDto` involves manual, error-prone mapping that often simply replicates entity state.
**Suggestion:**
- Return JPA entities directly from Controllers for simple read operations.
- Eliminate DTOs (and their mapping boilerplate) unless there is a strict need to hide sensitive fields, prevent recursive serialization (e.g., bidirectional relationships), or aggregate data from multiple entities that cannot be simply solved by Jackson annotations (like `@JsonIgnore` or `@JsonView`).

## 5. Frontend: Eliminate Unnecessary Wrapper Components
**Issue:** Applying strict modularization often leads to creating wrapper components that do nothing but proxy props and events down to a library component.
**Suggestion:**
- Avoid creating Vue components that only wrap base UI components (like BootstrapVueNext components) without adding substantial domain logic or distinct visual structure. Use the base components directly to avoid prop-drilling and event-bubbling overhead.

## 6. General Architectural Philosophy for this Scale
- **No Complex Architectures:** Avoid Hexagonal Architecture, Onion Architecture, CQRS, or strict Clean Architecture boundaries. Use JPA annotations directly on domain models.
- **Interfaces:** Avoid single-implementation interfaces (e.g., `IService` and `ServiceImpl`). If an interface only has an `Impl` class, remove the interface and use the concrete class directly.
- **Patterns:** Avoid complex design patterns (Builders for simple objects, Factories). Use Spring Boot and Hibernate features directly and simply. Keep the monolithic structure cohesive (no microservices or fragmented Gradle subprojects).
- **Direct Repositories:** Keep things simple and do not feel obligated to layer simple CRUD logic; if a Controller only saves or reads a record, it can inject the Spring Data Repository interface directly.

## 7. Active Simplification Efforts
**Current Status:** There are active efforts to implement these simplifications, as documented in `plano-simplificacao.md`.
**Ongoing Work:**
- **DTOs:** Continuing to remove manual DTO mapping in the `Processo` module, favoring Jackson serialization via `@JsonView(ProcessoViews.Publica.class)` directly on entities.
- **Pinia Stores:** Actively refactoring `frontend/src/stores/processos.ts` to replace the global store with a context-based composable (`useProcessos.ts`) that manages local reactive state (`processos`, `processoAtivo`, `loading`). Views are being updated to use this composable, enabling the eventual removal of the `processos.ts` store.
