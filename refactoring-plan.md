# Comprehensive Refactoring Plan: Migrating Logic from Frontend to Backend

This document provides a detailed plan for refactoring the SGC application. The primary objective is to migrate all business logic, validation, and authorization from the frontend (Vue.js) to the backend (Java/Spring Boot). This will transform the frontend into a pure presentation layer, making the application more robust, secure, and easier to maintain.

## 1. Guiding Philosophy: The "Thin Frontend" Principle

The core of this refactoring is to establish a clear separation of concerns, with the backend acting as the single source of truth for all business operations.

### 1.1. Backend as the Single Source of Truth
- **All business rules, validation, and authorization logic** must reside exclusively in the backend.
- The frontend should not duplicate any of this logic. Its role is to present data and collect user input.

### 1.2. "Smart" DTOs and "Dumb" Views
- The backend API should provide Data Transfer Objects (DTOs) that are tailor-made for the frontend's needs.
- **Pre-calculated State**: Instead of sending raw data that requires frontend logic to interpret, the backend should send pre-calculated state. For example, a `SubprocessoDTO` should include a boolean flag like `isEmAndamento` rather than just a `situacao` enum that the frontend has to decode.
- **Permissions-based UI**: The backend should determine what actions a user can perform on a given resource. DTOs should include a `permissoes` object (e.g., `permissoes: { podeEditar: true, podeExcluir: false }`). The frontend will use these flags to conditionally render buttons, links, and form fields, eliminating the need for client-side authorization checks.

### 1.3. API-Driven User Experience
- The frontend should be completely driven by the API. If a piece of data is displayed or a UI element is enabled/disabled, it's because the API response explicitly instructed it to do so.
- **Client-side validation** should be limited to basic, non-business-critical checks for a better user experience (e.g., checking for empty required fields before sending a request). The authoritative validation will always be on the backend.

## 2. Detailed Refactoring Plan by Component

---

### 2.1. Process Creation (`CadProcesso.vue`) - Concluído

*   **Objective**: Simplify the process creation form by removing all business logic related to unit selection and validation. The backend will determine which units are eligible to participate in a process.

*   **Identified Issues**:
    *   **Client-Side Unit Filtering**: The frontend currently fetches a list of all units and then calls `processosStore.buscarStatusUnidades`. This action hits a backend endpoint that returns a simple list of unit IDs to be disabled. The frontend then has to walk through the unit tree and apply the disabled state. This is inefficient and places business logic on the client.
    *   **Data Structure Manipulation**: The `extrairCodigosUnidades` function is a client-side workaround to a nested data structure for submission, which indicates a mismatch between the API's expectations and the component's data model.

*   **Proposed Changes**:
    *   **Backend (Agent Task)**:
        1.  **Create a new endpoint**: `GET /api/unidades/arvore-com-elegibilidade`. This endpoint replaces the need for separate calls to fetch units and their status.
        2.  **Endpoint Parameters**: It should accept `tipoProcesso` (e.g., 'MAPEAMENTO', 'REVISAO') and an optional `codProcesso` (for when editing an existing process).
        3.  **Business Logic**: The service behind this endpoint must implement the rules for unit eligibility. For example:
            *   For `tipoProcesso = MAPEAMENTO`, a unit is ineligible if it already has an active or recently completed map.
            *   For `tipoProcesso = REVISAO`, a unit is ineligible if it *does not* have a map to be reviewed.
            *   The logic must correctly handle parent/child unit states (e.g., if a parent is ineligible, are its children?).
        4.  **"Smart" DTO Structure**: The endpoint should return the *full* tree of `Unidade` objects. Each `UnidadeDto` in the tree must be enhanced with a new boolean field: `isElegivel`. This flag will be `true` if the unit can be selected for the process, and `false` otherwise.
            ```json
            [
              {
                "codUnidade": 1,
                "sigla": "PRES",
                "isElegivel": false, // Example: Parent unit not selectable
                "filhos": [
                  {
                    "codUnidade": 2,
                    "sigla": "SEC",
                    "isElegivel": true, // This unit can be selected
                    "filhos": []
                  }
                ]
              }
            ]
            ```

    *   **Frontend (Agent Task)**:
        1.  **Refactor `processos` Store**:
            *   Remove the `buscarStatusUnidades` action and the `unidadesDesabilitadas` state property.
            *   Create a new action, e.g., `fetchUnidadesParaProcesso(tipo: string, codProcesso?: number)`, that calls the new `GET /api/unidades/arvore-com-elegibilidade` endpoint.
            *   Store the returned tree in the `unidades` store, as this is now the single source of truth for the unit tree.
        2.  **Refactor `CadProcesso.vue`**:
            *   In the `onMounted` hook and the `watch` on `tipo`, call the new `fetchUnidadesParaProcesso` action.
            *   The `v-if="!unidadesStore.isLoading"` should now correctly show the tree once the single API call is complete.
        3.  **Refactor `ArvoreUnidades.vue` Component**:
            *   Remove the `desabilitadas` prop.
            *   Modify the component to read the `isElegivel` property from each `unidade` object passed in the `unidades` prop.
            *   The checkbox for each unit should be enabled or disabled based on this flag (`:disabled="!unidade.isElegivel"`).
        4.  **Cleanup**:
            *   Remove the `extrairCodigosUnidades` function. The `ArvoreUnidades` component's `v-model` should be configured to directly provide the simple array of selected unit codes (e.g., `[1, 2, 5]`) that the backend API expects.

