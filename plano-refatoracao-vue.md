# Plano de Refatoração de Componentes Vue.js

**⚠️ DOCUMENTO SUPERSEDIDO — Veja `plano-refatoracao-vue-atualizado.md` para a versão mais recente (v2.0)**

Última atualização: 2025-12-13 (versão original mantida para referência histórica)

Este documento detalha as alterações necessárias nos componentes Vue.js localizados em `@frontend/src/components/`. O
objetivo é remover lógicas de "protótipo" (hardcoded, filtros locais excessivos, mocks) e otimizar a integração com o
backend real.

**Nova versão disponível:** [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) — Documento expandido com contexto completo do estado atual do projeto, análise das mudanças recentes, e plano detalhado atualizado.

## Contexto Técnico do Projeto

**Stack Frontend:**

- Vue 3.5 (Composition API com TypeScript)
- Pinia para gerenciamento de estado (com Setup Stores)
- BootstrapVueNext para componentes de UI
- Vitest para testes unitários (com cobertura)
- Vite como builder
- Axios para requisições HTTP (com interceptors de autenticação)

**Arquitetura de Camadas:**

1. **Views** → **Stores (Pinia)** → **Services** → **Axios API**
2. Componentes geralmente devem ser "leves" (props in, emits out), embora suas capacidades devem ser exploradas para
   deixar a aplicação moderna e responsíveis
3. Modais devem receber dados via props ou buscar por IDs específicos, não por estado global

**Estrutura de Testes:**

- Testes de componentes em `src/components/__tests__/*.spec.ts`
- Cobertura minimamente mantida com `npm run coverage:unit`
- E2E com Playwright (definidos em `e2e/`)
- No frontend: Lint com `npm run lint` e typecheck com `npm run typecheck`

---

## 1. Visão Geral e Diretrizes

1. **Remover Filtragem e Paginação no Cliente:** Componentes que filtram grandes listas no JavaScript (ex: buscar todos
   os processos para depois filtrar os "Finalizados") devem passar a usar endpoints específicos do backend com
   parâmetros de query (server-side filtering).
2. **Eliminar IDs e Siglas Hardcoded:** Remover verificações explícitas de IDs (ex: `codigo === 1`) ou siglas (ex:
   `'SEDOC'`). Essas regras de negócio devem vir do backend (flags booleanas no DTO) ou configurações.
3. **Desacoplar Modais de Stores Globais:** Modais devem, preferencialmente, receber dados via `props` e emitir eventos,
   ou buscar dados específicos baseados em um ID passado, em vez de dependerem do estado global de listas carregadas em
   outras telas.
4. **Padronizar Tratamento de Erros (CONCLUÍDO):** Remover `window.alert()` e `window.confirm()` nativos do browser e substituir
   por:
    - **BAlert** (componente BootstrapVueNext) para mensagens inline na UI, com botão de fechar.
    - **useFeedbackStore** (store Pinia em `src/stores/feedback.ts`) para notificações toast/banner que aparecem no topo
      da aplicação e desaparecem automaticamente.
    - **useApi** (composable em `src/composables/useApi.ts`) que já captura erros da API e os armazena em
      `error.value` (string); o componente consumidor deve renderizar esse erro via `BAlert`.
    - Validações devem emitir eventos (via `emit`) para o componente pai tratar a exibição de mensagem, em vez de
      bloquear com `alert()`.
    - Detalhes adicionais em `plano-refatoracao-erros.md` e `frontend/src/utils/README.md`.

---

## 2. Detalhamento Técnico por Componente

### 2.1. `ImportarAtividadesModal.vue` (Prioridade Alta)

**Situação Atual:**

- Usa `processosStore.processosPainel` (resultado de `buscarProcessosPainel()` com paginação de 1000 registros
  hardcoded)
- Filtra localmente:
  `processosStore.processosPainel.filter((p) => (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) && p.situacao === "FINALIZADO")`
- Se dados de painel estiverem paginados, modal não verá processos das outras páginas
- Watch dispara `buscarProcessosPainel("ADMIN", 0, 0, 1000)` toda vez que modal abre
- Arquivo: `frontend/src/components/ImportarAtividadesModal.vue` (linhas 170-176: computed `processosDisponiveis`,
  linhas 182-190: watch)
- Testes: `frontend/src/components/__tests__/ImportarAtividadesModal.spec.ts` (existentes, devem ser atualizados)

