# Plano de Refatoração de Componentes Vue.js (Atualizado)

**Última atualização:** 2025-12-13  
**Versão:** 2.0  
**Status do Projeto:** Em produção ativa com 21 CDUs implementados

Este documento detalha as alterações necessárias nos componentes Vue.js localizados em `frontend/src/components/` e módulos relacionados. O objetivo é continuar removendo lógicas de "protótipo" (hardcoded, filtros locais excessivos, mocks), otimizar a integração com o backend real, e consolidar as melhorias arquiteturais já implementadas.

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

## Mudanças Implementadas Recentemente (Desde Última Versão)

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

**Resultado:** Sistema de erro robusto, previsível e user-friendly.

### ✅ Novos Componentes Criados

1. **`AtividadeItem.vue`:** Componente granular para renderizar uma atividade individual com conhecimentos associados, permitindo edição inline.
2. **`UnidadeTreeNode.vue`:** Componente recursivo para renderizar nós da árvore de unidades com tri-state checkboxes.

### ✅ Funcionalidades de Diagnóstico Adicionadas

Quatro novas views para o fluxo de diagnóstico de competências:
- `AutoavaliacaoDiagnostico.vue`
- `ConclusaoDiagnostico.vue`
- `MonitoramentoDiagnostico.vue`
- `OcupacoesCriticasDiagnostico.vue`

**Contexto:** Diagnóstico é o terceiro tipo de processo (além de Mapeamento e Revisão), introduzindo autoavaliação de domínio de competências, identificação de ocupações críticas e geração de relatórios gerenciais.

### ✅ Store `feedback.ts` para Notificações Toast

Store dedicada para exibir notificações globais (toasts) de forma centralizada, usada em conjunto com o sistema de erro normalizado.

---

## Visão Geral e Diretrizes de Refatoração

### Princípios Arquiteturais

1. **Server-Side Filtering/Paginação:** Componentes que filtram grandes listas no JavaScript (ex: buscar todos os processos para depois filtrar os "Finalizados") devem passar a usar endpoints específicos do backend com parâmetros de query.

2. **Eliminar IDs e Siglas Hardcoded:** Remover verificações explícitas de IDs (ex: `codigo === 1`) ou siglas (ex: `'SEDOC'`). Essas regras de negócio devem vir do backend (flags booleanas no DTO) ou serem parametrizáveis via props.

3. **Desacoplar Modais de Stores Globais:** Modais devem, preferencialmente:
   - Receber dados via `props` (ex: `codSubprocesso: number`)
   - Buscar dados específicos baseados em IDs passados
   - Emitir eventos para comunicação com o pai
   - **Evitar** depender do estado global de listas carregadas em outras telas (ex: `processoDetalhe` da store)

4. **Componentes "Dumb" (Apresentacionais):**
   - Não devem acessar `useRoute()` ou `useRouter()` internamente (exceto para navegação de botões específicos)
   - Não devem acessar stores diretamente (exceto quando são componentes de layout global como `MainNavbar`)
   - Devem receber todos os dados necessários via props
   - Devem comunicar ações via `emit`

5. **Tratamento de Erros:**
   - **Erros inline** (validação, conflito, not found em formulário): Renderizar via `BAlert` no componente
   - **Erros globais** (500, rede, não autorizado): Interceptor Axios já exibe toast global
   - Stores devem usar `lastError: NormalizedError | null`; componentes decidem UX
   - **Nunca** usar `window.alert()` ou `window.confirm()`

6. **Ordenação e Paginação:**
   - Sempre server-side quando possível
   - Componentes de tabela devem emitir eventos de ordenação/paginação
   - O pai (view) é responsável por chamar a store/service com os parâmetros corretos

---

## Detalhamento Técnico por Componente

### 1. `ImportarAtividadesModal.vue` (Prioridade Alta)