---

### 2.2. Subprocess Visualization (`SubprocessoView.vue`) - Concluído

*   **Objective**: Transform the view into a pure display component that renders state and options provided by the backend, removing all complex client-side state calculations and authorization logic.

*   **Identified Issues**:
    *   **Dynamic Responsible User Calculation**: `unidadeComResponsavelDinamico` is a complex computed property that manually checks for temporary assignments (`atribuicoes`) and merges them with the original unit data. This is core business logic for determining responsibility and should be centralized in the backend.
    *   **Client-Side State Derivation**: `isSubprocessoEmAndamento` and `etapaAtual` are computed properties that derive the subprocess's state (e.g., "in progress," "step 1") from its `situacao` enum. This is a fragile duplication of the backend's state machine logic. If the backend state machine changes, the frontend will break.
    *   **Client-Side Authorization**: The `irParaAtividadesConhecimentos` function contains explicit authorization logic (`if (perfilStore.perfilSelecionado === Perfil.CHEFE && ...)`), deciding which page to navigate to based on the user's role. This is insecure and brittle. All UI actions should be driven by permissions sent from the backend.

*   **Proposed Changes**:
    *   **Backend (Agent Task)**:
        1.  **Enhance the `SubprocessoDetalhesDto`**: The DTO returned by the subprocess details endpoint must be enriched with all the information the UI needs, pre-calculated.
        2.  **Responsible User Logic**: The `SubprocessoService` must resolve the current responsible user. It should check for any active `AtribuicaoTemporaria` for the subprocess's unit and populate the `responsavel` field in the DTO accordingly. The DTO should have both `titular` (permanent head) and `responsavel` (current person in charge) fields, clearly populated with the correct `UsuarioDto`.
        3.  **Add Pre-Calculated State Indicators**:
            *   `isEmAndamento: boolean`
            *   `etapaAtual: number | null`
            *   `situacaoLabel: string` (A human-readable status, e.g., "Aguardando validação do chefe")
            *   `dataLimite: string` (Formatted date)
        4.  **Implement a Comprehensive `permissoes` Object**: This is the most critical change. The service layer must, for the currently authenticated user, calculate a set of permissions for this specific subprocess.
            *   This logic will involve checking the user's `Perfil`, their associated `Unidade`, and the `Subprocesso`'s current `situacao`.
            *   The `permissoes` object in the DTO should be exhaustive.
                ```json
                "permissoes": {
                  "podeVerPagina": true, // Can the user even see this page?
                  "podeEditarMapa": true, // Can edit activities/competencies
                  "podeVisualizarMapa": true,
                  "podeDisponibilizarCadastro": true,
                  "podeDevolverCadastro": false,
                  "podeAceitarCadastro": false,
                  "podeVisualizarDiagnostico": false,
                  "podeAlterarDataLimite": true
                }
                ```
    *   **Frontend (Agent Task)**:
        1.  **Major Simplification of `SubprocessoView.vue`**:
            *   Remove the `unidadeComResponsavelDinamico`, `titularDetalhes`, `responsavelDetalhes`, `isSubprocessoEmAndamento`, and `etapaAtual` computed properties.
            *   The `SubprocessoHeader` component should be updated to receive the `titular` and `responsavel` user objects directly from the DTO.
        2.  **Refactor `SubprocessoCards.vue`**:
            *   This component should receive the entire `permissoes` object as a prop.
            *   Every button and link within the cards (e.g., "Acessar Atividades," "Visualizar Diagnóstico") must be wrapped in a `v-if` directive that checks the corresponding flag in the `permissoes` object. For example: `v-if="permissoes.podeEditarMapa"`.
        3.  **Remove Navigational Logic**:
            *   The `irParaAtividadesConhecimentos` function must be removed. The button that triggers this navigation will now only be rendered if the user has permission. The `@click` handler should be a simple, direct `router.push(...)` to the appropriate page, as the decision of *which* page (edit vs. view) is now implicitly handled by which button was rendered.
        4.  **Pinia Store Cleanup**:
            *   The `atribuicaoTemporariaStore` might no longer be needed by this view, as the responsible user is provided by the backend. Review and remove unnecessary store dependencies.