**Problema:** Realiza filtragem de processos no cliente dependendo da lista do painel (
`processosStore.processosPainel`). Se o painel estiver paginado, a importação não verá processos de outras páginas.
**Meta:** Usar endpoint de processos finalizados.

- [ ] **Backend/Store:**
    - Ação `buscarProcessosFinalizados` já existe em `processos.ts` (linhas 52-54) e chama
      `processoService.buscarProcessosFinalizados()`.
    - Verificar que o endpoint `GET /api/processos/finalizados` existe no backend e retorna a estrutura esperada.
- [ ] **Componente:**
    - Remover computed `processosDisponiveis` (linhas 170-176).
    - Remover watch que chama `buscarProcessosPainel()` (linhas 182-190).
    - No novo `watch(mostrar)`, chamar `processosStore.buscarProcessosFinalizados()`.
    - Usar `processosStore.processosFinalizados` para popular o select (ref já existe na store).

  **Contratos/DTOs esperados:**
    - GET
      /api/processos/finalizados → [{ id?: number, codigo: string, descricao: string, tipo: 'MAPEAMENTO'|'REVISAO'|'DIAGNOSTICO', situacao: 'FINALIZADO', unidadesParticipantes?: number }]
    - Ação Pinia: `buscarProcessosFinalizados()` (já existe, persistir em `processosStore.processosFinalizados`)

  **Checklist de testes:**
    - Unit: mockar `processosStore.buscarProcessosFinalizados` com Vitest e garantir que select renderiza
      `processosFinalizados` (arquivo: `ImportarAtividadesModal.spec.ts`).
    - Unit: verificar que `processosDisponiveis` computed foi removido.
    - Unit: validar que `watch(mostrar)` chama `buscarProcessosFinalizados()` e limpa modal ao fechar.
    - Integration/E2E: testar com múltiplos processos finalizados; validar select population e seleção de processo.

  **Estimativa e subtarefas:**
    - Estimativa (agente IA): execução automatizada em ~1-3 horas de agente (ciclos de codificação, testes e PR).
    - Subtarefas (agente): criar branch `refactor/importar-atividades-endpoint`, aplicar mudanças no componente,
      executar `npm run test:unit -- ImportarAtividadesModal.spec.ts`, rodar `npm run lint` e `npm run typecheck`, abrir
      PR com payload de exemplo e instruções para stub do E2E.

### 2.2. `ImpactoMapaModal.vue` (Prioridade Média)

**Situação Atual:**

- Recebe props `mostrar`, `idProcesso`, `siglaUnidade` (nenhuma obrigatória)
- Depende de `processosStore.processoDetalhe` para buscar `codSubprocesso` (linhas 45-52: lógica para encontrar
  subprocesso)
- Se `processoDetalhe` não estiver carregado ou `siglaUnidade` não corresponder, lógica quebra
- Arquivo: `frontend/src/components/ImpactoMapaModal.vue` (linhas 45-52: find logic)
- Testes: `frontend/src/components/__tests__/ImpactoMapaModal.spec.ts` (existentes)

**Problema:** Depende de `processosStore.processoDetalhe` para descobrir o `codSubprocesso` usando `siglaUnidade`. Isso
cria um acoplamento frágil; se o modal for usado fora do contexto de detalhe do processo, ele quebra.
**Meta:** Receber `codSubprocesso` via prop.

- [ ] **Componente:**
    - Adicionar prop obrigatória `codSubprocesso: number`.
    - Remover props `idProcesso` e `siglaUnidade` (se não forem usadas para mais nada).
    - Remover dependência de `processosStore.processoDetalhe`.
    - Usar `props.codSubprocesso` diretamente na chamada `mapasStore.buscarImpactoMapa`.
- [ ] **Consumidores:**
    - Buscar uso com `grep -r "ImpactoMapaModal" frontend/src/` → achará `CadAtividades.vue`, `VisAtividades.vue`,
      `CadMapa.vue`
    - Atualizar `CadAtividades.vue`: Passar `codSubprocesso`.
    - Atualizar `VisAtividades.vue`: Passar `codSubprocesso`.
    - Atualizar `CadMapa.vue`: Passar `codSubprocesso`.

  **Contratos/DTOs esperados:**
    - Nenhuma alteração de endpoint necessária; modal passará a receber apenas um id (codSubprocesso: number).

  **Checklist de testes:**
    - Unit: validar que o componente lança erro/warning se prop obrigatória faltar e que chama
      `mapasStore.buscarImpactoMapa` com `codSubprocesso` (via Vitest mock).
    - Unit: validar que consumidores passam `codSubprocesso` corretamente.
    - Integration/E2E: abrir modal a partir dos consumidores e garantir que dados aparecem com o `codSubprocesso`
      passado.

  **Subtarefas:**
    - Subtarefas (agente): adicionar prop via patch, executar buscas por usos com grep e atualizar consumidores com
      commits, gerar testes unitários automatizados, rodar linter/typecheck, abrir PR.