**Localização:** `frontend/src/components/ImportarAtividadesModal.vue`  
**Linhas de código:** ~250  
**Testes:** `frontend/src/components/__tests__/ImportarAtividadesModal.spec.ts`

#### Situação Atual

- **Problema principal:** Usa `processosStore.processosPainel` (resultado de `buscarProcessosPainel()` com paginação hardcoded de 1000 registros) e filtra localmente por tipo e situação "FINALIZADO"
- **Lógica problemática (linhas 170-176):**
  ```typescript
  const processosDisponiveis = computed<ProcessoResumo[]>(() => {
    return processosStore.processosPainel.filter(
      (p) =>
        (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) &&
        p.situacao === "FINALIZADO",
    );
  });
  ```
- **Watch problemático (linhas 182-190):** Dispara `buscarProcessosPainel("ADMIN", 0, 0, 1000)` toda vez que o modal abre
- **Impacto:** Se houver mais de 1000 processos ou se a paginação mudar, o modal não verá todos os processos finalizados disponíveis para importação

#### Solução

**Backend/Store:**
- ✅ Endpoint `GET /api/processos/finalizados` existe (implementado em `processoService.ts:18-23`)
- ✅ Ação `buscarProcessosFinalizados()` existe em `stores/processos.ts:64-72`
- ✅ Ref `processosFinalizados` existe na store

**Componente:**
1. Remover computed `processosDisponiveis` (linhas 170-176)
2. Remover watch que chama `buscarProcessosPainel()` (linhas 182-190)
3. No watch de `mostrar`, chamar `processosStore.buscarProcessosFinalizados()`
4. Usar `processosStore.processosFinalizados` para popular o select

**Código sugerido:**

```typescript
watch(
  () => props.mostrar,
  (mostrar) => {
    if (mostrar) {
      resetModal();
      processosStore.buscarProcessosFinalizados(); // Nova chamada
    }
  },
);

// No template, substituir:
// :options="processosDisponiveis"
// por:
// :options="processosStore.processosFinalizados"
```

#### Checklist de Implementação

- [ ] Remover computed `processosDisponiveis`
- [ ] Atualizar watch para chamar `buscarProcessosFinalizados()`
- [ ] Atualizar template para usar `processosStore.processosFinalizados`
- [ ] Atualizar testes para mockar `buscarProcessosFinalizados` em vez de `buscarProcessosPainel`
- [ ] Validar com múltiplos processos finalizados (E2E ou manual)

#### Estimativa

- Desenvolvimento: 30-60 minutos
- Testes: 30 minutos
- Total: 1-1.5 horas

---

### 2. `ImpactoMapaModal.vue` (Prioridade Média)

**Localização:** `frontend/src/components/ImpactoMapaModal.vue`  
**Linhas de código:** ~180  
**Testes:** `frontend/src/components/__tests__/ImpactoMapaModal.spec.ts`

#### Situação Atual

- **Props atuais:** `mostrar`, `idProcesso`, `siglaUnidade` (nenhuma obrigatória)
- **Problema:** Depende de `processosStore.processoDetalhe` para buscar `codSubprocesso` usando `siglaUnidade` (linhas 45-52)
- **Lógica frágil:**
  ```typescript
  const subprocesso = processoDetalhe.value?.resumoSubprocessos?.find(
    (s) => s.siglaUnidade === props.siglaUnidade
  );
  const codSubprocesso = subprocesso?.codigo;
  ```
- **Impacto:** Se o modal for usado fora do contexto de detalhe do processo ou se `processoDetalhe` não estiver carregado, a lógica quebra

#### Solução

**Componente:**
1. Adicionar prop **obrigatória** `codSubprocesso: number`
2. Remover props `idProcesso` e `siglaUnidade` (se não forem usadas para mais nada)
3. Remover dependência de `processosStore.processoDetalhe`
4. Usar `props.codSubprocesso` diretamente na chamada `mapasStore.buscarImpactoMapa(codSubprocesso)`

