# Plano de Refatoração de Componentes Vue.js (Atualizado)

**Última atualização:** 2025-12-13
**Versão:** 2.2 (Final)
**Status do Projeto:** ✅ Concluído

Este documento detalha as alterações realizadas nos componentes Vue.js localizados em `frontend/src/components/` e módulos relacionados. O objetivo de remover lógicas de "protótipo" (hardcoded, filtros locais excessivos, mocks), otimizar a integração com o backend real e consolidar as melhorias arquiteturais foi atingido.

---

## Contexto Técnico do Projeto (Atualizado)

### Stack Frontend

- **Vue 3.5** (Composition API com `<script setup>` e TypeScript)
- **Pinia** para gerenciamento de estado (Setup Stores)
- **BootstrapVueNext** para componentes de UI
- **Vitest** para testes unitários com cobertura
- **Vite** como bundler e dev server
- **Axios** para requisições HTTP com interceptors de autenticação e tratamento de erros normalizado
- **Playwright** para testes E2E (15+ specs de CDU + captura de telas)

### Arquitetura de Camadas

```
Views (Páginas) → Stores (Pinia) → Services (Axios) → API REST (Backend)
                      ↓
                 Composables
                      ↓
                  Utils/Mappers
```

**Princípios:**

1. **Views** são "inteligentes": orquestram stores e componentes
2. **Componentes** são "burros": recebem props, emitem eventos
3. **Stores** gerenciam estado global e chamam services
4. **Services** encapsulam chamadas à API
5. **Composables** (`useApi`, `usePerfil`) fornecem lógica reutilizável
6. **Modais** devem receber dados via props ou buscar por IDs específicos (não depender de estado global de listas)

### Estrutura de Diretórios

```
frontend/src/
├── components/      # 25 componentes reutilizáveis
├── views/           # 18 páginas (incluindo 4 de diagnóstico)
├── stores/          # 12 stores Pinia
├── services/        # 12 services de API
├── composables/     # 2 composables (useApi, usePerfil)
├── utils/           # Utilitários (apiError, logger, index)
├── mappers/         # Mapeadores de DTOs
├── types/           # Tipos TypeScript
├── constants/       # Constantes e enums
├── router/          # Configuração de rotas
└── test-utils/      # Utilitários para testes
```

### Testes e Qualidade

- **Testes Unitários Frontend:** `npm run test:unit` (Vitest) — 85+ specs
- **Testes E2E:** `npm run test:e2e` (Playwright) — 15+ specs cobrindo 21 CDUs
- **Linting:** `npm run lint` (ESLint com auto-fix)
- **Type Checking:** `npm run typecheck` (vue-tsc)
- **Qualidade Completa:** `npm run quality:all`
- **Captura de Telas:** `./scripts/capturar-telas.sh` (50+ screenshots automatizadas)

---

## Mudanças Implementadas e Concluídas

### ✅ Tratamento de Erros Padronizado (CONCLUÍDO)

**Implementação completa em `plano-refatoracao-erros.md`:**

1. **Backend:**
   - ✅ Interface `ErroNegocio` com `getCode()`, `getStatus()`, `getDetails()`
   - ✅ Classe base `ErroNegocioBase` para exceções de domínio
   - ✅ `ErroApi` estendido com campos `code` e `traceId`
   - ✅ `RestExceptionHandler` unificado com logging por severidade
   - ✅ Documentação OpenAPI/Swagger atualizada

2. **Frontend:**
   - ✅ `utils/apiError.ts` com `normalizeError()`, `notifyError()`, `shouldNotifyGlobally()`
   - ✅ Tipos `NormalizedError`, `ErrorKind`, `ApiErrorPayload`
   - ✅ Interceptor Axios refatorado para usar normalizador
   - ✅ `useApi` composable expõe `normalizedError` e `error` (string)
   - ✅ Stores usam padrão `lastError: NormalizedError | null`
   - ✅ Helpers `existsOrFalse()` e `getOrNull()` para casos 404
   - ✅ Sem `window.alert()` ou `window.confirm()` no código (substituídos por `BAlert` e toasts)

### ✅ Refatoração de Componentes (CONCLUÍDO)

Todos os componentes identificados como prioritários foram refatorados:

1. **`ImportarAtividadesModal.vue`**
   - ✅ Removida filtragem client-side de processos.
   - ✅ Implementada busca server-side via `processosStore.buscarProcessosFinalizados()`.

2. **`ImpactoMapaModal.vue`**
   - ✅ Desacoplado de `processoDetalhe` global.
   - ✅ Recebe `codSubprocesso` obrigatoriamente via prop.

3. **`SubprocessoCards.vue`**
   - ✅ Removido uso de `useRoute()`.
   - ✅ Props de navegação tornadas obrigatórias.

4. **`AcoesEmBlocoModal.vue` & `ModalAcaoBloco.vue`**
   - ✅ `AcoesEmBlocoModal.vue` (não utilizado) foi excluído.
   - ✅ `ModalAcaoBloco.vue` auditado e limpo.

