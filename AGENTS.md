# Guia para Agentes de Desenvolvimento - SGC

Este documento resume as diretrizes essenciais para o desenvolvimento no projeto SGC. O foco está nas **convenções específicas** do projeto que diferem dos padrões genéricos.

### Regras gerais

* **Idioma:** Todo o código (variáveis, métodos), comentários, mensagens de erro e documentação deve ser em **Português brasileiro**.
* **Identificadores:** Use sempre `codigo` em vez de `id` para chaves primárias e referências.
* **Convenções de Nomenclatura:**
    * **Backend:** Classes `PascalCase`, métodos `camelCase`. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`
    * Exceções iniciam com `Erro` (ex: `ErroNegocio`)
    * **Frontend:** Componentes `PascalCase` (`ProcessoCard.vue`), arquivos TS `camelCase`. Stores seguem `use{Nome}Store`

* **Qualidade de Código:**
    * **Limite de Parâmetros:** Métodos devem ter no máximo **3 parâmetros**. Se ultrapassar, use um objeto de transporte (DTO de 'command').
    * **Código depreciado:** Código marcado como `@Deprecated` deve ser removido sumariamente assim que não houver mais dependências internas (especialmente após consolidações arquiteturais).

### Referências e Padrões

Para detalhes técnicos, consulte:
* Regras de acesso /etc/docs/regras-acesso.md
* Regras para ajustes em testes e2e e correção de bugs: /etc/docs/regras-e2e.md
* README.md de cada camada para responsabilidades específicas

### Backend (Java 25 / Spring Boot 4)

* **REST Não-Padrão:**
    * `GET` para consultas.
    * `POST` para criação.
    * `POST` com sufixo semanticamente claro para atualizações, ações de workflow e exclusão (ex:
      `/api/processos/{codigo}/iniciar`, `/api/processos/{codigo}/excluir`).
* **Persistence:** Tabelas em `UPPER_CASE`, colunas em `snake_case`. Enums como `STRING`.
* **Controle de Acesso (Security):**
    * Baseado nas regras documentadas em [`regras-acesso.md`](/etc/reqs/regras-acesso.md):
    * **Leitura**: Hierarquia da Unidade responsável
    * **Escrita**: Localização atual do Subprocesso (com algumas exceções para admin quando não há processo envolvido)
    * **Implementação:** `SgcPermissionEvaluator` (implementa `PermissionEvaluator` do Spring Security)
    * **Controllers:** Use `@PreAuthorize` para verificações
    * **Hierarquia:** `HierarquiaService` para verificações de hierarquia de unidades
    * **Perfis:** `ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`

### Frontend (Vue 3.5 / TypeScript)

* **Padrão de componentes:** Use `<script setup lang="ts">` e **BootstrapVueNext**.
* **Estado:** **Pinia** utilizando "Setup stores" (com `ref` e `computed`).
* **Roteamento:** Modularizado (cada módulo tem seu arquivo `.routes.ts`).

### Comandos e Testes

* **Backend:** `./gradlew :backend:test` (JUnit 6 + Mockito + H2).
* **Frontend:** `npm run typecheck` (inclui e2e), `npm run lint` (inclui e2e), `npm run test:unit` (Vitest).
* **E2E:** Playwright `npx playwright test`