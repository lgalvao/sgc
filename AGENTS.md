# Guia para Agentes de Desenvolvimento - SGC

Este documento resume as diretrizes essenciais para o desenvolvimento no projeto SGC. O foco está nas **convenções
específicas** do projeto que diferem dos padrões genéricos.

## 1. Regras Fundamentais

* **Idioma:** Todo o código (variáveis, métodos), comentários, mensagens de erro e documentação deve ser em **Português
  Brasileiro**.
* **Identificadores:** Use sempre `codigo` em vez de `id` para chaves primárias e referências.
* **Convenções de Nomenclatura:**
    * **Backend:** Classes `PascalCase`, métodos `camelCase`. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`.
      Exceções iniciam com `Erro` (ex: `ErroNegocio`).
    * **Frontend:** Componentes `PascalCase` (`ProcessoCard.vue`), arquivos TS `camelCase`. Stores seguem
      `use{Nome}Store`.

* **Qualidade de Código:**
    * **Limite de Parâmetros:** Métodos devem ter no máximo **3 parâmetros**. Se ultrapassar, use um objeto de
      transporte (Record ou DTO).
    * **Código Depreciado:** Código marcado como `@Deprecated` deve ser removido sumariamente assim que não houver mais
      dependências internas (especialmente após consolidações arquiteturais).

## 2. Backend (Java / Spring Boot 4)

* **Arquitetura:** Módulos de domínio. Facades orquestram múltiplos services quando há lógica de coordenação real.
  Controllers podem injetar services diretamente quando a facade é pass-through (ver ADR-001).
* **Comunicação entre Módulos:** Use **Spring Events** para desacoplamento (ex:
  `eventPublisher.publishEvent(new EventoProcessoIniciado(codigo))`).
* **REST Não-Padrão:**
    * `GET` para consultas.
    * `POST` para criação.
    * `POST` com sufixo semanticamente claro para atualizações, ações de workflow e exclusão (ex:
      `/api/processos/{id}/iniciar`, `/api/processos/{id}/excluir`).
* **Persistence:** Tabelas em `UPPER_CASE`, colunas em `snake_case`. Enums como `STRING`.
* **Controle de Acesso (Security):**
    * Baseado na **"Regra de Ouro"** documentada em [`acesso.md`](/acesso.md):
        * **Leitura**: Hierarquia da Unidade Responsável
        * **Escrita**: Localização Atual do Subprocesso
    * **Implementação:** `SgcPermissionEvaluator` (implementa `PermissionEvaluator` do Spring Security)
    * **Controllers:** Use `@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACAO')")` para verificações
    * **Services:** NÃO fazem verificações de acesso diretas
    * **Hierarquia:** `HierarquiaService` para verificações de hierarquia de unidades
    * **Perfis:** `ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR` (ver `acesso.md` para detalhes)

## 3. Frontend (Vue 3.5 / TypeScript)

* **Padrão de componentes:** Use `<script setup lang="ts">` e **BootstrapVueNext**.
* **Estado:** **Pinia** utilizando "Setup Stores" (com `ref` e `computed`).
* **Camadas:** `View -> Store -> Service -> API`. 
* **Erros:** Use `normalizeError` em services/stores. Componentes decidem como exibir (preferencialmente `BAlert` inline
  para erros de negócio).
* **Roteamento:** Modularizado (cada módulo tem seu arquivo `.routes.ts`).
* **Logging:**
    * **NAO** use `console.log`, `console.warn`, ou `console.debug` em código de produção
    * **USE** o logger estruturado: `import { logger } from '@/utils'`
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
* **E2E:** Playwright (consulte `/e2e/README.md`).
* **Git Hooks:** Existe um hook de `pre-push` local que impede o envio de código se os testes do backend falharem. Agentes de IA devem garantir que os testes passem antes de sugerir ou realizar um push.

## 5. Padrões Arquiteturais (ADRs)

O SGC segue padrões arquiteturais documentados em ADRs (Architectural Decision Records):

