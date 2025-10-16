# Plano de Integração: Frontend (Vue) e Backend (Java/REST)

Este documento detalha a estratégia e os passos necessários para integrar o frontend Vue.js com o backend Java/REST do sistema SGC. O objetivo é realizar uma migração gradual e segura, substituindo os dados mockados por chamadas reais à API, garantindo a estabilidade e a manutenibilidade da aplicação.

**Status Atual:** A integração inicial foi concluída. O fluxo de autenticação e o carregamento de dados para os módulos de `Unidades` e `Processos` foram migrados para usar a API real. Os demais módulos ainda utilizam dados mockados e seguirão o mesmo padrão de migração.

## 1. Visão Geral da Estratégia

A integração será realizada de forma iterativa, módulo por módulo (ex: Processos, Subprocessos, Mapas). A abordagem consiste em:

1.  **[CONCLUÍDO]** **Configurar a comunicação** entre o servidor de desenvolvimento do frontend e o backend.
2.  **[CONCLUÍDO]** **Centralizar o acesso à API** para reutilização de código e fácil manutenção.
3.  **[CONCLUÍDO]** **Implementar o fluxo de autenticação** realista.
4.  **[EM ANDAMENTO]** **Substituir os dados mockados** por chamadas à API dentro dos `stores` do Pinia.
5.  **Mapear os DTOs** do backend para os tipos de dados do frontend.
6.  **Definir um padrão para tratamento de erros** da API.

## 2. Passos Detalhados da Integração

### 2.1. Configuração do Proxy do Servidor de Desenvolvimento

**[CONCLUÍDO]** O `vite.config.js` já possui a configuração de proxy necessária. Todas as requisições feitas pelo frontend para o caminho `/api` serão automaticamente redirecionadas para o servidor backend em `http://localhost:8080`.

### 2.2. Criação de um Cliente de API Centralizado

**[CONCLUÍDO]** Foi criado um "composable" `useApi` para centralizar a lógica de requisição.

**Local:** `frontend/src/composables/useApi.ts`

**Responsabilidades:**
*   Encapsula o `fetch`.
*   Define a URL base da API (`/api`).
*   Anexa o cabeçalho `Content-Type: application/json`.
*   Centraliza o tratamento de erros básicos.

### 2.3. Fluxo de Autenticação

**[CONCLUÍDO]** O fluxo de login foi refeito para utilizar a API do backend.

1.  **Componente:** `frontend/src/views/Login.vue` foi modificado para usar o `perfilStore`.
2.  **Store:** `frontend/src/stores/perfil.ts` agora contém as actions `autenticar` e `entrar` que chamam a API.
3.  **Navegação:** O `router.ts` foi configurado com um `beforeEach` para proteger as rotas.

### 2.4. Estratégia de Migração de Dados (Mock para Real)

A substituição dos dados mockados está sendo feita de forma gradual, store por store.

**Ordem de Migração e Status:**

1.  **[CONCLUÍDO]** `unidades.ts` (`GET /api/unidades`)
2.  **[CONCLUÍDO]** `processos.ts` (`GET /api/processos`)
3.  **[CONCLUÍDO]** `subprocessos.ts` (`GET /api/processos/{id}/detalhes`)
4.  **[CONCLUÍDO]** `atividades.ts` (`GET /api/subprocessos/{id}/cadastro`)
5.  ... e assim por diante para os demais stores.

### 2.5. Mapeadores (Mappers)

**[EM ANDAMENTO]** O diretório `frontend/src/mappers` está sendo usado para desacoplar o frontend do backend. Mapeadores para `Processo` já foram implementados.

### 2.6. Tratamento de Erros

**[EM ANDAMENTO]** O `useApi` centraliza o tratamento primário de erros. O `perfilStore` já trata erros de autenticação e os exibe na tela de login. O tratamento de outros erros (401, 403, 404, 500) será refinado conforme a migração avança.

### 2.7. Testes

**[EM ANDAMENTO]** A estratégia de testes foi atualizada:

*   **Testes Unitários (Vitest):** Os testes para os stores `perfil`, `unidades` e `processos` foram adaptados para mockar as chamadas do `useApi`. Os testes para os stores restantes ainda precisam ser atualizados.
*   **Testes End-to-End (Playwright):** **[PENDENTE]** Novos testes E2E precisam ser criados para os fluxos integrados.

## 3. Próximos Passos

Os próximos passos se concentrarão em continuar a migração dos stores restantes, seguindo o padrão já estabelecido:

1.  Migrar `subprocessos.ts`.
2.  Migrar `atividades.ts` e `conhecimentos`.
3.  Migrar os demais stores (`mapas`, `alertas`, etc.).
4.  Criar testes E2E para os fluxos críticos.
5.  Refinar o tratamento de erros em toda a aplicação.