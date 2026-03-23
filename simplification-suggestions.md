# Simplification Suggestions & Guidelines

This document outlines the core principles for keeping the system architecture simple, pragmatic, and free of overengineering.

**Context:** The application is an intranet tool for at most 5-10 simultaneous users. Guidelines for highly-scalable, multilayered, or super-modularized applications do not apply here. The focus should be on direct, simple, and readable code.

## General Architectural Philosophy

1.  **Avoid Single-Implementation Interfaces:**
    *   Do not create interfaces that only have a single concrete implementation just for the sake of "loose coupling." Use concrete classes directly.
2.  **Favor Procedural Code in Services:**
    *   Overly complex design patterns (like Strategy, Command, State) are usually unnecessary for our use cases. Prefer straightforward, procedural code within service methods unless the complexity strongly justifies a pattern.
3.  **Avoid Builders/Factories for Simple Objects:**
    *   Use direct constructors or standard instantiation for simple objects. Avoid the overhead of Builder or Factory patterns unless creating the object is genuinely complex.
4.  **Maintain a Cohesive Monolith:**
    *   Keep the system as a monolithic application. Do not fragment it into microservices or multiple disjointed subprojects.

## Backend Guidelines

1.  **Avoid Facades for Direct Passthrough:**
    *   Backend Facade layers (e.g., `PainelFacade`, `UsuarioFacade`) are considered overengineering. Do not create layers that merely pass calls through to another layer.
2.  **Consolidate Domain Logic Directly into Services:**
    *   Domain logic should live directly in Services (e.g., `ProcessoService`, `PainelService`).
3.  **Controllers Can Access Repositories Directly for Simple CRUD:**
    *   For basic CRUD operations that require no additional business logic, it is perfectly acceptable for Controllers to inject and use Spring Data Repositories directly. You do not need a Service just to call `.findById()` or `.save()`.
4.  **Avoid Fragmented Services (Merge Cohesive Logic):**
    *   Merge cohesive, highly granular services into consolidated domain services. For example, combine `SubprocessoTransicaoService` and `SubprocessoValidacaoService` into a single `SubprocessoService`. This reduces unnecessary complexity and fragmentation.
5.  **Minimize DTO Mapping for Simple Reads:**
    *   Avoid mapping to DTOs for simple read operations where the entity structure is sufficient and safe to expose. Return entities directly (using `@JsonView` or `@JsonIgnore` if necessary) to reduce boilerplate code.
6.  **Avoid Hexagonal/Onion Architectures:**
    *   Do not separate domain models from persistence models. Use standard JPA annotations directly on your domain models. Keep it simple.

## Frontend Guidelines

1.  **Minimize Pass-Through Pinia Stores:**
    *   Avoid creating Pinia stores that merely pass through API calls or hold very simple, non-shared state. Use Vue composables (e.g., `useProcessos`) or component-local state instead. Pinia should be reserved for truly global, shared state.
2.  **Avoid Wrapper Components that Only Proxy Props/Events:**
    *   Do not create Vue components whose sole purpose is to wrap another component (like a BootstrapVueNext component) and just proxy its props and events. Use the underlying component directly unless you are adding meaningful, reusable behavior or visual structure.
