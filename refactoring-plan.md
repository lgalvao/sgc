# Refactoring Plan: Frontend and Backend

This document outlines the necessary refactoring steps to move business logic from the frontend to the backend, making the frontend leaner and the backend the single source of truth for business rules and validations.

## Frontend Refactoring

### 1. **`CadProcesso.vue`**

*   **Objective**: Simplify the process creation form by removing business logic related to unit selection.
*   **Identified Issues**:
    *   The frontend calls `processosStore.buscarStatusUnidades` to get a list of all units and then determines which ones should be disabled based on the process type. This is business logic.
    *   The `extrairCodigosUnidades` function is a client-side workaround to handle a nested data structure.
*   **Proposed Changes**:
    *   **Agent Task**: Modify the `processosStore` and the `CadProcesso.vue` component.
    *   Instead of fetching all units and disabling some, the frontend should call a new backend endpoint `GET /api/processos/unidades-validas?tipoProcesso={tipo}`.
    *   This endpoint will return only the units that are eligible to participate in a process of the given type.
    *   The `ArvoreUnidades` component will then display these units without any disabling logic.
    *   Remove the `extrairCodigosUnidades` function. The component should be able to get the selected unit codes directly from the `ArvoreUnidades` component's `v-model`.

### 2. **`SubprocessoView.vue`**

*   **Objective**: Remove complex state calculations and authorization logic. The view should primarily display data provided by the backend.
*   **Identified Issues**:
    *   `unidadeComResponsavelDinamico`: This computed property determines the responsible user for a unit by checking for temporary assignments. This logic for resolving the current responsible user is business logic.
    *   `isSubprocessoEmAndamento` and `etapaAtual`: These computed properties derive the subprocess's state (e.g., "in progress", "step 1") from its `situacao`. This is frontend logic replicating the backend's state machine.
    *   `irParaAtividadesConhecimentos`: This function contains authorization logic, deciding which page to show based on the user's profile.
*   **Proposed Changes**:
    *   **Agent Task**: Modify the `SubprocessoView.vue` component and its related stores (`processosStore`, `usuariosStore`).
    *   The backend should enhance the `SubprocessoDetalhesDto` to include pre-calculated fields.
    *   The logic for `unidadeComResponsavelDinamico` should be moved to the backend. The `SubprocessoDetalhesDto` should directly provide the `titular` and `responsavel` for the unit within the context of that subprocess, already considering any temporary assignments.
    *   The backend should provide clear state indicators. Add `isEmAndamento: boolean` and `etapaAtual: number` fields to the `SubprocessoDetalhesDto`. The frontend will use these fields directly, removing the `isSubprocessoEmAndamento` and `etapaAtual` computed properties.
    *   The authorization logic in `irParaAtividadesConhecimentos` should be removed. The backend should provide a list of available actions or permissions for the current user within the `SubprocessoDetalhesDto`. For example: `permissoes: { podeEditarMapa: true, podeVisualizarDiagnostico: false }`. The frontend will use these flags to conditionally render buttons and links.

### 3. **General Frontend Cleanup**

*   **Objective**: Standardize validation and remove redundant logic across the application.
*   **Proposed Changes**:
    *   **Agent Task**: Review all components and views for client-side validation logic that duplicates backend rules (e.g., checking field lengths, complex cross-field validation).
    *   Keep only basic client-side validation (e.g., required fields) for better UX.
    *   Rely on the backend for all authoritative business rule validation, displaying errors returned from the API.

## Backend Refactoring

### 1. **New Endpoint for Valid Units**

*   **Objective**: Create an endpoint to provide the frontend with a list of units eligible for a new process.
*   **Proposed Changes**:
    *   **Agent Task**: Create a new endpoint `GET /api/processos/unidades-validas`.
    *   This endpoint will accept a `tipoProcesso` query parameter.
    *   It will contain the business logic to determine which units can participate in a given process type (e.g., checking if a unit already has an active map for a `MAPEAMENTO` process).
    *   The endpoint should return a tree structure of `UnidadeDto` objects that the `ArvoreUnidades` component can directly consume.

### 2. **Enhance `SubprocessoDetalhesDto`**

*   **Objective**: Provide the frontend with a rich DTO that minimizes the need for client-side logic.
*   **Proposed Changes**:
    *   **Agent Task**: Modify the `SubprocessoService` and the mappers responsible for creating the `SubprocessoDetalhesDto`.
    *   **Responsible User**: The service should resolve the current responsible user for the subprocess's unit, considering temporary assignments. The `SubprocessoDetalhesDto` should have `titular` and `responsavel` fields populated with the correct `UsuarioDto`.
    *   **State Indicators**: Add the following fields to the DTO:
        *   `isEmAndamento: boolean`
        *   `etapaAtual: number`
    *   **Permissions**: Add a `permissoes` object to the DTO. This object will contain boolean flags indicating the actions the *current authenticated user* can perform on this subprocess. For example:
        ```json
        "permissoes": {
          "podeEditarMapa": true,
          "podeEditarAtividades": true,
          "podeVisualizarDiagnostico": false,
          "podeAlterarDataLimite": true
        }
        ```
    *   The service layer will need to be aware of the current user's security context to compute these permissions.

### 3. **Strengthen Backend Validation**

*   **Objective**: Ensure all business rules are enforced on the backend.
*   **Proposed Changes**:
    *   **Agent Task**: Review all services (`ProcessoService`, `SubprocessoService`, etc.) and ensure that all validation logic currently present in the frontend is implemented in the backend.
    *   This includes validating the state of a process/subprocess before allowing an action, checking user permissions, and validating input data.
    *   Use custom exceptions for validation errors that can be mapped to appropriate HTTP status codes (e.g., 400, 409).