5. **`ArvoreUnidades.vue`**
   - ✅ Removido hardcoding de `SEDOC` e `codigo === 1`.
   - ✅ Implementada lógica genérica com prop `ocultarRaiz`.

6. **`HistoricoAnaliseModal.vue`**
   - ✅ Corrigidas race conditions no watch.
   - ✅ Implementada limpeza de dados ao fechar.

7. **`TabelaProcessos.vue`**
   - ✅ Validada ordenação server-side.
   - ✅ Removida qualquer lógica de sort local.

### ✅ Padronização de Stores (CONCLUÍDO)

- ✅ Stores principais (`mapas`, `processos`, `subprocessos`) padronizadas com `lastError`.
- ✅ Remoção de `feedbackStore.show` para erros (mantido apenas para sucessos explícitos).

---

## Detalhamento Técnico por Componente (Histórico da Execução)

### 1. `ImportarAtividadesModal.vue` (Prioridade Alta)

**Status:** ✅ Concluído

- **Solução Aplicada:** O componente agora utiliza `processosStore.processosFinalizados` populado pela ação `buscarProcessosFinalizados`, que chama o endpoint específico `/api/processos/finalizados`. A filtragem de paginação client-side foi removida.

### 2. `ImpactoMapaModal.vue` (Prioridade Média)

**Status:** ✅ Concluído

- **Solução Aplicada:** Prop `codSubprocesso` tornou-se obrigatória. A dependência frágil de `processosStore.processoDetalhe` foi removida, garantindo que o modal funcione independentemente do contexto da view pai.

### 3. `SubprocessoCards.vue` (Prioridade Média)

**Status:** ✅ Concluído

- **Solução Aplicada:** Componente transformado em "dumb component" puro. Recebe todos os IDs necessários via props (`codProcesso`, `codSubprocesso`, `siglaUnidade`) e usa `useRouter` apenas para disparar a navegação, sem ler `useRoute`.

### 4. `AcoesEmBlocoModal.vue` & `ModalAcaoBloco.vue` (Prioridade Média)

**Status:** ✅ Concluído

- **Solução Aplicada:** O arquivo não utilizado `AcoesEmBlocoModal.vue` foi deletado. `ModalAcaoBloco.vue` foi verificado e está livre de `window.alert`, usando componentes BootstrapVueNext nativos.

### 5. `ArvoreUnidades.vue` (Prioridade Alta)

**Status:** ✅ Concluído

- **Solução Aplicada:** Lógica hardcoded (`if sigla == 'SEDOC'`) substituída por filtro genérico baseado na prop `ocultarRaiz`. A árvore agora renderiza corretamente baseada na estrutura de dados recebida, sem regras de negócio fixas no template.

### 6. `HistoricoAnaliseModal.vue` (Prioridade Baixa)

**Status:** ✅ Concluído

- **Solução Aplicada:** Adicionada verificação `!isLoading` no watch e limpeza do array `analises` ao fechar o modal, prevenindo flicker e race conditions.

### 7. `TabelaProcessos.vue` (Prioridade Média)

**Status:** ✅ Concluído

- **Solução Aplicada:** Código auditado para garantir ausência de `Array.sort()`. Adicionados comentários explicativos sobre o contrato de ordenação server-side via eventos.

---

## Padrões Consolidados

### ✅ Tratamento de Erros

- ✅ `utils/apiError.ts` implementado e testado
- ✅ Interceptor Axios usa normalizador
- ✅ `useApi` expõe `normalizedError`
- ✅ Sem `window.alert()` ou `window.confirm()` no código

### ✅ Arquitetura de Componentes

- ✅ Componentes novos seguem padrão "dumb" (props/emits)
- ✅ Uso consistente de BootstrapVueNext (`BModal`, `BButton`, `BAlert`, etc.)
- ✅ Setup Stores (Pinia) com composables bem definidos

---

## Lições Aprendidas e Descobertas

Durante a execução deste plano, os seguintes pontos foram destacados:

1. **Benefício dos Componentes "Burros":** Remover a dependência de `useRoute` e `stores` globais dos componentes de UI (como `SubprocessoCards` e `ImpactoMapaModal`) simplificou drasticamente os testes unitários e permitiu o reuso dos componentes em diferentes contextos (ex: dentro de modais ou dashboards).

2. **Server-Side é Mandatório:** Tentar filtrar ou ordenar grandes listas no frontend (como em `ImportarAtividadesModal`) é insustentável. A migração para endpoints específicos (`/finalizados`) melhorou a performance e a consistência dos dados.

3. **UX de Erros:** A substituição de `window.alert` por `BAlert` inline ou Toasts globais melhorou significativamente a experiência do usuário, tornando a aplicação mais profissional e menos intrusiva.

4. **Padronização de Stores:** O uso de `lastError` normalizado permite que cada componente decida como mostrar o erro (inline vs global), oferecendo flexibilidade sem perder a padronização do formato do erro.

---

**Prioridades Futuras:**

1. Manter a cobertura de testes acima de 80%.
2. Garantir que novos componentes sigam estritamente o guia de arquitetura.
3. Monitorar a performance das novas views de diagnóstico.

**Status Final:** Plano executado com sucesso. Todos os itens concluídos.