---

### 2.3. Dashboard / Main Panel (`PainelView.vue`)

*   **Objective**: Offload sorting, filtering, and navigation logic to the backend, ensuring the user is presented with a view that is already tailored to them.

*   **Identified Issues**:
    *   **Client-Side Sorting**: `processosOrdenados` and `alertasOrdenados` are computed properties that perform sorting on the client. For large datasets, this is inefficient. Sorting should be a data presentation concern handled by the backend.
    *   **Client-Side Navigation Logic**: The `abrirDetalhesProcesso` function contains complex conditional logic to determine the correct navigation target based on the user's `Perfil` and the `situacao` of the process. This is authorization and business logic that makes the frontend brittle.

*   **Proposed Changes**:
    *   **Backend (Agent Task)**:
        1.  **Enhance Painel API**: Modify the `GET /api/painel/processos` and `GET /api/painel/alertas` endpoints to handle sorting.
            *   The endpoints should accept `sort` and `order` query parameters (e.g., `?sort=descricao&order=asc`).
            *   The backend service should apply this sorting to the database query itself, ensuring a performant and scalable solution.
        2.  **Add "Smart" Navigation Link to DTOs**: This is the key change to simplify the frontend.
            *   The `ProcessoResumoDto` must be enhanced with a new field, `linkDestino`.
            *   The backend service will compute the correct destination for a user clicking on this process. The logic will be identical to what is currently in `abrirDetalhesProcesso`. For example:
                *   If `user.perfil == ADMIN` and `processo.situacao == CRIADO`, `linkDestino` would be `"/processos/cadastro?codProcesso=123"`.
                *   If `user.perfil == CHEFE`, `linkDestino` would be `"/subprocessos/123/unidade/ABC"`.
                *   If the user should not be able to navigate anywhere, this field can be `null`.
            *   The `AlertaDto` should similarly be enhanced with a `linkDestino` field.

    *   **Frontend (Agent Task)**:
        1.  **Refactor `PainelView.vue`**:
            *   Remove the `processosOrdenados` and `alertasOrdenados` computed properties.
            *   Remove the local state variables `criterio`, `asc`, `alertaCriterio`, and `alertaAsc`.
            *   The `ordenarPor` and `ordenarAlertasPor` methods should now trigger a new API call to the `processos` and `alertas` stores, passing the new sorting parameters. The stores will then re-fetch the data from the backend, which will return the already-sorted list.
            *   **Crucially, gut the `abrirDetalhesProcesso` function**. It should be simplified to:
                ```javascript
                function abrirDetalhesProcesso(processo: ProcessoResumo) {
                  if (processo.linkDestino) {
                    router.push(processo.linkDestino);
                  }
                  // Optional: handle the case where linkDestino is null, e.g., show a notification.
                }
                ```
        2.  **Refactor `TabelaProcessos.vue` and `TabelaAlertas.vue`**:
            *   The `@click` handler on a table row should simply emit the `processo` object. The parent (`PainelView.vue`) will handle the navigation using the new `linkDestino` property.
            *   The table headers should still emit the `ordenar` event, which will now trigger the API refetch in the parent.

---

### 2.4. Action Modals (e.g., `DisponibilizarMapaModal`, `AceitarMapaModal`)

*   **Objective**: Ensure that all actions triggered by modals are authoritatively validated by the backend and that the UI proactively prevents invalid actions.