**Consumidores a atualizar:**
- `CadAtividades.vue`
- `VisAtividades.vue`
- `CadMapa.vue`

**Busca de consumidores:**
```bash
grep -r "ImpactoMapaModal" frontend/src/views/ --include="*.vue"
```

#### Checklist de Implementação

- [ ] Adicionar prop obrigatória `codSubprocesso: number`
- [ ] Remover props opcionais desnecessárias
- [ ] Remover lógica de busca de subprocesso via `processoDetalhe`
- [ ] Atualizar todos os consumidores para passar `codSubprocesso`
- [ ] Atualizar testes para validar prop obrigatória
- [ ] Validar funcionamento em todos os contextos (E2E)

#### Estimativa

- Desenvolvimento: 1-1.5 horas
- Testes: 30 minutos
- Total: 1.5-2 horas

---

### 3. `SubprocessoCards.vue` (Prioridade Média)

**Localização:** `frontend/src/components/SubprocessoCards.vue`  
**Linhas de código:** ~120  
**Testes:** `frontend/src/components/__tests__/SubprocessoCards.spec.ts`

#### Situação Atual

- **Props atuais:** `codProcesso`, `codSubprocesso`, `siglaUnidade` (opcionais)
- **Problema:** Lê `route.params` internamente usando `useRoute()` para montar links de navegação
- **Fallback:** Se props não forem passadas, usa valores da rota
- **Impacto:** Impede o reuso dos cards em contextos onde a rota não tem esses parâmetros (ex: preview, modal, outros contextos)

#### Solução

**Componente:**
1. Tornar props `codProcesso`, `codSubprocesso` e `siglaUnidade` **obrigatórias** (remover `?`)
2. Remover `useRoute()` e leitura de `route.params`
3. Usar props diretamente para montar os links de navegação

**Consumidor:**
- `SubprocessoView.vue` (principal consumidor)
- Garantir que está passando todas as props necessárias

#### Checklist de Implementação

- [ ] Tornar props obrigatórias (remover `| undefined`)
- [ ] Remover `const route = useRoute()`
- [ ] Remover fallbacks para `route.params`
- [ ] Atualizar consumidores para passar todas as props
- [ ] Atualizar testes para passar props mock (sem route mock)
- [ ] Validar links de navegação em contexto sem route.params

#### Estimativa

- Desenvolvimento: 45-60 minutos
- Testes: 30 minutos
- Total: 1-1.5 horas

---

### 4. `AcoesEmBlocoModal.vue` & `ModalAcaoBloco.vue` (Prioridade Média)

**Localização:**  
- `frontend/src/components/AcoesEmBlocoModal.vue` (não utilizado)
- `frontend/src/components/ModalAcaoBloco.vue` (utilizado)

**Testes:** `frontend/src/components/__tests__/ModalAcaoBloco.spec.ts`

#### Situação Atual

- **`AcoesEmBlocoModal.vue`:** Componente não utilizado em nenhum lugar do código (verificar com `grep -r "AcoesEmBlocoModal" frontend/src/`)
- **`ModalAcaoBloco.vue`:** Componente utilizado; pode conter lógica duplicada ou padrões antigos

#### Solução

**Exclusão:**
1. Apagar `frontend/src/components/AcoesEmBlocoModal.vue` (não utilizado)
2. Remover testes associados se existirem

**Refatoração de `ModalAcaoBloco.vue` (se necessário):**
1. Verificar se há uso de `window.alert()` ou `window.confirm()` — substituir por `BAlert` ou `emit`
2. Garantir que validações mostram `BAlert` inline
3. Garantir que erros inesperados são tratados via `lastError` da store

#### Checklist de Implementação

- [ ] Confirmar que `AcoesEmBlocoModal.vue` não é usado (busca no código)
- [ ] Excluir `AcoesEmBlocoModal.vue`
- [ ] Remover testes associados (se existirem)
- [ ] Auditar `ModalAcaoBloco.vue` para uso de `alert()`/`confirm()`
- [ ] Substituir por `BAlert` ou `emit` conforme necessário
- [ ] Atualizar testes