### 2.3. `SubprocessoCards.vue` (Prioridade Média)

**Situação Atual:**

- Lê `route.params` internamente para montar links de navegação (usando `useRoute()`)
- Props `codProcesso`, `codSubprocesso`, `siglaUnidade` são opcionais
- Fallback para route.params se props não forem passadas
- Arquivo: `frontend/src/components/SubprocessoCards.vue` (uso de `useRoute`, fallback para params)
- Testes: `frontend/src/components/__tests__/SubprocessoCards.spec.ts` (existentes)

**Problema:** Lê `route.params` internamente para montar links de navegação. Isso impede o reuso dos cards em contextos
onde a rota não tem esses parâmetros.
**Meta:** Componente "dumb" que recebe IDs via props.

- [ ] **Componente:**
    - Tornar props `codProcesso`, `codSubprocesso` e `siglaUnidade` obrigatórias (remover opcionais/null checks que
      fallback para rota).
    - Remover `useRoute` e leitura de params.
- [ ] **Consumidor:**
    - Localizar consumidor: `grep -r "SubprocessoCards" frontend/src/views/` → achará `SubprocessoView.vue`
    - Atualizar `SubprocessoView.vue`: Garantir que está passando todas as props necessárias para o componente.

  **Contratos/DTOs esperados:**
    - Nenhum endpoint novo; componente passa a receber pela props: { codProcesso: number, codSubprocesso: number,
      siglaUnidade?: string }

  **Checklist de testes:**
    - Unit: renderizar SubprocessoCards com props mock e validar links gerados (sem precisar de route).
    - Unit: garantir que erro/warning é disparado se props obrigatórias faltarem.
    - Integration/E2E: usar SubprocessoCards em contexto sem route.params e validar funcionamento.

  **Subtarefas:**
    - Subtarefas (agente): aplicar modificação de props, remover uso de rota via refactor script, atualizar
      consumidores, rodar testes unitários automaticamente.

### 2.4. `AcoesEmBlocoModal.vue` & `ModalAcaoBloco.vue` (Prioridade Média)

**Situação Atual:**

- `AcoesEmBlocoModal.vue`: Componente que não é utilizado em nenhum lugar (verificar com
  `grep -r "AcoesEmBlocoModal" frontend/src/`)
- `ModalAcaoBloco.vue`: Componente utilizado; contém `alert()` nativo no código
- Arquivo: `frontend/src/components/AcoesEmBlocoModal.vue` (não utilizado)
- Arquivo: `frontend/src/components/ModalAcaoBloco.vue` (usado, contém alert)
- Testes: `frontend/src/components/__tests__/ModalAcaoBloco.spec.ts` (existentes)

**Problema:** Existem dois componentes fazendo coisas similares. `AcoesEmBlocoModal.vue` não é utilizado.
`ModalAcaoBloco.vue` é usado mas contém `alert` nativo.
**Meta:** Manter apenas um e padronizar.

- [ ] **Exclusão:**
    - Apagar `frontend/src/components/AcoesEmBlocoModal.vue` (não utilizado).
    - Remover testes associados se existirem.
- [ ] **Refatoração (`ModalAcaoBloco.vue`):**
    - Localizar todas as chamadas a `alert()` e `confirm()` no arquivo.
    - Substituir `alert()` nativo por `BAlert` interno para validação ou usar composable de toast (se existir em
      `src/composables/`).
    - Usar `emit` para comunicar erros ao componente pai, que tratará exibição de mensagens.

  **Contratos/DTOs esperados:**
    - Nenhum.

  **Checklist de testes:**
    - Unit: garantir que nenhuma chamada a `window.alert` ou `window.confirm` permanece no componente.
    - Unit: validar que validações mostram `BAlert` ou emitem eventos apropriados.
    - Integration/E2E: fluxo de validação em bloco mostrando toast/alert interno.

  **Subtarefas:**
    - Subtarefas (agente): detectar e remover arquivo `AcoesEmBlocoModal.vue` via script, refatorar chamadas a `alert()`
      por `BAlert`/emit, atualizar testes automaticamente.