* **[ADR-001: Facade Pattern](/backend/etc/docs/adr/ADR-001-facade-pattern.md)** - 🔄 Em Revisão
    * Facades são usadas quando há orquestração real de múltiplos services
    * Controllers podem injetar services diretamente quando a facade é pass-through
    * `SubprocessoFacade` é candidata a simplificação (ver ADR-008)

* **[ADR-002: Unified Events Pattern](/backend/etc/docs/adr/ADR-002-unified-events.md)** - ✅ Implementado
    * Eventos de domínio para comunicação assíncrona entre módulos
    * Padrão unificado: `EventoTransicaoSubprocesso` (design ⭐)
    * Exemplo: `EventoProcessoCriado`, `EventoProcessoIniciado`, `EventoMapaAlterado`

* **[ADR-003: Security Architecture](/backend/etc/docs/adr/ADR-003-security-architecture.md)** - ✅ Implementado (Reescrito 2026-02-24)
    * `SgcPermissionEvaluator` implementa `PermissionEvaluator` do Spring Security
    * "Regra de Ouro": Leitura por Hierarquia, Escrita por Localização
    * Sem framework custom — usa padrão nativo do Spring
    * Regras de negócio detalhadas em [`acesso.md`](/acesso.md)

* **[ADR-004: DTO Pattern](/backend/etc/docs/adr/ADR-004-dto-pattern.md)**
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
        * Lombok: `@Builder` para todos; **`@Data` está PROIBIDO**; classes usam `@Getter` + `@Builder`; preferir
          `record`
    * **Documentação completa:** Ver [`backend/etc/regras/guia-dtos.md`](/backend/etc/regras/guia-dtos.md)

* **[ADR-005: Controller Organization](/backend/etc/docs/adr/ADR-005-controller-organization.md)** - 🔄 Em Revisão
    * Originalmente: Controllers separados por workflow phase
    * Reavaliação (2026-02-24): Consolidação em 1 controller por domínio é preferível
      para controllers thin (que apenas delegam)

* **[ADR-008: Simplification Decisions](/backend/etc/docs/adr/ADR-008-simplification-decisions.md)** - 🚀 Em Andamento
    * Histórico de todas as decisões de simplificação
    * Fases 1-2 concluídas, Fases 4-5 em andamento

## 6. Referências e Padrões Detalhados

Para detalhes técnicos e exemplos de código, consulte:

* **Padrões de Código:**
    * [Backend Patterns](/backend/etc/regras/backend-padroes.md)
    * [Frontend Patterns](/frontend/etc/regras/frontend-padroes.md)
    * [Regras de DTOs](/backend/etc/regras/guia-dtos.md) - Taxonomia e convenções de DTOs
    * [Regras para execução de testes e2e e correção de bugs](/frontend/etc/regras/guia-correcao-e2e.md)

* **Arquitetura e Decisões:**
    * [ADRs](/backend/etc/docs/adr/) - Decisões arquiteturais documentadas

* **Módulo-Específico:**
    * `README.md` de cada módulo e diretório para responsabilidades específicas

## 7. Aprendizados e Diretrizes (Específicos)

- **Testes E2E Seriais**: Em testes marcados como `test.describe.serial`, não se deve executar cenários individualmente (ex: usando `-g "Cenario X"`), pois cada cenário depende do estado deixado pelo anterior. Execute sempre o arquivo de teste completo.

## 8. Apêndice: Aprendizados do Ambiente (Local)

* **Sistema Operacional:** Windows (win32).
* **Shell:** PowerShell via `powershell.exe -NoProfile -Command`.
* **Comandos de Shell:**
    * Comandos `dir` com sintaxe legada do CMD (ex: `dir /s /b`) podem falhar se usados com múltiplos argumentos de busca simultâneos ou se mal interpretados pelo wrapper do PowerShell.
    * Prefira comandos nativos do PowerShell como `Get-ChildItem` para buscas recursivas.
* **Ferramenta `glob`:** Funciona bem para padrões simples, mas pode ser sensível a maiúsculas/minúsculas dependendo da configuração.
* **Deploy:** O script `release-hom.sh` é exclusivo para Linux/Bash e não deve ser executado localmente no Windows.