#### Estimativa

- Desenvolvimento: 30-45 minutos
- Testes: 15-30 minutos
- Total: 45 minutos - 1 hora

---

### 5. `ArvoreUnidades.vue` (Prioridade Alta)

**Localização:** `frontend/src/components/ArvoreUnidades.vue`  
**Linhas de código:** ~220  
**Testes:** `frontend/src/components/__tests__/ArvoreUnidades.spec.ts`, `ArvoreUnidades.integration.spec.ts`, `ArvoreUnidades.bug.spec.ts`

#### Situação Atual

- **Problema:** Contém hardcoding explícito: `if (u.sigla === 'SEDOC' || u.codigo === 1)` (linhas 66-68)
- **Lógica atual:**
  ```typescript
  // Se for SEDOC (pela sigla ou código 1), não mostra ela, mas mostra as filhas
  if (u.sigla === 'SEDOC' || u.codigo === 1) {
    if (u.filhas) lista.push(...u.filhas);
  } else {
    lista.push(u);
  }
  ```
- **Contexto:** A SEDOC é realmente especial (raiz da hierarquia) e deve ser ocultada, mas o critério deve ser genérico
- **Lógica de tipo:** `tipo` (INTERMEDIARIA, INTEROPERACIONAL) é usado para habilitação de seleção (lógica válida, manter)

#### Solução

**Opção 1: Backend fornece flag `ocultar: boolean`**
- Backend retorna `{ ocultar: true }` para unidades que devem ser ocultadas
- Componente filtra por `!u.ocultar`

**Opção 2: Prop `ocultarRaiz: boolean` (mais simples)**
- Adicionar prop `ocultarRaiz: boolean` (padrão `true`)
- Ocultar unidades com `nivel === 0` (ou critério similar)
- Remover hardcoding de sigla/código

**Opção 3: Critério de nível**
- Remover hardcoding de `SEDOC` e `codigo === 1`
- Usar `u.nivel === 0` ou `u.tipo === 'ROOT'` (se backend fornecer)

**Recomendação:** Opção 2 (prop) no curto prazo; Opção 1 (backend) no médio prazo.

#### Checklist de Implementação

- [ ] Remover `if (u.sigla === 'SEDOC' || u.codigo === 1)`
- [ ] Implementar lógica genérica baseada em `nivel` ou prop `ocultarRaiz`
- [ ] Manter lógica de `tipo` (INTERMEDIARIA, INTEROPERACIONAL) para habilitação
- [ ] Atualizar testes para validar comportamento sem hardcoding
- [ ] Testar com diferentes estruturas de unidades (E2E)

#### Estimativa

- Desenvolvimento: 1-1.5 horas
- Testes: 45 minutos
- Total: 2-2.5 horas

---

### 6. `HistoricoAnaliseModal.vue` (Prioridade Baixa)

**Localização:** `frontend/src/components/HistoricoAnaliseModal.vue`  
**Linhas de código:** ~90  
**Testes:** `frontend/src/components/__tests__/HistoricoAnaliseModal.spec.ts`

#### Situação Atual

- **Problema:** Watch na prop `mostrar` dispara busca imediatamente sem verificação de `loading`
- **Risco:** Race conditions se o modal for aberto/fechado rapidamente
- **Problema secundário:** Sem limpeza de dados ao fechar (flicker ao reabrir)

#### Solução

**Componente:**
1. Adicionar verificação de `loading` antes de disparar nova busca
2. Limpar dados (`analises.value = []`) ao fechar o modal (no handler de `@hide`)

**Código sugerido:**

```typescript
watch(
  () => props.mostrar,
  async (mostrar) => {
    if (mostrar && !isLoading.value) {
      await analisesStore.buscarAnalises(props.codSubprocesso);
    }
  },
);

const fechar = () => {
  analisesStore.clearError();
  emit('fechar');
};
```

