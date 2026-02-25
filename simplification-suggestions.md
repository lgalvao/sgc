# Radical Simplification Suggestions for SGC

This document outlines a "Radical Simplification" strategy for the SGC system, overriding previous conservative simplification plans (e.g., `simplification-plan.md` and ADR-008). Given the constraint of **5-10 concurrent users** in an intranet environment, the current architecture is significantly over-engineered.

## Core Principle
**Eliminate layers and fragmentation.** Embrace vertical slices and direct service calls. Remove all patterns designed for distributed teams or high scalability (Facades, excessive DTO mapping, fragmented services).

---

## 1. Backend Architecture

### Current State (Over-engineered)
- **Layering:** `Controller` → `Facade` → `WorkflowService` → `DomainService` → `Repository`.
- **Fragmentation:** `Subprocesso` domain is split into 4 controllers (`Cadastro`, `Mapa`, `Crud`, `Validacao`) and ~12 services (`Workflow`, `MapaWorkflow`, `Transicao`, `AjusteMapa`, etc.).
- **Redundancy:** `Facade` classes (`SubprocessoFacade`, `AnaliseFacade`, `MapaFacade`) act as pass-throughs with no added value.
- **Security:** Complex ACL maps and custom `AccessControlService` logic mixed with business rules.

### Proposed Solution (Radical Simplification)

#### A. Unified Controller (`SubprocessoController`)
Merge all subprocess-related controllers into a single entry point.
- **Target:** `sgc.subprocesso.SubprocessoController`
- **Merge:**
  - `SubprocessoCadastroController`
  - `SubprocessoMapaController`
  - `SubprocessoCrudController`
  - `SubprocessoValidacaoController`
  - `AnaliseController` (actions related to subprocess history)

#### B. Unified Service (`SubprocessoService`)
Consolidate all business logic for subprocesses into one robust service. Methods should be grouped by region (e.g., `// Workflow`, `// Validation`, `// Factory`).
- **Target:** `sgc.subprocesso.service.SubprocessoService`
- **Merge Logic From:**
  - `SubprocessoWorkflowService`
  - `SubprocessoMapaWorkflowService`
  - `SubprocessoTransicaoService` (move transition logic to private methods)
  - `SubprocessoValidacaoService` (move validations to private methods)
  - `SubprocessoAjusteMapaService`
  - `SubprocessoFactory` (factory methods)
  - `SubprocessoFacade` (delete)

#### C. Remove Facades
Delete all Facade classes. Controllers must call Services directly.
- **Delete:** `SubprocessoFacade`, `AnaliseFacade`, `MapaFacade`, `OrganizacaoFacade`, `UsuarioFacade`.

#### D. Simplified Security
Replace custom `AccessControlService` and ACL maps with standard Spring Security.
- Use `@PreAuthorize("hasPermission(..., 'EDITAR')")` directly on Controller methods.
- Implement a simple `SgcPermissionEvaluator` that checks the two core rules (Hierarchy for Read, Location for Write) directly against the `Subprocesso` entity.

#### E. Direct Repository Access (Read-Only)
For simple queries (e.g., `listar()`, `buscar()`), the Controller or Service can call the Repository directly, skipping the service layer if no business logic is needed.

---

## 2. Frontend Architecture

### Current State
- Services mirror the backend fragmentation (`subprocessoService`, `mapaService`, `cadastroService`).
- Manual Mappers (`frontend/src/mappers/*.ts`) duplicate data transformation logic.

### Proposed Solution

#### A. Unified Service
- Merge `subprocessoService.ts`, `mapaService.ts`, and `analiseService.ts` into a single `subprocessoService.ts` to match the backend API.

#### B. Remove Mappers
- Delete `frontend/src/mappers/`.
- Use TypeScript interfaces that strictly match the Backend DTOs.
- If transformation is needed (e.g., dates), do it in the Service or a utility, not a heavy Mapper layer.

---

## 3. Testing Strategy

### Current State
- Heavy reliance on Unit Tests with mocks for internal dependencies (`@MockBean` / `@MockitoBean`).
- Presence of Pact (contract) tests (now removed but concept lingers).

### Proposed Solution
- **Shift to Integration Tests:** Prioritize `@SpringBootTest` or `@WebMvcTest` that spin up the full context (Controller + Service + Repository + H2). This verifies the *actual* flow.
- **Delete Pact:** Ensure all contract testing artifacts are removed.
- **End-to-End:** Rely on the existing Playwright E2E tests for critical user flows.

---

## 4. Execution Roadmap

1.  **Refactor Backend:**
    1.  Create `SubprocessoService` (empty).
    2.  Move logic from `SubprocessoWorkflowService` and `SubprocessoMapaWorkflowService` into it, resolving circular dependencies by inlining logic.
    3.  Update `Subprocesso` entity to include necessary helper methods.
    4.  Create `SubprocessoController` and migrate endpoints from the 4 fragmented controllers.
    5.  Delete old Controllers, Facades, and Services.

2.  **Refactor Security:**
    1.  Update `SgcPermissionEvaluator` to handle the consolidated logic.
    2.  Apply annotations to `SubprocessoController`.

3.  **Refactor Frontend:**
    1.  Update `subprocessoService.ts` to call the new endpoints.
    2.  Remove `mapaService.ts` and update components.
    3.  Delete Mappers.

4.  **Verification:**
    1.  Run Backend Integration Tests.
    2.  Run Frontend E2E Tests.
