# Simplification Suggestions for SGC Project

This document outlines suggestions for simplifying the SGC codebase, focusing on reducing overengineering, unnecessary complexity, and fragmentation, given the constraint of the system being used by at most 5-10 simultaneous users on an intranet.

## Principles of Simplification

Given the project's scale and audience, many guidelines meant for highly-scalable, multi-layered applications are counter-productive. We should embrace a simpler, more direct architecture.

1.  **Avoid Microservices and Layered Architectures:** Stick with a simple, monolithic structure. Avoid Hexagonal/Onion architectures and use standard JPA annotations directly on domain models.
2.  **Eliminate the Facade Layer:** In many places, the `Facade` pattern is used simply to pass calls through to `Services` without adding business value. This creates unnecessary indirection. Controllers should communicate directly with Services.
    *   *Action:* Avoid `*Facade.java` classes (e.g., `PainelFacade`, `UsuarioFacade`). Domain logic should go directly into `Services` (e.g., `ProcessoService`, `PainelService`).
3.  **Consolidate Fragmented Services:** Avoid splitting domain logic across multiple granular services unless there is a very strong reason. Merge cohesive, highly granular services into consolidated domain services.
    *   *Action:* Review service fragmentation in domains like `Subprocesso` and `Mapa`. Avoid service fragmentation (e.g., merge `SubprocessoTransicaoService` and `SubprocessoValidacaoService` into a unified `SubprocessoService`) to reduce unnecessary complexity.
4.  **Direct Repository Access for Simple CRUD:** For basic read operations, Controllers should be allowed to access Spring Data Repositories directly, rather than routing through a pass-through Service method.
5.  **Reduce DTO Mapping for Simple Reads:** Avoid creating DTOs and mapping logic just to return data exactly as it exists in the Entity.
    *   *Action:* Minimize DTO mapping by returning entities for simple reads. Utilize `@JsonView` and `@JsonIgnore` to control serialization and hide sensitive or internal fields. Only use DTOs when aggregating data from multiple sources, avoiding recursion, or when the API contract must strictly differ from the persistence model.
6.  **Avoid Complex Design Patterns:** Avoid Builders and Factories for simple objects. Favor procedural code in services over complex design patterns. Avoid single-implementation interfaces (use concrete classes instead).
7.  **Simplify Frontend State Management:** Pinia stores should be reserved for truly global state (e.g., current user session).
    *   *Action:* Minimize pass-through Pinia stores. Identify stores that act merely as pass-throughs for API calls or manage state specific to a single view. Migrate these to Vue Composables (`use...`) or local component state (`ref`, `reactive`).
8.  **Eliminate Empty Wrapper Components:** Avoid creating Vue components that only wrap a third-party component and pass props/events through without adding custom logic or consistent styling.
    *   *Action:* Avoid frontend wrapper components that only proxy props/events without adding real business value.

## Conclusion

By adopting these simpler patterns, the codebase will become easier to navigate, maintain, and extend, without sacrificing the reliability needed for a small-scale intranet application. The focus shifts from "preparing for massive scale" to "maximizing developer productivity and code clarity".