#### Checklist de Implementação

- [ ] Adicionar verificação de `loading` no watch
- [ ] Implementar limpeza de dados ao fechar
- [ ] Atualizar testes para validar prevenção de race conditions
- [ ] Testar abrir/fechar modal rapidamente (manual ou E2E)

#### Estimativa

- Desenvolvimento: 30 minutos
- Testes: 15-30 minutos
- Total: 45 minutos - 1 hora

---

### 7. `TabelaProcessos.vue` (Prioridade Média)

**Localização:** `frontend/src/components/TabelaProcessos.vue`  
**Linhas de código:** ~150  
**Testes:** `frontend/src/components/__tests__/TabelaProcessos.spec.ts`

#### Situação Atual

- **Props:** `processos`, `criterioOrdenacao`, `direcaoOrdenacaoAsc`
- **Evento:** Emite `ordenar` quando usuário clica em coluna
- **Ambiguidade:** Não fica claro se ordenação é client-side ou server-side
- **Risco:** Se componente ordenar localmente (`Array.sort()`), dados paginados do backend ficarão inconsistentes

#### Solução

**Documentação e Validação:**
1. Documentar em comentário que ordenação é **server-side**
2. Verificar que componente **não** ordena localmente (remover qualquer `Array.sort()` se existir)
3. Garantir que evento `ordenar` é emitido corretamente
4. O pai (view) deve chamar a store com parâmetros de ordenação, que chama o backend

**Contrato esperado:**
```typescript
emit('ordenar', { campo: 'descricao', direcao: 'asc' | 'desc' })
```

#### Checklist de Implementação

- [ ] Auditar componente para verificar que **não** há `Array.sort()` local
- [ ] Adicionar comentário documentando que ordenação é server-side
- [ ] Validar que evento `ordenar` propaga corretamente
- [ ] Atualizar testes para validar que componente não reordena dados
- [ ] Testar ordenação via backend (stub E2E ou manual)

#### Estimativa

- Desenvolvimento: 30-45 minutos
- Testes: 30 minutos
- Total: 1-1.5 horas

---

## Novos Itens de Refatoração Identificados

### 8. Componentes Novos: `AtividadeItem.vue` e `UnidadeTreeNode.vue`

**Status:** Recém-criados, arquitetura já moderna.

**Análise:**
- `AtividadeItem.vue`: Componente granular para renderizar atividades individuais com edição inline. Usa padrão de props/emits correto.
- `UnidadeTreeNode.vue`: Componente recursivo para árvore de unidades. Usa padrão tri-state correto.

**Ação:** ✅ Sem refatoração necessária. Manter como referência de boas práticas.

---

### 9. Views de Diagnóstico (Novas)

**Arquivos:**
- `AutoavaliacaoDiagnostico.vue`
- `ConclusaoDiagnostico.vue`
- `MonitoramentoDiagnostico.vue`
- `OcupacoesCriticasDiagnostico.vue`

**Análise:** Módulo recém-implementado seguindo arquitetura moderna (Views → Stores → Services → API).

**Ação recomendada:**
- [ ] Auditar para garantir que não há hardcoding de IDs/estados
- [ ] Validar que tratamento de erros usa `lastError` e `BAlert` inline
- [ ] Garantir que filtros/paginação são server-side
- [ ] Criar testes E2E para fluxo completo de diagnóstico

**Estimativa:** 2-3 horas de auditoria + ajustes menores

---

### 10. Padronização de Stores: Uso Completo de `lastError`

**Status:** Parcialmente implementado (stores principais como `processos`, `mapas` já usam)

**Stores a revisar:**
- `alertas.ts`
- `analises.ts`
- `atividades.ts`
- `atribuicoes.ts`
- `subprocessos.ts`
- `unidades.ts`

