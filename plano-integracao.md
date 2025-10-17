# Plano de Integração: Frontend (Vue) e Backend (Java/REST)

Este documento detalha a estratégia e os passos necessários para integrar o frontend Vue.js com o backend Java/REST do sistema SGC. O objetivo é realizar uma migração gradual e segura, substituindo os dados mockados por chamadas reais à API, garantindo a estabilidade e a manutenibilidade da aplicação.

## 1. Visão Geral da Estratégia

A integração será realizada de forma iterativa, módulo por módulo (ex: Processos, Subprocessos, Mapas). A abordagem consiste em:

1.  **Configurar a comunicação** entre o servidor de desenvolvimento do frontend e o backend.
2.  **Centralizar o acesso à API** para reutilização de código e fácil manutenção.
3.  **Implementar o fluxo de autenticação** realista -- mas por enquanto mockado no nível de serviços
4.  **Substituir os dados mockados** por chamadas à API dentro dos `stores` do Pinia.
5.  **Mapear os DTOs** do backend para os tipos de dados do frontend.
6.  **Definir um padrão para tratamento de erros** da API.

## 2. Passos Detalhados da Integração

### 2.1. Configuração do Proxy do Servidor de Desenvolvimento

O `vite.config.js` já possui a configuração de proxy necessária. Todas as requisições feitas pelo frontend para o caminho `/api` serão automaticamente redirecionadas para o servidor backend em `http://localhost:8080`.

### 2.2. Criação de um Cliente de API Centralizado

Para evitar a duplicação de código e centralizar a lógica de requisição, será criado um "composable" do Vue.

**Local:** `frontend/src/composables/useApi.ts`

**Responsabilidades:**

*   Encapsular o `fetch` ou uma biblioteca como `axios`.
*   Definir a URL base da API (`/api`).
*   Anexar automaticamente o `Content-Type: application/json` em requisições `POST`, `PUT`, `PATCH`.
*   Gerenciar o token de autenticação (se aplicável, em uma etapa futura).
*   Centralizar o tratamento de erros de rede e respostas da API.

### 2.3. Fluxo de Autenticação

O fluxo de login mockado será substituído pelo fluxo real de autenticação do backend.

1.  **Componente:** `frontend/src/views/Login.vue`
    *   Modificar o formulário de login para chamar a action de autenticação do `store` de perfil.
2.  **Store:** `frontend/src/stores/perfil.ts`
    *   Criar uma `action` chamada `login(credenciais)`.
    *   Esta action fará uma requisição `POST` para o endpoint `/api/login` (ou o endpoint de autenticação correspondente) usando o `useApi`.
    *   Em caso de sucesso, armazenará os dados do usuário e o estado de "autenticado" no `state` do store.
    *   Em caso de falha, armazenará a mensagem de erro para ser exibida na tela de login.
3.  **Navegação:**
    *   O `router.ts` deve ser configurado para verificar o estado de autenticação no `store` de perfil antes de permitir o acesso a rotas protegidas, redirecionando para `/login` caso o usuário não esteja autenticado.

### 2.4. Estratégia de Migração de Dados (Mock para Real)

A substituição dos dados mockados será feita de forma gradual, store por store. Para cada módulo (ex: `processos.ts`), o processo será:

1.  **Identificar o Mock:** Localizar o arquivo `.json` correspondente em `frontend/src/mocks/`.
2.  **Identificar o Endpoint:** Mapear a funcionalidade ao endpoint da API do backend (ex: `GET /api/processos`).
3.  **Atualizar o Store (Pinia):**
    *   Localizar o store correspondente (ex: `frontend/src/stores/processos.ts`).
    *   Modificar a `action` que carrega os dados (ex: `carregarProcessos`).
    *   Remover a importação do arquivo JSON mockado.
    *   Utilizar o `useApi` para fazer a chamada ao endpoint real da API.
    *   Tratar a resposta da API:
        *   Em caso de sucesso, usar um `mapper` (se necessário) para transformar o DTO do backend no formato esperado pelo frontend.
        *   Salvar os dados transformados no `state` do store.
        *   Em caso de erro, definir um estado de erro no store.

