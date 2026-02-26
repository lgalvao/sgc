# Simplification Suggestions

**Goal:** Reduce overengineering and fragmentation for a small-scale intranet application (5-10 concurrent users).

The current codebase exhibits signs of excessive layering and fragmentation, which are unnecessary for the scale of the application. The following recommendations aim to simplify the architecture, reduce maintenance overhead, and improve developer velocity.

## 1. Frontend Simplification (Vue/TypeScript)

### 1.1 Consolidation of Services
Currently, the frontend separates logic into multiple service files (`subprocessoService.ts`, `mapaService.ts`, `analiseService.ts`) despite all interacting with the same domain entity (`Subprocesso`). This fragmentation forces developers to jump between files unnecessarily.

**Recommendation:**
- **Merge** `mapaService.ts` and `analiseService.ts` into `subprocessoService.ts`.
- Rename `subprocessoService.ts` to simply `api.ts` or `domain.ts` if it becomes the primary entry point for domain logic, or keep it as `subprocessoService.ts` but make it comprehensive.

**Status:**
- [x] `mapaService.ts` and `analiseService.ts` have been merged into `subprocessoService.ts`.

### 1.2 Removal of Manual Mappers
The `frontend/src/mappers/` directory contains manual mapping logic (e.g., `mapSubprocessoDetalheDtoToModel`) that duplicates data structures already present in the backend DTOs. This adds a layer of maintenance without providing significant value in a small app.

**Recommendation:**
- **Delete** the `frontend/src/mappers/` directory.
- **Directly consume** backend DTOs in the frontend components.
- Use tools like `openapi-generator` or simple interface definitions that match the JSON response.
- If transformation is needed for UI specific logic (e.g., formatting dates), do it within the component or a shared utility, not a dedicated mapper layer.

**Status:**
- [x] `frontend/src/mappers/` directory has been deleted.
- [ ] Internal mapper functions (e.g. `mapMapaDtoToModel`) in `subprocessoService.ts` are pending removal.

### 1.3 Simplification of Types
The `frontend/src/types/tipos.ts` file likely contains types that are mirror images of the backend DTOs but manually maintained.

**Recommendation:**
- Use a tool to **generate TypeScript interfaces** from the backend Java classes (e.g., `typescript-generator-maven-plugin` or similar) or simply align them 1:1 and remove the manual mapping layer.
- Avoid "Model" vs "DTO" distinction in the frontend unless absolutely necessary.

## 2. Backend Simplification (Spring Boot)

### 2.1 Reduce DTO Proliferation
The `sgc.subprocesso.dto` package contains a large number of DTOs (e.g., `CriarAnaliseCommand`, `CriarAnaliseRequest`, `RegistrarTransicaoCommand`). Many of these seem to serve similar purposes or wrap simple data.

**Recommendation:**
- **Consolidate** similar DTOs. For example, a single `SubprocessoActionRequest` could handle multiple state transitions if they share common fields (like `observacao` or `data`).
- Reuse DTOs where appropriate. If `CriarAnaliseRequest` and `CriarAnaliseCommand` are identical, remove one.
- Avoid "Command" vs "Request" separation unless using CQRS (which is likely overkill here).

**Status:**
- [ ] `CriarAnaliseCommand` identified as redundant with `CriarAnaliseRequest`. Pending consolidation.

### 2.2 Simplify Security Logic
The `SgcPermissionEvaluator` is a good step towards standard Spring Security, but it still contains complex business logic (e.g., `checkSituacaoImpactos`).

**Recommendation:**
- **Move business logic** out of the security layer and into the service layer where possible. The security layer should focus on *who* can do *what*, not *when* (business state).
- Rely on standard `@PreAuthorize` annotations with simple roles/permissions where possible.
- If complex rules are needed, keep them in the service layer as "Business Rules" rather than "Security Rules" to simplify testing and reasoning.

### 2.3 Layering
The `backend` structure seems to follow a standard Controller -> Service -> Repository pattern.

**Recommendation:**
- **Avoid** creating interfaces for Services (e.g., `SubprocessoService` interface + `SubprocessoServiceImpl`) unless there are multiple implementations (unlikely).
- **Avoid** Facades if they just delegate to Services. The `SubprocessoController` should call `SubprocessoService` directly.

**Status:**
- [x] `SubprocessoFacade` removed.
- [x] `SubprocessoController` calls `SubprocessoService` directly.

## 3. General Architecture

### 3.1 Vertical Slices
The current package structure is somewhat modular (`sgc.subprocesso`, `sgc.mapa`, etc.), which is good.

**Recommendation:**
- **Encourage** a "Vertical Slice" approach where all logic for a feature (Controller, Service, Repository, DTO, Entity) stays in one package, rather than spreading it across layers.
- **Delete** unused or "future-proof" code. If a feature isn't used *now*, remove it.

### 3.2 Testing
The memory mentions "Radical Simplification" and removing Pact tests.

**Recommendation:**
- **Focus on Integration Tests** (`@SpringBootTest`, `@WebMvcTest`) that cover the full flow.
- **Minimize** extensive Unit Tests for simple CRUD operations that are better covered by integration tests.
- **E2E Tests** (Playwright) are valuable for the critical paths but should not be brittle.

## Summary

For a 5-10 user intranet app, the priority is **simplicity and maintainability**. Code should be easy to read and change. Over-abstraction (layers, mappers, excessive DTOs) adds friction. The goal is to have "just enough" architecture to support the current requirements, not to build a framework for a future enterprise system that may never exist.