**Ação:**
- [ ] Auditar cada store para verificar se usa `lastError: NormalizedError | null`
- [ ] Remover `feedbackStore.show()` de blocos `catch` (exceto para mensagens de sucesso)
- [ ] Garantir que stores sempre fazem `throw error` após captura (para propagação)
- [ ] Atualizar consumidores para renderizar `BAlert` inline quando apropriado

**Estimativa:** 3-4 horas para todas as stores

---

## Padrões Consolidados (Não Requerem Refatoração)

### ✅ Tratamento de Erros

- ✅ `utils/apiError.ts` implementado e testado
- ✅ Interceptor Axios usa normalizador
- ✅ `useApi` expõe `normalizedError`
- ✅ Sem `window.alert()` ou `window.confirm()` no código

### ✅ Arquitetura de Componentes

- ✅ Componentes novos seguem padrão "dumb" (props/emits)
- ✅ Uso consistente de BootstrapVueNext (`BModal`, `BButton`, `BAlert`, etc.)
- ✅ Setup Stores (Pinia) com composables bem definidos

### ✅ Testes

- ✅ 85+ specs de testes unitários (Vitest)
- ✅ 15+ specs de testes E2E (Playwright)
- ✅ Cobertura de testes mantida

---

## Estratégia de Rollout

### Fase 1: Alta Prioridade (Sprint 1-2)

**Componentes críticos com impacto funcional:**
1. `ImportarAtividadesModal.vue` (filtragem server-side)
2. `ArvoreUnidades.vue` (remover hardcoding)
3. `ImpactoMapaModal.vue` (desacoplar de `processoDetalhe`)

**Estimativa total:** 5-7 horas

**Critérios de aceite:**
- Todos os testes unitários passam
- E2E relevantes atualizados e passando
- Lint e typecheck sem erros
- Code review aprovado

### Fase 2: Média Prioridade (Sprint 3-4)

**Componentes com melhorias arquiteturais:**
4. `SubprocessoCards.vue` (remover `useRoute()`)
5. `ModalAcaoBloco.vue` / `AcoesEmBlocoModal.vue` (consolidar e limpar)
6. `TabelaProcessos.vue` (documentar ordenação server-side)

**Estimativa total:** 3-4 horas

### Fase 3: Baixa Prioridade (Sprint 5)

**Componentes com melhorias de qualidade:**
7. `HistoricoAnaliseModal.vue` (race conditions)
8. Padronização completa de `lastError` em stores restantes
9. Auditoria de views de diagnóstico

**Estimativa total:** 5-6 horas

### Fase 4: Documentação e Consolidação (Sprint 6)

- Atualizar README de cada diretório (`components/`, `views/`, `stores/`)
- Atualizar `AGENTS.md` com novos padrões
- Criar guia de migração para novos componentes
- Code review final
- Smoke tests E2E completos

**Estimativa total:** 2-3 horas

---

## Checklist Geral de Qualidade

### Para Cada PR de Refatoração

- [ ] Testes unitários do componente refatorado passam
- [ ] Testes E2E relacionados passam (se existirem)
- [ ] `npm run lint` sem erros
- [ ] `npm run typecheck` sem erros
- [ ] Nenhum `window.alert()` ou `window.confirm()` no código
- [ ] Tratamento de erro usa `NormalizedError` e `lastError` (stores)
- [ ] Componentes não dependem de IDs/siglas hardcoded
- [ ] Modais recebem dados via props (não leem estado global diretamente)
- [ ] Componentes "dumb" não usam `useRoute()` (exceto para navegação)
- [ ] Documentação inline (comentários) atualizada
- [ ] Payload de exemplo da API documentado no PR (se houver novo endpoint)

### Validação Final do Projeto

- [ ] Zero `window.alert()` ou `window.confirm()` no código
- [ ] Zero hardcoding de IDs ou siglas em lógica de negócio
- [ ] Todas as stores usam `lastError: NormalizedError | null`
- [ ] Todos os modais recebem dados via props ou buscam por ID
- [ ] Todos os componentes de tabela/lista usam ordenação/paginação server-side
- [ ] 100% dos testes unitários passando
- [ ] 100% dos testes E2E críticos passando
- [ ] Documentação README atualizada em cada diretório