**Exemplo de Ordem de Migração Sugerida:**

1.  `unidades.ts` (`GET /api/unidades`)
2.  `processos.ts` (`GET /api/processos`)
3.  `subprocessos.ts` (`GET /api/processos/{id}/subprocessos`)
4.  `atividades.ts` (`GET /api/subprocessos/{id}/cadastro`)
5.  ... e assim por diante para os demais stores.

### 2.5. Mapeadores (Mappers)

O diretório `frontend/src/mappers` será fundamental para desacoplar o frontend do backend.

*   Para cada DTO complexo do backend, um `mapper` correspondente será criado.
*   **Responsabilidade:** Transformar a estrutura de dados da resposta da API na estrutura de dados que os componentes Vue esperam. Isso evita a necessidade de refatorar os componentes caso a API mude.

### 2.6. Tratamento de Erros

O `useApi` centralizará o tratamento primário de erros.

*   **Erros 401 (Não Autorizado):** O `useApi` deve interceptar este erro e acionar uma `action` no `store` de perfil para deslogar o usuário e redirecioná-lo para a página de login.
*   **Erros 403 (Proibido):** Exibir uma notificação global ou uma página de "Acesso Negado".
*   **Erros 404 (Não Encontrado):** Os componentes devem tratar este erro, exibindo mensagens como "Processo não encontrado".
*   **Erros 500 (Erro Interno do Servidor):** Exibir uma mensagem de erro genérica e amigável para o usuário.

Um `store` de `alertas` ou `notificacoes` pode ser usado para exibir mensagens de erro de forma consistente em toda a aplicação.

### 2.7. Testes

A estratégia de testes deve ser atualizada:

*   **Testes Unitários (Vitest):** Os testes de stores devem ser adaptados para mockar as chamadas do `useApi`, permitindo testar a lógica do store (actions, mutations) isoladamente.
*   **Testes End-to-End (Playwright):** Devem ser criados novos testes E2E para os fluxos mais críticos (login, cadastro de processo, etc.). Como o Playwright opera contra um ambiente real, ele testará a integração completa, desde a UI até o banco de dados do backend.
*   **Verificação Contínua:** Para cada funcionalidade integrada, o agente deve rodar os testes unitários e/ou E2E relacionados para garantir a estabilidade e o correto funcionamento.

## 3. Conclusão

Este plano fornece um roteiro claro para a integração do frontend com o backend. A abordagem gradual e modular minimiza os riscos e permite a entrega contínua de valor. A chave para o sucesso será a comunicação constante entre as equipes de frontend e backend para garantir o alinhamento dos contratos de API (endpoints, DTOs). Para cada funcionalidade integrada, o agente deve propor commits intermediários, garantindo rastreabilidade e facilitando a revisão do código.

## 4. Recomendações para Robustez Adicional

Para aumentar ainda mais a robustez e a resiliência da aplicação, considere as seguintes recomendações:

*   **Versionamento da API:** Estabelecer uma política clara de versionamento da API para gerenciar mudanças e evitar quebras de compatibilidade entre frontend e backend.
*   **Mecanismo de Retry/Backoff:** Implementar um mecanismo de retry com backoff exponencial no `useApi` para lidar com falhas de rede temporárias ou sobrecarga do servidor, aumentando a resiliência das chamadas.
*   **Cache de Dados:** Para endpoints que retornam dados que não mudam com frequência, considere implementar um cache no frontend (no `useApi` ou nos stores) para melhorar a performance e reduzir a carga no backend.
*   **Estados de Carregamento (Loading States):** Gerenciar explicitamente os estados de carregamento (`isLoading`, `isError`) nos stores e componentes para fornecer feedback visual ao usuário durante as operações assíncronas.
*   **Validação de Dados Abrangente:** Reforçar a validação de dados tanto no frontend quanto no backend para garantir a integridade e consistência das informações.
*   **Documentação de Endpoints:** Manter uma documentação clara e atualizada dos endpoints da API (utilizando ferramentas como Swagger/OpenAPI) para facilitar a comunicação e o alinhamento entre as equipes de frontend e backend.