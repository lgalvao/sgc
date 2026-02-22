# Pragmatic Simplification Strategy for SGC

> **Context:** This system will be used by **at most 5-10 simultaneous users** inside an intranet.
> **Diagnosis:** The current architecture is designed for high-scale, distributed teams (Microservices patterns, Hexagonal/Clean Architecture, strict isolation), leading to excessive "glue code" managing non-existent complexity.
> **Goal:** This document outlines a strategy to aggressively simplify the codebase, reducing maintenance overhead and cognitive load, while maintaining application stability and security.

---

## 1. Cohesive Controllers & Services

The `subprocesso` module (`backend/src/main/java/sgc/subprocesso`) is a prime example of fragmentation. Currently, it has 4 Controllers, 1 Facade (injecting 11 services), and 10+ Services representing arbitrarily split logic.

Navigating a simple feature requires jumping through 5 files. The Facade pattern is designed for orchestrating complex microservice boundaries, not for a simple intranet monolith. It adds pure passthrough boilerplate.

**Action Plan:**
- **Merge Controllers:** Consolidate into a single cohesive `SubprocessoController` handling all Subprocess-related HTTP routing.
- **Delete the Facade:** Controllers should inject focused Services directly.
- **Group Services by Cohesion:** Merge the 10+ fragmented services into 2-3 focused services based on domain logic (e.g., `SubprocessoCrudService` for basic data entry, `SubprocessoWorkflowService` for state transitions). Eliminate classes with only 1 or 2 methods that simply pass data through.
- **Maintain Standard Flow:** Always maintain the standard `Controller -> Service -> Repository` flow. The `@Service` layer is crucial for safely defining database transaction boundaries (`@Transactional`).

## 2. Standardize Access Control

The system currently implements a custom security framework in `sgc.seguranca.acesso` (`AccessControlService`, `AccessPolicy`, `AccessAuditService`, `AbstractAccessPolicy`). It mimics Spring Security ACLs using bespoke code and manual interceptors.

Maintaining custom security code is a massive liability. Spring Security is the industry standard and natively supports complex method-level authorization. A custom framework for a small intranet application is unnecessary overhead.

**Action Plan:**
- **Use Standard Spring Security:** Leverage standard expressions like `@PreAuthorize("@subprocessoSecurity.canEdit(#id, principal)")`.
- **Delete the Custom Framework:** Remove all `*AccessPolicy` classes and custom security interceptors.
- **Centralize Checks:** Create localized security services (e.g., `SubprocessoSecurity`) to evaluate complex rules using standard Spring paradigms.
- **Simplify Auditing:** Rely on standard `slf4j` logging instead of custom audit tables for authorization checks.

## 3. Simplify Data Transport (DTOs & Mappers)

The system currently uses complex mapping frameworks and deep defensive programming when passing data between the database and the frontend.

**Action Plan:**
- **Use Simple DTOs:** Keep Data Transfer Objects, but simplify them. Instead of complex mappers (like MapStruct), use Java 17+ `record` types to create lightweight DTOs. Use basic constructors or static factory methods (e.g., `return new SubprocessoResponse(entity)`) to map data safely. This avoids the `LazyInitializationException` risks of returning JPA Entities directly while removing mapper boilerplate.
- **Use Generated API Types on the Frontend:** Eliminate manual mapping code in the UI (`frontend/src/mappers/*.ts`). Rely on strictly typed TypeScript definitions generated from the backend OpenAPI/Swagger contracts.

## 4. Specific Targets for Refactoring

| Component | Diagnosis | Action |
| :--- | :--- | :--- |
| `SubprocessoFacade` | Passthrough "glue code" | **Delete.** Controllers inject Services directly. |
| `Subprocesso*Controller` (x4) | Arbitrary fragmentation | **Merge** into `SubprocessoController`. |
| `*AccessPolicy` classes | Overengineered custom framework | **Delete.** Replace with `@PreAuthorize` + standard service checks. |
| `AccessAuditService` | Redundant custom logging | **Delete.** Use standard `slf4j` logging. |
| `frontend/src/mappers/*.ts` | Manual, redundant mapping | **Refactor.** Use direct API types via OpenAPI generators. |
| Complex mapStruct mappers | Excessive boilerplate | **Refactor.** Replace with Java `record` constructors. |

## 5. Development Workflow Guidelines

- **Stop arbitrarily splitting logic:** If a feature belongs to "Subprocess Management", it goes in the primary Subprocess service. Avoid creating new classes for single methods.
- **Leverage Integration Tests:** Avoid writing 50 lines of Mockito setup to test a 3-line service passthrough method. Focus on `@SpringBootTest` or `@DataJpaTest` to verify the code actually works against a real (or in-memory) database schema.
