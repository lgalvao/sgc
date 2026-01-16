# Guia para Agentes de Desenvolvimento - SGC

Este documento resume as diretrizes essenciais para o desenvolvimento no projeto SGC. O foco está nas **convenções específicas** do projeto que diferem dos padrões genéricos.

## 1. Regras Fundamentais

* **Idioma:** Todo o código (variáveis, métodos), comentários, mensagens de erro e documentação deve ser em **Português Brasileiro**.
* **Identificadores:** Use sempre `codigo` em vez de `id` para chaves primárias e referências.
* **Convenções de Nomenclatura:**
  * **Backend:** Classes `PascalCase`, métodos `camelCase`. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`. Exceções iniciam com `Erro` (ex: `ErroNegocio`).
  * **Frontend:** Componentes `PascalCase` (`ProcessoCard.vue`), arquivos TS `camelCase`. Stores seguem `use{Nome}Store`.

* **Qualidade de Código:**
  * **Limite de Parâmetros:** Métodos devem ter no máximo **7 parâmetros**. Se ultrapassar, use um objeto de transporte (Record ou DTO).
  * **Código Depreciado:** Código marcado como `@Deprecated` deve ser removido sumariamente assim que não houver mais dependências internas (especialmente após consolidações arquiteturais).

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
* **Logging:**
  * **NAO** use `console.log`, `console.warn`, ou `console.debug` em código de produção
  * **USE** o logger estruturado: `import { logger } from '@/utils'`
  * **Métodos disponíveis:**
    * `logger.info(message, ...args)` - Informações gerais (apenas em desenvolvimento)
    * `logger.warn(message, ...args)` - Avisos importantes
    * `logger.error(message, ...args)` - Erros críticos
    * `logger.debug(message, ...args)` - Debug detalhado (apenas em desenvolvimento)
  * **ESLint:** Configurado para bloquear `console.*` (exceto `console.error` para casos extremos)
  * **Exemplo:**
    ```typescript
    // ❌ ERRADO
    console.log('Usuário logado:', usuario);
    
    // ✅ CORRETO
    logger.info('Usuário logado:', usuario);
    ```

## 4. Comandos e Testes

* **Backend:** `./gradlew :backend:test` (JUnit 5 + Mockito + H2).
* **Frontend:** `npm run typecheck`, `npm run lint`, `npm run test:unit` (Vitest).
* **E2E:** Playwright (consulte `/regras/e2e_regras.md`).

## 5. Padrões Arquiteturais (ADRs)

O SGC segue padrões arquiteturais bem definidos, documentados em ADRs (Architectural Decision Records):

* **[ADR-001: Facade Pattern](/docs/adr/ADR-001-facade-pattern.md)** - ✅ Implementado
  * Controllers usam APENAS Facades, nunca Services especializados diretamente
  * Facades orquestram operações complexas delegando para Services especializados
  * Exemplo: `ProcessoFacade`, `SubprocessoFacade`, `MapaFacade`, `AtividadeFacade`
  
* **[ADR-002: Unified Events Pattern](/docs/adr/ADR-002-unified-events.md)** - ✅ Implementado
  * Eventos de domínio para comunicação assíncrona entre módulos
  * Padrão unificado: `EventoTransicaoSubprocesso` (design ⭐)
  * Exemplo: `EventoProcessoCriado`, `EventoProcessoIniciado`, `EventoMapaAlterado`
  
* **[ADR-003: Security Architecture](/docs/adr/ADR-003-security-architecture.md)** - ✅ Implementado
  * Arquitetura centralizada de controle de acesso em 3 camadas
  * `AccessControlService` centraliza TODAS as verificações de permissão
  * `AccessPolicy` especializada por tipo de recurso (Processo, Subprocesso, Atividade, Mapa)
  * `HierarchyService` para verificações de hierarquia de unidades
  * `AccessAuditService` para auditoria completa de decisões de acesso
  * **CRÍTICO:** Services NUNCA fazem verificações de acesso diretas
  
* **[ADR-004: DTO Pattern](/docs/adr/ADR-004-dto-pattern.md)** 
  * DTOs obrigatórios em TODAS as APIs REST
  * Entidades JPA NUNCA são expostas diretamente
  * Mappers implementados com MapStruct para conversão Entidade ↔ DTO
  * **Taxonomia de DTOs:**
    * `*Request` - Entrada de API (com Bean Validation)
    * `*Response` - Saída de API (sem validação)
    * `*Command` - Ação entre Services (record imutável)
    * `*Query` - Parâmetros de busca (record imutável)
    * `*View` - Projeções reutilizáveis (record imutável)
    * `*Dto` - Mapeamento interno entre camadas (class)
    * `Evento*` - Spring ApplicationEvent (prefixo em português)
  * **Regras:**
    * Validação com Bean Validation (`@NotNull`, `@Valid`) apenas em `*Request`
    * Preferir `record` para DTOs imutáveis, `class` quando mutabilidade é necessária
    * Lombok: `@Builder` para todos; **`@Data` está PROIBIDO**; classes usam `@Getter` + `@Builder`; preferir `record`
  * **Documentação completa:** Ver [`backend/regras-dtos.md`](/backend/regras-dtos.md)

* **[ADR-005: Controller Organization](/docs/adr/ADR-005-controller-organization.md)** - ✅ Implementado
  * Controllers organizados por workflow phase, não consolidados em arquivos grandes
  * Separação clara: CRUD, Cadastro, Mapa, Validação
  * Mantém arquivos de tamanho gerenciável (~200-300 linhas)
  * Melhor navegabilidade, testabilidade e documentação Swagger
  * Aderência ao Single Responsibility Principle

## 6. Referências e Padrões Detalhados

Para detalhes técnicos e exemplos de código, consulte:

* **Padrões de Código:**
  * [Backend Patterns](/regras/backend-padroes.md)
  * [Frontend Patterns](/regras/frontend-padroes.md)
  * [Regras de DTOs](/backend/regras-dtos.md) - Taxonomia e convenções de DTOs
  * [Regras para execução de testes e2e e correção de bugs](/regras/e2e_regras.md)

* **Arquitetura e Decisões:**
  * [ARCHITECTURE.md](/docs/ARCHITECTURE.md) - Visão geral da arquitetura
  * [ADRs](/docs/adr/) - Decisões arquiteturais documentadas

* **Módulo-Específico:**
  * `README.md` de cada módulo e diretório para responsabilidades específicas
  * `package-info.java` em cada pacote para documentação detalhada