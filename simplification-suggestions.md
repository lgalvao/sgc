# Simplification Suggestions for SGC

This document outlines guidelines and specific suggestions for avoiding overengineering and excessive complexity within the SGC project. Considering that the system is an intranet tool intended for at most 5-10 simultaneous users, many patterns suited for highly-scalable, multi-layered, or super-modularized applications are **unnecessary and actively discouraged**. The focus should be on direct, simple, and cohesive code.

## General Architectural Guidelines

- **Avoid Architectural Overengineering:** Strict architectural patterns such as Hexagonal or Onion architectures are not required. Use JPA annotations directly on domain models.
- **Direct Repository Access:** Allow Controllers direct access to Spring Data Repositories for simple CRUD operations, bypassing the need for a service layer if no business logic is involved.
- **No Builders/Factories for Simple Objects:** Avoid Builders and Factories for simple object instantiation.
- **Single-Implementation Interfaces:** Avoid creating interfaces that only have a single concrete implementation. Use the concrete classes directly.
- **Minimize DTO Mapping:** For simple reads, return domain entities directly rather than creating and mapping to DTOs.
- **Avoid Service Fragmentation:** Favor procedural code in services over complex design patterns. Merge cohesive, highly granular services into consolidated domain services to reduce unnecessary complexity.

## Backend Specific Issues & Suggestions

### 1. Remove Facade Layers
Facade layers in the backend are considered overengineering for this project. Domain logic should be consolidated directly into Services, and Controllers can interact directly with Repositories or Services as needed.

**Action:** Remove pass-through Facades such as:
- `PainelFacade`
- `UsuarioFacade`

### 2. Consolidate Fragmented Services
Services have been artificially fragmented, increasing navigation difficulty and coupling without tangible benefits.

**Action:** Consolidate related services. For example:
- Combine `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`, and `SubprocessoSituacaoService` directly into `SubprocessoService`.
- Consolidate Mapa-related services like `CopiaMapaService`, `ImpactoMapaService`, `MapaManutencaoService`, `MapaSalvamentoService`, and `MapaVisualizacaoService` into a more cohesive `MapaService`.

### 3. Simplify Read Operations
Avoid manually mapping to DTOs for simple read operations.

**Action:** Directly expose entities for basic queries where the risk of over-exposing sensitive data is not an issue, reducing boilerplate code.

## Frontend Specific Issues & Suggestions

### 1. Minimize Pass-Through Pinia Stores
Pinia stores should be reserved for globally shared, durable state (like user profiles or authentication). Stores that merely keep screen state or pass through API calls to services without adding value should be removed.

**Action:** Avoid or refactor pass-through stores like `configuracoes.ts` (which currently acts largely as a wrapper around `configuracaoService`). Use Vue composables or local component state instead.

### 2. Avoid Pointless Wrapper Components
Frontend components that only act as proxies—passing props and events down to base components (like those from BootstrapVueNext) without adding meaningful domain logic, UI composition, or behavior—should be eliminated.

**Action:** Use the base components directly in the views rather than maintaining pass-through wrappers.