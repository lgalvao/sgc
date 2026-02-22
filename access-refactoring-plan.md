# Access Control Refactoring Plan

## 1. Context and Goals
The current access control system is over-engineered, relying on a custom framework (`sgc.seguranca.acesso`) that mimics Spring Security ACLs but adds unnecessary complexity, boilerplate, and database auditing for a small 10-user intranet application.

This complexity has leaked into the frontend via a massive `SubprocessoPermissoes` DTO, tightly coupling UI rendering to backend security calculations.

**Goals:**
1.  **Backend**: Replace the custom framework with a streamlined Spring Security approach using `@PreAuthorize` and a simple `SubprocessoSecurity` logic bean.
2.  **Frontend**: Decouple the UI from the backend by moving visibility logic (which buttons to show) entirely to the client, based on the user's profile, unit, and the subprocess's situation and location.

## 2. Core Security Mechanics
As detailed in `acesso.md`:
*   **Vision (Read-Only)** follows **Ownership Hierarchy**: Users can see subprocesses owned by their unit or subordinate units.
*   **Execution (Write/Move)** follows **Current Location**: Users can only execute actions if the subprocess is physically located at their current unit.

## 3. Execution Phases

### Phase 1: Frontend Decoupling
*Goal: Stop relying on the backend to tell the UI which buttons to show.*
*   **Step 1**: Create a `useAcesso` composable (or similar utility) in the frontend that calculates permissions locally using profile, unit, `situacao`, and `localizacaoAtual`.
*   **Step 2**: Refactor all views (e.g., `SubprocessoDetalheView`, `AtividadesCadastroView`, `SubprocessoCards`) to use this local logic instead of `subprocesso.permissoes`.
*   **Step 3**: Strip `permissoes` out of the frontend types (`tipos.ts`), stores (`subprocessos.ts`), and mappers.

### Phase 2: Backend Simplification
*Goal: Replace the custom framework with native Spring Security.*
*   **Step 1**: Create a simple `@Service("subprocessoSecurity")` bean with two main methods: `canView(user, subprocess)` and `canExecute(user, subprocess)`, implementing the Ownership vs. Location logic.
*   **Step 2**: Annotate Controller endpoints (or Services) with `@PreAuthorize("@subprocessoSecurity.canExecute(principal, #subprocessoId)")`.
*   **Step 3**: Remove the `SubprocessoPermissaoCalculator` and the `SubprocessoPermissoesDto` from the backend data transfer.

### Phase 3: Deletion and Cleanup
*Goal: Remove dead code.*
*   **Step 1**: Delete the entire `sgc.seguranca.acesso` package (policies, custom audits, `AccessControlService`).
*   **Step 2**: Update affected tests to remove mocks of the old security framework and test the new `@PreAuthorize` logic or the `SubprocessoSecurity` bean directly.
