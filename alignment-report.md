# Relatório de Alinhamento (CDU-22 a CDU-36)

Este documento detalha o status de implementação, cobertura de testes de integração e cobertura de testes E2E para os Casos de Uso 22 a 36.

## Status Geral

| CDU | Título | Backend (Impl.) | Frontend (Impl.) | Teste Integração | Teste E2E | Status Geral |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| **22** | Aceitar cadastros em bloco | ⚠️ Parcial | ✅ Sim | ✅ Sim | ✅ Sim | **Incompleto (Backend)** |
| **23** | Homologar cadastros em bloco | ⚠️ Parcial | ✅ Sim | ✅ Sim | ✅ Sim | **Incompleto (Backend)** |
| **24** | Disponibilizar mapas em bloco | ⚠️ Parcial | ✅ Sim | ✅ Sim | ✅ Sim | **Incompleto (Backend)** |
| **25** | Aceitar mapas em bloco | ⚠️ Parcial | ✅ Sim | ✅ Sim | ✅ Sim | **Incompleto (Backend)** |
| **26** | Homologar mapas em bloco | ⚠️ Parcial | ✅ Sim | ✅ Sim | ✅ Sim | **Incompleto (Backend)** |
| **27** | Alterar data limite | ✅ Sim | ✅ Sim | ✅ Sim | ✅ Sim | **Completo** |
| **28** | Manter atribuição temporária | ✅ Sim | ✅ Sim | ✅ Sim | ✅ Sim | **Completo** |
| **29** | Consultar histórico de processos | ✅ Sim | ✅ Sim | ✅ Sim | ✅ Sim | **Completo** |
| **30** | Manter Administradores | ❌ Não | ❌ Não | ❌ Não | ⚠️ Existente* | **Não Implementado** |
| **31** | Configurar sistema | ✅ Sim | ✅ Sim | ✅ Sim | ✅ Sim | **Completo** |
| **32** | Reabrir cadastro | ⚠️ Parcial | ✅ Sim | ❌ Não | ✅ Sim | **Incompleto (Backend)** |
| **33** | Reabrir revisão de cadastro | ⚠️ Parcial | ✅ Sim | ❌ Não | ✅ Sim | **Incompleto (Backend)** |
| **34** | Enviar lembrete de prazo | ✅ Sim | ✅ Sim | ❌ Não | ✅ Sim | **Falta Teste Int.** |
| **35** | Relatório de andamento | ✅ Sim | ✅ Sim | ❌ Não | ✅ Sim | **Falta Teste Int.** |
| **36** | Relatório de mapas | ✅ Sim | ✅ Sim | ❌ Não | ✅ Sim | **Falta Teste Int.** |

*\* O teste E2E existe, mas provavelmente falha ou testa uma interface inexistente.*

---

## Detalhes das Lacunas

### 1. Ações em Bloco (CDU-22 a CDU-26)
*   **Backend**: O `ProcessoController` possui o endpoint para listar elegíveis (`/subprocessos-elegiveis`), mas **não possui o endpoint para executar a ação em bloco** (`POST /api/processos/{id}/acoes-em-bloco`). O frontend espera este endpoint.
*   **Ação Necessária**: Implementar o endpoint de execução de ações em bloco no `ProcessoController` ou `SubprocessoController`.

### 2. Manter Administradores (CDU-30)
*   **Backend**: A entidade `Administrador` e `AdministradorRepo` existem, mas não há **Service** nem **Controller** para gerenciar (CRUD) administradores.
*   **Frontend**: Não foram encontradas telas ou componentes para listagem e cadastro de administradores.
*   **Testes**: Não há testes de integração.
*   **Ação Necessária**: Implementar CRUD completo (Backend + Frontend) e criar testes.

### 3. Reabertura de Processos (CDU-32 e CDU-33)
*   **Backend**: A lógica de negócio existe no `SubprocessoService` (`reabrirCadastro`, `reabrirRevisaoCadastro`), mas estes métodos **não estão expostos** em nenhum Controller.
*   **Testes**: Não há testes de integração específicos.
*   **Ação Necessária**: Expor métodos de reabertura no `SubprocessoCadastroController` (ou similar) e criar testes.

### 4. Testes de Integração Faltantes (CDU-34, 35, 36)
*   Os recursos estão implementados (Lembretes e Relatórios), mas não possuem arquivos de teste correspondentes em `backend/src/test/java/sgc/integracao/`.
*   **Ação Necessária**: Criar `CDU34IntegrationTest.java`, `CDU35IntegrationTest.java` e `CDU36IntegrationTest.java`.

---

## Instruções Passo-a-Passo para Agente AI

Para alinhar completamente o sistema, execute as seguintes tarefas na ordem apresentada:

### Passo 1: Implementar Backend de Ações em Bloco
1.  No `ProcessoController` (ou novo controller dedicado), crie o endpoint `POST /api/processos/{codigo}/acoes-em-bloco`.
2.  Este endpoint deve receber um DTO contendo: Tipo de Ação (ACEITAR_CADASTRO, HOMOLOGAR_CADASTRO, etc.) e Lista de IDs de Subprocessos/Unidades.
3.  Implemente a delegação para os serviços de `SubprocessoCadastroWorkflowService` e `SubprocessoMapaWorkflowService` para iterar e executar a ação em cada subprocesso.

### Passo 2: Implementar Reabertura (CDU-32/33)
1.  No `SubprocessoCadastroController` (ou onde for mais apropriado semanticamente), adicione endpoints:
    *   `POST /{codigo}/reabrir-cadastro`
    *   `POST /{codigo}/reabrir-revisao`
2.  Ambos devem receber um corpo com a `justificativa` (String).
3.  Conecte aos métodos já existentes no `SubprocessoService`.

### Passo 3: Implementar CRUD de Administradores (CDU-30)
1.  **Backend**:
    *   Crie `AdministradorService` com métodos `listar`, `adicionar(usuarioTitulo)`, `remover(usuarioTitulo)`.
    *   Crie `AdministradorController` com endpoints:
        *   `GET /api/administradores`
        *   `POST /api/administradores`
        *   `DELETE /api/administradores/{titulo}`
2.  **Frontend**:
    *   Crie a view `AdministradoresView.vue` em `frontend/src/views/`.
    *   Adicione a rota no `router/index.ts` (acessível apenas por ADMIN).
    *   Adicione o link no menu de Configurações.

### Passo 4: Criar Testes de Integração Faltantes
1.  Crie `backend/src/test/java/sgc/integracao/CDU30IntegrationTest.java` (após implementar Passo 3).
2.  Crie `backend/src/test/java/sgc/integracao/CDU32IntegrationTest.java` e `CDU33IntegrationTest.java`.
3.  Crie `backend/src/test/java/sgc/integracao/CDU34IntegrationTest.java` (testando `enviarLembrete`).
4.  Crie `backend/src/test/java/sgc/integracao/CDU35IntegrationTest.java` e `CDU36IntegrationTest.java` (testando geração de relatórios).

### Passo 5: Verificar e Corrigir Testes E2E
1.  Execute os testes E2E existentes para estes CDUs (`npm run test:e2e -- cdu-XX`).
2.  Ajuste os seletores e fluxos conforme as novas implementações (especialmente para CDU-30 e Ações em Bloco).
