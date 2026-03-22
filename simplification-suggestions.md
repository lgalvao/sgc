# Simplification Suggestions for SGC

Given the project constraints—an intranet application with at most 5-10 simultaneous users—many patterns suited for highly scalable, multi-layered, or super-modularized applications are unnecessary and lead to overengineering.

Here are suggestions to simplify the architecture, reduce boilerplate, and decrease maintenance overhead:

## Backend (Spring Boot / Java)

1. **Remove Facades (Anti-Pattern for this Context)**
   - **Current State:** The application uses several Facades (e.g., `AtividadeFacade`, `PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`).
   - **Suggestion:** Eliminate the Facade layer. Controllers should interact directly with Services. For a monolithic application with low complexity and few users, Facades add an unnecessary layer of indirection that only passes calls through without adding meaningful business value.
   - **Action:** Consolidate domain logic directly into Services (e.g., `MapaService`, `ProcessoService`, `UsuarioService`).

2. **Allow Direct Controller-to-Repository Communication for Simple CRUD**
   - **Current State:** Controllers call Services which then call Repositories, even for simple reads or list operations.
   - **Suggestion:** For basic CRUD operations or simple queries that do not involve complex business rules or multi-entity orchestrations, Controllers can and should inject and call Spring Data Repositories directly. This eliminates "pass-through" Service methods.

3. **Minimize the use of DTOs for Simple Reads**
   - **Current State:** Extensive mapping between Entities and DTOs.
   - **Suggestion:** For simple read operations where the Entity structure aligns closely with the JSON response required by the frontend, return the JPA Entity directly from the Controller. Modern Spring features (like `@JsonView`, which is already in use in some places like `ProcessoViews`) are perfectly capable of handling serialization without needing dedicated DTOs and Mappers for every endpoint.

4. **Consolidate Granular Services**
   - **Suggestion:** Avoid service fragmentation. Merge cohesive, highly granular services into consolidated domain services (e.g., combining `SubprocessoTransicaoService` and `SubprocessoValidacaoService` into a single `SubprocessoService`) to reduce unnecessary complexity and dependency injection overhead.

5. **Avoid Abstract Patterns for Simple Objects**
   - **Suggestion:** Do not use overly abstract patterns like generic Builders, Factories, or Hexagonal/Onion architectures. Use JPA annotations directly on domain models and keep the application as a simple, cohesive Modular Monolith.
   - **Note:** Use concrete classes over single-implementation interfaces. Interfaces should only be created when multiple implementations are actually required.

## Frontend (Vue.js / TypeScript)

1. **Simplify State Management (Pinia vs Composables)**
   - **Current State:** The application uses Pinia stores, but also has Composables like `useProcessos.ts` that maintain global state using `ref` variables declared outside the function scope.
   - **Suggestion:** Consolidate global state management. Use Pinia "Setup Stores" for true global state that needs to be shared across multiple non-related components. For view-specific logic, use Composables that encapsulate logic but do not act as pseudo-global stores. Avoid creating pass-through Pinia stores that merely call API services without actually managing state.

2. **Avoid Pass-through Wrapper Components**
   - **Suggestion:** Do not create custom Vue components whose sole purpose is to wrap a library component (e.g., BootstrapVueNext components) and proxy its props and events. Use the UI library components directly in your views unless you are adding significant custom behavior or styling that needs to be reused.

3. **Direct Error Handling**
   - **Suggestion:** While `useErrorHandler` is useful, avoid over-wrapping every single API call if the local component can handle the specific error state natively and more intuitively.

## General Philosophy

- **YAGNI (You Aren't Gonna Need It):** Don't build for hypothetical future scalability or modularity that a 5-user intranet system will never require.
- **KISS (Keep It Simple, Stupid):** Procedural code in Services is often easier to read, test, and maintain than complex design patterns spread across multiple classes.