---

## Ferramentas e Comandos Úteis

### Frontend (executar dentro de `frontend/`)

```bash
# Testes
npm run test:unit              # Testes unitários Vitest
npm run test:unit -- [arquivo] # Teste específico
npm run coverage:unit          # Cobertura de testes

# Qualidade de código
npm run lint                   # ESLint com auto-fix
npm run typecheck              # Verificação de tipos TypeScript
npm run quality:all            # Todas as verificações

# Desenvolvimento
npm run dev                    # Dev server (Vite)
npm run build                  # Build de produção
```

### E2E (executar na raiz)

```bash
npm run test:e2e               # Todos os testes E2E
npm run test:e2e -- [spec]     # Teste E2E específico
./scripts/capturar-telas.sh    # Captura de telas automatizada
./scripts/visualizar-telas.sh  # Visualizador de capturas
```

### Backend (executar na raiz)

```bash
./gradlew :backend:test        # Testes unitários backend
./gradlew qualityCheck         # Qualidade backend
./gradlew qualityCheckAll      # Qualidade backend + frontend
```

### Busca no Código

```bash
# Encontrar uso de componente
grep -r "ImportarAtividadesModal" frontend/src/ --include="*.vue"

# Encontrar window.alert ou confirm
grep -r "window.alert\|window.confirm" frontend/src/ --include="*.vue" --include="*.ts"

# Encontrar hardcoding de IDs
grep -r "codigo === 1\|codigo === '1'" frontend/src/ --include="*.vue" --include="*.ts"

# Encontrar uso de feedbackStore em stores
grep -r "feedbackStore.show" frontend/src/stores/ --include="*.ts"
```

---

## Referências e Documentação

### Documentação do Projeto

- **README.md:** Visão geral do projeto e stack tecnológico
- **AGENTS.md:** Guia para agentes de desenvolvimento (convenções, boas práticas)
- **plano-refatoracao-erros.md:** Plano detalhado de tratamento de erros (CONCLUÍDO)
- **frontend/README.md:** Arquitetura detalhada do frontend com diagramas
- **backend/README.md:** Arquitetura detalhada do backend com diagramas
- **frontend/src/[diretório]/README.md:** Documentação específica de cada módulo

### Casos de Uso

- **reqs/:** 21 casos de uso documentados (CDU-01 a CDU-21)
- **e2e/:** Testes E2E cobrindo os CDUs principais

### Recursos Externos

- [Vue 3 Composition API](https://vuejs.org/guide/introduction.html)
- [Pinia Stores](https://pinia.vuejs.org/)
- [BootstrapVueNext](https://bootstrap-vue-next.github.io/bootstrap-vue-next/)
- [Vitest](https://vitest.dev/)
- [Playwright](https://playwright.dev/)

---

## Conclusão

Este plano atualizado reflete o estado atual do projeto SGC, incorporando as melhorias já implementadas (especialmente tratamento de erros) e identificando os itens restantes de refatoração. O projeto evoluiu significativamente desde o protótipo inicial, e as refatorações propostas visam consolidar a arquitetura moderna, eliminar resquícios de código prototipado e garantir escalabilidade e manutenibilidade.

**Prioridades:**
1. Filtragem server-side (`ImportarAtividadesModal`)
2. Remover hardcoding (`ArvoreUnidades`)
3. Desacoplar modais de estado global (`ImpactoMapaModal`)
4. Componentes "dumb" sem `useRoute()` (`SubprocessoCards`)
5. Consolidar padrão `lastError` em todas as stores

**Estimativa total:** 15-20 horas de desenvolvimento + testes para completar todas as fases.

**Próximos passos:** Iniciar Fase 1 (Alta Prioridade) com `ImportarAtividadesModal.vue`.