### 2.5. `ArvoreUnidades.vue` (Prioridade Alta)

**Situação Atual:**

- Contém hardcoding explícito: `if (u.sigla === 'SEDOC' || u.codigo === 1)` (linhas 66-68)
- Oculta raiz por ID: `u.codigo === 1` e por sigla: `u.sigla === 'SEDOC'` -- a SEDOC é realmente especial e deve ser
  ocultada; vale a pena só rever o uso do código.
- Lógica de tipo: `tipo` (INTERMEDIARIA, INTEROPERACIONAL) é usada para habilitação
- Arquivo: `frontend/src/components/ArvoreUnidades.vue` (linhas 66-68: hardcoding, métodos `isHabilitado`,
  `getEstadoSelecao`)
- Testes: `frontend/src/components/__tests__/ArvoreUnidades.spec.ts`, `ArvoreUnidades.integration.spec.ts`,
  `ArvoreUnidades.bug.spec.ts` (existentes)

**Problema:** Lógica hardcoded (`codigo === 1`, `sigla === 'SEDOC'`) e regras de negócio complexas no frontend. Mas: a
SEDOC é realmente especial e deve ser ocultada; vale a pena só rever o uso do código.
**Meta:** Lógica genérica baseada em tipos.

- [ ] **Remover Hardcoding:**
    - Remover `if (u.sigla === 'SEDOC' || u.codigo === 1)` (linhas 66-68).
    - Se for necessário ocultar a raiz, criar uma prop `ocultarRaiz: boolean` e aplicar a lógica de forma genérica (ex:
      ocultar o nó de nível 0 ou com `nivel === 0`).
    - Substituir por critério de nível ou por flag do backend.
