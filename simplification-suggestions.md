# Simplification Suggestions for SGC

Considering the system will be used by at most 5-10 simultaneous users inside an intranet, many guidelines for highly-scalable, multilayered, super-modularized applications **do not apply** here. The goal is a straightforward, cohesive monolith without overengineering.

Based on the existing codebase, system constraints, and the consolidation plan (`plano-simplificacao.md`), here are specific guidelines to avoid excessive complexity and fragmentation:

## 1. Backend: Eliminate Artificial Layers (Facades)
- **Problem**: The codebase currently employs Facade layers (e.g., `PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, `AtividadeFacade`) that often just orchestrate trivial calls or duplicate existing service responsibilities.
- **Guideline**: Backend Facade layers are considered overengineering for this project. Remove these facades entirely. Consolidate their domain logic directly into the respective Services (e.g., `ProcessoService`, `PainelService`, `UsuarioService`). This flattens the architecture, making navigation easier and reducing boilerplate.

## 2. Backend: Consolidate Fragmented Services
- **Problem**: There is artificial fragmentation in the domains of `Subprocesso` and `Mapa`.
    - `Subprocesso` is split into: `SubprocessoService`, `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`.
    - `Mapa` is split into: `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService`, `CopiaMapaService`.
- **Guideline**: Avoid service fragmentation. Merge cohesive, highly granular services into consolidated domain services (e.g., combining `SubprocessoTransicaoService` and `SubprocessoValidacaoService` into a single `SubprocessoService`) to reduce unnecessary complexity. Only preserve separate classes if they isolate genuinely complex workflows or distinct rules.
- **Rule of Thumb**: Favor procedural, straightforward code in services over complex design patterns.

## 3. Backend: Avoid DTO Overuse for Simple Reads
- **Problem**: Manual mapping to DTOs for simple, read-only operations adds unnecessary boilerplate.
- **Guideline**: Minimize DTO mapping by returning JPA entities directly for simple reads. Control serialization safely using Jackson's `@JsonView` and `@JsonIgnore`. Only use DTOs when there is a real need for data aggregation, field protection (e.g., passwords), preventing recursion, or fulfilling a specific API contract that differs significantly from the domain model.
- **Note**: When DTOs are necessary, continue using Java Records. Use native record accessor methods (e.g., `dto.valido()`, `dto.siglaUnidade()`) instead of traditional JavaBeans getter methods.

## 4. Backend: Controllers and Repositories
- **Constraint Reminder**: While the general guideline for simple apps allows Controllers to access Repositories directly for basic CRUD, the current project has automated architectural rules (ArchUnit) prohibiting this.
- **Guideline**: Respect the existing ArchUnit rule (`ArchConsistencyTest`). Do not modify or relax architectural constraints to bypass rules when performing unrelated tasks. Controllers must still go through Services, but ensure those Services are not just empty pass-throughs. The simplification should occur by flattening the `Controller -> Facade -> Service -> Repository` chain into a simpler `Controller -> Service -> Repository` chain.

## 5. Backend: Avoid "Enterprise" Patterns
- **Guideline**: Avoid Hexagonal or Onion architectures; use JPA annotations directly on domain models. Do not use single-implementation interfaces (use concrete classes directly). Avoid Builders or Factories for simple object creation. Maintain a cohesive monolith (no microservices or fragmented Gradle subprojects).

## 6. Frontend: Eliminate Pass-Through Pinia Stores
- **Problem**: Stores like `frontend/src/stores/configuracoes.ts` are acting as pass-throughs or simple local caches that just forward calls to services.
- **Guideline**: Pinia should be reserved strictly for globally shared, durable state (like authentication, user profile, or global toasts). Minimize pass-through Pinia stores. For single-page workflows, form states, or simple API fetching, migrate logic to Vue Composables (`useAsyncAction`, etc.) or handle it directly within the component using `ref` and `reactive`.

## 7. Frontend: Remove Useless Wrapper Components
- **Problem**: Creating Vue components that do nothing but pass props and events down to base BootstrapVueNext components.
- **Guideline**: Avoid frontend wrapper components that only proxy props/events. Remove these wrappers and use the base BootstrapVueNext components directly in the views, unless the wrapper encapsulates real reusable behavior, specific domain semantics, or relevant visual structure.