*   **Identified Issues**:
    *   **Implicit Backend Validation**: Components like `AceitarMapaModal` gather user input, but the critical validation (e.g., "Is this user a CHEFE of the correct unit?", "Is the subprocess in the 'MAPA_ELABORADO' state?") happens implicitly in the backend, and failures are handled reactively.
    *   **Superficial Client-Side Validation**: `DisponibilizarMapaModal` disables its confirm button if a date is missing. This is good UX, but it doesn't prevent the user from selecting a date in the past or a date that violates other business rules.

*   **Proposed Changes**:
    *   **Backend (Agent Task)**:
        1.  **Enforce Strict Validation in Service Layers**: For every action (e.g., `disponibilizarMapa`, `aceitarMapa`), the corresponding service method in the backend must be the single source of truth for all business rules.
            *   It must validate the user's permissions via the `SecurityContext`.
            *   It must validate the state of the entity (e.g., `subprocesso.getSituacao()`).
            *   It must validate the input data (e.g., ensuring the `dataLimite` is in the future).
            *   It must throw specific, custom exceptions (`EstadoInvalidoException`, `PermissaoNegadaException`) that the `RestExceptionHandler` can map to appropriate HTTP status codes (409 Conflict, 403 Forbidden, 400 Bad Request).
        2.  **Use Permissions to Drive UI State**: The `permissoes` object in the `SubprocessoDetalhesDto` (described in section 2.2) is the key to proactive UI. The flags in this object (e.g., `podeDisponibilizarCadastro`, `podeAceitarCadastro`) are the result of the backend's validation logic.

    *   **Frontend (Agent Task)**:
        1.  **Proactive Disabling of Actions**: The buttons that *open* these modals should be disabled based on the `permissoes` object. For instance, the "Disponibilizar Mapa" button in `SubprocessoCards.vue` should have a `v-if="permissoes.podeDisponibilizarCadastro"`. If the user doesn't have permission to perform the action, they should not even be able to open the modal.
        2.  **Simplify Modals**:
            *   The modals themselves should be simplified to focus solely on gathering the necessary input (like the `dataLimite` or an `observacao`).
            *   Keep basic UX validation (like the disabled button in `DisponibilizarMapaModal`).
        3.  **Robust Error Handling**:
            *   When the user confirms an action, the frontend store (`subprocessos.ts`, etc.) will make the API call.
            *   The store must have a robust `catch` block that can handle the specific error responses from the backend.
            *   If the backend returns a 409 Conflict with a message like "O subprocesso não está no estado correto," the frontend should display this user-friendly message in a notification. This provides clear feedback to the user about *why* their action failed.

---

## 3. General Guidelines and Store Refactoring

This section provides a checklist for applying the "Thin Frontend" philosophy to any component or view in the application.

### 3.1. Component Refactoring Checklist
When analyzing any Vue component, ask the following questions:
-   **Is it calculating state?** Any `computed` property that decodes an enum, combines multiple data points to create a new state (like `isSubprocessoEmAndamento`), or derives information that isn't directly for presentation should be a red flag.
    -   **Action**: Move this logic to the backend and add the resulting value to the relevant DTO.
-   **Is it checking roles or permissions?** Any `v-if` or script logic that checks `perfilStore.perfilSelecionado` or other user-related properties to decide if something should be shown or enabled is a security risk.
    -   **Action**: Move this logic to the backend. Enhance the relevant DTO with a `permissoes` object and use its boolean flags to drive the UI.
-   **Is it manipulating data for the API?** Any function that reformats or restructures data before sending it to the backend (like `extrairCodigosUnidades`) indicates a mismatch that should be fixed.
    -   **Action**: Adjust the backend endpoint to accept the data in a format that is more convenient for the frontend, or adjust the frontend component to naturally produce the expected format.
-   **Is it performing complex validation?** Any validation beyond "is this field empty?" is likely business logic.
    -   **Action**: Move the validation to the backend service layer. The frontend should rely on the API's error responses for feedback.

### 3.2. Pinia Store Refactoring
The Pinia stores should be lean and focused. Their only responsibilities should be:
1.  **State Management**: Holding the data fetched from the API (e.g., `processosPainel`, `processoDetalhe`).
2.  **API Abstraction**: Exposing actions that call frontend services (e.g., `fetchProcessosPainel`, `criarProcesso`).

**Agent Task**: Review every Pinia store (`processos.ts`, `alertas.ts`, etc.) and remove any logic that goes beyond these two responsibilities. This includes getters that perform complex calculations and actions that contain business rules. The stores should be simple proxies between the components and the services.