- [ ] **Revisão de Lógica:**
    - Manter a lógica baseada em `tipo` (INTERMEDIARIA, INTEROPERACIONAL) pois faz parte da regra de negócio de seleção
      visual, mas garantir que não dependa de IDs específicos.
    - Simplificar `isHabilitado` e `getEstadoSelecao` se possível, ou documentar claramente a regra de tri-state.

  **Contratos/DTOs esperados:**
    - Sugestão: backend retornar, onde aplicável, flags como { ocultar: boolean, tipo: 'INTERMEDIARIA'|'
      INTEROPERACIONAL'|'ROOT', nivel: number } para evitar hardcoding.

  **Checklist de testes:**
    - Unit: validar comportamento da árvore com diferentes tipos e flags (ocultar raiz, estados
      habilitados/desabilitados e tri-state) sem usar hardcoded IDs (Vitest com fixtures).
    - Unit: testar seleção e deselecção de nós em cenários tri-state.
    - Integration/E2E: navegar árvore com dados reais/fixtures e validar seleção e UI.

  **Estimativa e subtarefas:**
    - Estimativa (agente IA): ~1-4 horas de execução automatizada (varia conforme complexidade de regras).
    - Subtarefas (agente): remover hardcoding por transformações regex, adicionar prop `ocultarRaiz`, adaptar lógica
      para usar `tipo`/flags, gerar/atualizar testes unitários e de integração, atualizar documentação automaticamente.

### 2.6. `HistoricoAnaliseModal.vue` (Prioridade Baixa)

**Situação Atual:**

- Watch na prop `mostrar` dispara busca imediatamente sem verificação de `loading`
- Sem limpeza de dados ao fechar
- Arquivo: `frontend/src/components/HistoricoAnaliseModal.vue` (watch, busca de análises)
- Testes: `frontend/src/components/__tests__/HistoricoAnaliseModal.spec.ts` (existentes)

**Problema:** O `watch` na prop `mostrar` dispara a busca imediatamente.
**Meta:** Prevenir race conditions e "flicker".

- [ ] **Componente:**
    - Adicionar verificação de `loading` antes de disparar nova busca (prevent duplicate calls).
    - Limpar dados (`analises.value = []`) ao fechar o modal (no handler de `@hide`).

  **Contratos/DTOs esperados:**
    - GET /api/analises?processoId={id} → [{ id, autor, data, comentario }]

  **Checklist de testes:**
    - Unit: garantir que `loading` evita chamadas duplicadas (Vitest com mock de `loading` state).
    - Unit: validar que `analises` é esvaziado ao fechar modal.
    - Integration/E2E: abrir/fechar modal rapidamente para detectar race conditions.

  **Subtarefas:**
    - Subtarefas (agente): incluir proteção de `loading`, limpar dados ao fechar via patch, adicionar testes
      unitários/e2e gerados automaticamente.

### 2.7. `TabelaProcessos.vue` (Prioridade Média)

**Situação Atual:**

- Emite evento `ordenar`, mas não fica claro se ordenação é client-side ou server-side
- Componente recebe lista `processos` via prop
- Props de ordenação: `criterioOrdenacao`, `direcaoOrdenacaoAsc`
- Arquivo: `frontend/src/components/TabelaProcessos.vue` (linhas 69-71: emit ordenação, props de sort)
- Testes: `frontend/src/components/__tests__/TabelaProcessos.spec.ts` (existentes)

**Problema:** Componente emite ordenação, mas a responsabilidade de ordenar (client vs server) não está clara.
**Meta:** Garantir suporte a ordenação server-side.

- [ ] **Componente:**
    - Documentar em comentário se ordenação é server-side ou client-side.
    - Verificar se a prop `processos` está sendo paginada corretamente pelo pai (pai deve passar dados já paginados e
      ordenados do backend).
    - Garantir que o componente não ordene localmente se a intenção for ordenação no backend (remover qualquer
      `Array.sort()` local).

  **Contratos/DTOs esperados:**
    - API de listagem deverá aceitar parâmetros de ordenação e paginação: GET
      /api/processos?pagina=1&size=20&sort=campo,asc

  **Checklist de testes:**
    - Unit: validar que `emit('ordenar')` propaga eventos corretamente e que o componente não reordena os dados
      recebidos localmente.
    - Unit: garantir que nenhum `Array.sort()` é feito no componente.
    - Integration/E2E: testar ordenação via backend (stub) e mudança de coluna ordenada.

  **Estimativa e subtarefas:**
    - Estimativa (agente IA): ~30-90 minutos de execução automatizada.
    - Subtarefas (agente): documentar contrato de ordenação, aplicar alterações no componente para garantir server-side
      sort, gerar testes automatizados; executar E2E com stub do backend.

---

## 3. Padronização Geral

- [x] **Alerts (Concluído):** Remover `window.alert()` e `window.confirm()` nativos do browser.

  **Mecanismos de Tratamento de Erro/Validação Disponíveis:**

    1. **BAlert (BootstrapVueNext)** — Mensagem inline dentro de um componente/modal.
    2. **useFeedbackStore** (Pinia) — Notificação toast.
    3. **useApi (composable)** — Captura automaticamente erros de requisições HTTP e armazena em `error.value` (string
       com mensagem).
    4. **Emits** — Para validações que impedem ação (ex: "selecione ao menos um item"), o componente deve emitir um
       evento e deixar o **componente pai** exibir a mensagem.

  **Rollout:**
    - Estratégia de rollout: dividir em PRs por domínio/componentes (ImportarAtividades & ArvoreUnidades como PRs
      prioritários), cada PR com checklist de testes e descrição do contrato de API exigido.
    - Critérios de aceite por PR: todos os testes unitários passam (`npm run test:unit`), E2E relevantes
      atualizados/verificados, exemplos de payloads/DTOs documentados no PR, lint e typecheck passam.

  **Notas para revisão de PRs:**
    - Incluir no PR payloads de exemplo dos endpoints usados e instruções para executar E2E locais com stubs (ou
      fixtures do Playwright).
    - Marcar reviewers para backend quando houver alterações/novo contrato esperado.
    - Verificar ausência de `window.alert`/`confirm` e uso de `BAlert`/toasts para UX consistente.
    - Validar que componentes alterados não dependem mais de IDs/siglas hardcoded.

  **Ferramentas e scripts disponíveis no agente:**
    - `npm run lint` — ESLint com auto-fix
    - `npm run typecheck` — Verificação de tipos TypeScript (vue-tsc)
    - `npm run test:unit` — Testes Vitest (com cobertura opcional)
    - `npm run coverage:unit` — Cobertura de testes com relatório
    - `npm run quality:all` — Todas as verificações em sequência
    - `grep -r <pattern> frontend/src/` — Buscar padrões no código
    - Playwright (E2E): scripts definidos em `e2e/` e `playwright.config.ts`
