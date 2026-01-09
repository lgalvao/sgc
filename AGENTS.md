# Guia para Agentes de Desenvolvimento - SGC

Este documento resume as diretrizes essenciais para o desenvolvimento no projeto SGC. O foco está nas **convenções específicas** do projeto que diferem dos padrões genéricos.

## 1. Regras Fundamentais

* **Idioma:** Todo o código (variáveis, métodos), comentários, mensagens de erro e documentação deve ser em **Português Brasileiro**.
* **Identificadores:** Use sempre `codigo` em vez de `id` para chaves primárias e referências.
* **Convenções de Nomenclatura:**
  * **Backend:** Classes `PascalCase`, métodos `camelCase`. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`. Exceções iniciam com `Erro` (ex: `ErroNegocio`).
  * **Frontend:** Componentes `PascalCase` (`ProcessoCard.vue`), arquivos TS `camelCase`. Stores seguem `use{Nome}Store`.

## 2. Backend (Java / Spring Boot 4)

* **Arquitetura:** Módulos de domínio com uma **Service Facade** (ex: `MapaService`) que orquestra serviços especializados. Controllers interagem *apenas* com a Facade.
* **Comunicação entre Módulos:** Use **Spring Events** para desacoplamento (ex: `eventPublisher.publishEvent(new EventoProcessoIniciado(codigo))`).
* **REST Não-Padrão:**
  * `GET` para consultas.
  * `POST` para criação.
  * `POST` com sufixo semanticamente claro para atualizações, ações de workflow e exclusão (ex: `/api/processos/{id}/iniciar`, `/api/processos/{id}/excluir`).
* **DTOs:** NUNCA exponha entidades JPA. Use DTOs e Mappers (MapStruct).
* **Persistence:** Tabelas em `UPPER_CASE`, colunas em `snake_case`. Enums como `STRING`.
* **Controle de Acesso (Security):**
  * **SEMPRE** use a arquitetura centralizada: `Controller → AccessControlService → Services`
  * **Controllers:** Use `@PreAuthorize` para verificações básicas de role
  * **Services:** NUNCA adicione verificações de acesso diretas. Use `AccessControlService.verificarPermissao(usuario, acao, recurso)`
  * **Políticas:** Crie `AccessPolicy` específica para cada tipo de recurso (Processo, Subprocesso, Atividade, Mapa)
  * **Ações:** Use enum `Acao` do pacote `sgc.seguranca.acesso`
  * **Hierarquia:** Use `HierarchyService` para verificações de hierarquia de unidades
  * **Auditoria:** Todas as decisões de acesso são automaticamente logadas por `AccessAuditService`
  * **Documentação completa:** Ver `SECURITY-REFACTORING.md` e `security-refactoring-plan.md`

## 3. Frontend (Vue 3.5 / TypeScript)

* **Padrão de componentes:** Use `<script setup lang="ts">` e **BootstrapVueNext**.
* **Estado:** **Pinia** utilizando "Setup Stores" (com `ref` e `computed`).
* **Camadas:** `View -> Store -> Service -> API`. Views são inteligentes; Componentes são majoritariamente apresentacionais (Props/Emits).
* **Erros:** Use `normalizeError` em services/stores. Componentes decidem como exibir (preferencialmente `BAlert` inline para erros de negócio).
* **Roteamento:** Modularizado (cada módulo tem seu arquivo `.routes.ts`).

## 4. Comandos e Testes

* **Backend:** `./gradlew :backend:test` (JUnit 5 + Mockito + H2).
* **Frontend:** `npm run typecheck`, `npm run lint`, `npm run test:unit` (Vitest).
* **E2E:** Playwright (consulte `/regras/e2e_regras.md`).

## 5. Referências e Padrões Detalhados

Para detalhes técnicos e exemplos de código, consulte:

* [Backend Patterns](/regras/backend-padroes.md)
* [Frontend Patterns](/regras/frontend-padroes.md)
* [Regras para execução de testes e2e e correção de bugs](/regras/e2e_regras.md)
* `README.md` de cada módulo e diretório para responsabilidades específicas.
