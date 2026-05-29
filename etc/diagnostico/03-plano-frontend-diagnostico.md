# Relatório 03 — Plano de Implementação do Frontend de Diagnóstico

> **Contexto:** Backend de diagnóstico criado. Frontend atual tem apenas cards de navegação.
> Este plano descreve a implementação completa das views, composables, stores, services e rotas.

---

## 1. Visão Geral das Telas a Implementar

```
SubprocessoView.vue
  └─ SubprocessoCards.vue  (já existe — cards com navegação)
       ├─ → AutoavaliacaoDiagnosticoView.vue      [NOVA]
       ├─ → OcupacoesCriticasDiagnosticoView.vue  [NOVA]
       └─ → MonitoramentoDiagnosticoView.vue       [NOVA]
```

Telas adicionais vinculadas ao fluxo (acessíveis a partir das views acima):
- `ConsensoDiagnosticoView.vue` — chefia visualiza e edita consenso de um servidor
- `ConsensoAprovacaoView.vue` — servidor aprova ou discorda do consenso
- `DiagnosticoUnidadeView.vue` — gestor/admin analisa a unidade antes de validar/devolver

---

## 2. Estrutura de Arquivos a Criar

```
frontend/src/
├── services/
│   └── diagnosticoService.ts                   [NOVO]
├── types/
│   └── diagnostico-competencias.ts             [NOVO] (não confundir com diagnostico.ts existente)
├── composables/
│   ├── useDiagnosticoContexto.ts               [NOVO]
│   ├── useAutoavaliacaoDiagnostico.ts          [NOVO]
│   ├── useConsensoDiagnostico.ts               [NOVO]
│   ├── useEquipeDiagnostico.ts                 [NOVO]
│   ├── useOcupacoesCriticasDiagnostico.ts      [NOVO]
│   ├── useFluxoDiagnostico.ts                  [NOVO]
│   └── useMonitoramentoDiagnostico.ts          [NOVO]
├── router/
│   └── diagnostico.routes.ts                   [NOVO]
└── views/
    ├── AutoavaliacaoDiagnosticoView.vue         [NOVA]
    ├── OcupacoesCriticasDiagnosticoView.vue     [NOVA]
    ├── MonitoramentoDiagnosticoView.vue         [NOVA]
    ├── ConsensoDiagnosticoView.vue              [NOVA]
    └── DiagnosticoUnidadeView.vue               [NOVA]
```

---

## 3. Fase 1 — Fundação (tipos, service, rotas)

### 3.1 `types/diagnostico-competencias.ts`

Criar interfaces TypeScript espelhando todos os DTOs do backend:

```typescript
// Enums
export type SituacaoAvaliacaoServidor =
  | 'AUTOAVALIACAO_NAO_REALIZADA'
  | 'AUTOAVALIACAO_CONCLUIDA'
  | 'CONSENSO_CRIADO'
  | 'CONSENSO_APROVADO'
  | 'AVALIACAO_IMPOSSIBILITADA';

export type SituacaoCapacitacao = 'NA' | 'AC' | 'EC' | 'C' | 'I';

export type SituacaoDiagnostico = 'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO';

// Modelos
export interface CompetenciaResumoDiag {
  codigo: number;
  descricao: string;
}

export interface AvaliacaoCompetencia {
  competenciaCodigo: number;
  importancia: number | null;
  dominio: number | null;
}

export interface DiagnosticoContexto {
  processoCodigo: number;
  subprocessoCodigo: number;
  unidadeCodigo: number;
  unidadeSigla: string;
  unidadeNome: string;
  situacaoSubprocesso: string;
  competencias: CompetenciaResumoDiag[];
}

export interface Autoavaliacao {
  competencias: AvaliacaoCompetencia[];
  situacaoServidor: SituacaoAvaliacaoServidor;
}

export interface Consenso {
  competencias: AvaliacaoCompetencia[];
  situacaoServidor: SituacaoAvaliacaoServidor;
}

export interface ServidorDiagnostico {
  titulo: string;
  nome: string;
  situacaoServidor: SituacaoAvaliacaoServidor;
  competencias: AvaliacaoCompetencia[];
}

export interface ItemEquipeDiagnostico {
  titulo: string;
  nome: string;
  situacaoServidor: SituacaoAvaliacaoServidor;
}

export interface DiagnosticoEquipe {
  itens: ItemEquipeDiagnostico[];
}

export interface OcupacaoCriticaItem {
  competenciaCodigo: number;
  servidorTitulo: string;
  situacaoCapacitacao: SituacaoCapacitacao;
}

export interface DiagnosticoUnidade {
  unidade: { codigo: number; sigla: string; nome: string; situacao: string };
  servidores: ServidorDiagnostico[];
  ocupacoesCriticas: OcupacaoCriticaItem[];
  movimentacoes: unknown[]; // usar MovimentacaoDto existente
}

// Requests
export interface AutoavaliacaoRequest { competencias: AvaliacaoCompetencia[] }
export interface ConsensoRequest { competencias: AvaliacaoCompetencia[]; motivoReabertura?: string }
export interface OcupacaoCriticaRequest { servidorTitulo: string; competenciaCodigo: number; situacaoCapacitacao: SituacaoCapacitacao }
export interface OcupacoesCriticasRequest { ocupacoes: OcupacaoCriticaRequest[] }
export interface JustificativaRequest { justificativa: string }
```

### 3.2 `services/diagnosticoService.ts`

```typescript
import type { ... } from '@/types/diagnostico-competencias';
import api from '@/axios-setup'; // ou o cliente HTTP padrão do projeto

const BASE = '/api/diagnosticos/subprocessos';

export async function obterContextoDiagnostico(codSp: number) { ... }
export async function obterAutoavaliacao(codSp: number) { ... }
export async function salvarAutoavaliacao(codSp: number, req: AutoavaliacaoRequest) { ... }
export async function concluirAutoavaliacao(codSp: number) { ... }
export async function salvarConsenso(codSp: number, servidorTitulo: string, req: ConsensoRequest) { ... }
export async function obterConsenso(codSp: number) { ... }
export async function aprovarConsenso(codSp: number) { ... }
export async function impossibilitarAvaliacao(codSp: number, servidorTitulo: string, req: JustificativaRequest) { ... }
export async function obterEquipe(codSp: number) { ... }
export async function salvarOcupacoesCriticas(codSp: number, req: OcupacoesCriticasRequest) { ... }
export async function obterDiagnosticoUnidade(codSp: number) { ... }
export async function concluirDiagnostico(codSp: number) { ... }
export async function validarDiagnostico(codSp: number, observacoes?: string) { ... }
export async function devolverDiagnostico(codSp: number, justificativa: string) { ... }
export async function homologarDiagnostico(codSp: number, observacoes?: string) { ... }
```

### 3.3 `router/diagnostico.routes.ts`

```typescript
const diagnosticoRoutes: RouteRecordRaw[] = [
  {
    path: '/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao',
    name: 'AutoavaliacaoDiagnostico',
    component: () => import('@/views/AutoavaliacaoDiagnosticoView.vue'),
    props: (route) => ({ codSubprocesso: Number(route.params.codSubprocesso), siglaUnidade: route.params.siglaUnidade }),
    meta: { title: 'Autoavaliação — Diagnóstico' },
  },
  {
    path: '/diagnostico/:codSubprocesso/:siglaUnidade/ocupacoes-criticas',
    name: 'OcupacoesCriticasDiagnostico',
    component: () => import('@/views/OcupacoesCriticasDiagnosticoView.vue'),
    props: (route) => ({ codSubprocesso: Number(route.params.codSubprocesso), siglaUnidade: route.params.siglaUnidade }),
    meta: { title: 'Ocupações Críticas — Diagnóstico' },
  },
  {
    path: '/diagnostico/:codSubprocesso/:siglaUnidade/monitoramento',
    name: 'MonitoramentoDiagnostico',
    component: () => import('@/views/MonitoramentoDiagnosticoView.vue'),
    props: (route) => ({ codSubprocesso: Number(route.params.codSubprocesso), siglaUnidade: route.params.siglaUnidade }),
    meta: { title: 'Monitoramento — Diagnóstico' },
  },
  {
    path: '/diagnostico/:codSubprocesso/:siglaUnidade/consenso/:servidorTitulo',
    name: 'ConsensoDiagnostico',
    component: () => import('@/views/ConsensoDiagnosticoView.vue'),
    props: true,
    meta: { title: 'Consenso — Diagnóstico' },
  },
  {
    path: '/diagnostico/:codSubprocesso/:siglaUnidade/unidade',
    name: 'DiagnosticoUnidade',
    component: () => import('@/views/DiagnosticoUnidadeView.vue'),
    props: true,
    meta: { title: 'Análise da Unidade — Diagnóstico' },
  },
];
```

Adicionar `diagnosticoRoutes` ao `router/index.ts`.

---

## 4. Fase 2 — Composables (Pinia Colada)

Usar o padrão já consolidado no projeto (Pinia Colada com `useQuery`/`useMutation`):

### 4.1 `composables/useDiagnosticoContexto.ts`
- Query: `obterContextoDiagnostico(codSubprocesso)` — chave `['diagnostico', 'contexto', codSubprocesso]`
- `staleTime: Infinity` — invalidar apenas após ações de fluxo

### 4.2 `composables/useAutoavaliacaoDiagnostico.ts`
- Query: `obterAutoavaliacao(codSubprocesso)` — chave `['diagnostico', 'autoavaliacao', codSubprocesso]`
- Mutation `salvarAutoavaliacao`: autosave com debounce de 800ms
- Mutation `concluirAutoavaliacao`: invalida `['diagnostico', 'contexto', codSubprocesso]` e `['diagnostico', 'equipe', codSubprocesso]`
- Estado reativo local para edição do formulário sem requerer network a cada keypress

### 4.3 `composables/useConsensoDiagnostico.ts`
- Query: `obterConsenso(codSubprocesso)` — chave `['diagnostico', 'consenso', codSubprocesso]`
- Mutations: `salvarConsenso`, `aprovarConsenso`, `impossibilitarAvaliacao`
- Após `salvarConsenso`: invalidar equipe e consenso
- Após `aprovarConsenso`: invalidar equipe e contexto

### 4.4 `composables/useEquipeDiagnostico.ts`
- Query: `obterEquipe(codSubprocesso)` — chave `['diagnostico', 'equipe', codSubprocesso]`
- Sem mutations (apenas leitura)

### 4.5 `composables/useOcupacoesCriticasDiagnostico.ts`
- Query: `obterDiagnosticoUnidade(codSubprocesso)` — chave `['diagnostico', 'unidade', codSubprocesso]`
- Mutation `salvarOcupacoesCriticas`: autosave com debounce
- Exibe apenas a parte de ocupações críticas

### 4.6 `composables/useFluxoDiagnostico.ts`
- Mutations para ações de fluxo: `concluirDiagnostico`, `validarDiagnostico`, `devolverDiagnostico`, `homologarDiagnostico`
- Após cada ação: invalidar contexto e redirecionar para `Subprocesso` ou atualizar a view

### 4.7 `composables/useMonitoramentoDiagnostico.ts`
- Query: futura `obterMonitoramento(codProcesso)` — chave `['diagnostico', 'monitoramento', codProcesso]`
- Fallback: pode usar `obterDiagnosticoUnidade` por enquanto para cada subprocesso

---

## 5. Fase 3 — Views

### 5.1 `AutoavaliacaoDiagnosticoView.vue`

**Perfis que acessam:** SERVIDOR (sua própria avaliação), CHEFE (avaliação de qualquer servidor)

**Seções:**
1. **Cabeçalho:** sigla da unidade + situação do subprocesso
2. **Formulário de competências:** tabela com colunas `Competência | Importância (1-5) | Domínio (1-5)`
   - Inputs numéricos ou selects com opções de 1 a 5
   - Autosave a cada alteração (debounce 800ms)
   - Indicador visual de "Salvo" / "Salvando..."
3. **Ações:**
   - SERVIDOR: botão "Concluir autoavaliação" (com confirmação)
   - CHEFE: formulário para **cada servidor** da equipe + botão "Salvar consenso"
4. **Status da equipe:** lista de servidores com badge de situação (consulta `obterEquipe`)

**Estados da tela por situação do servidor:**
- `AUTOAVALIACAO_NAO_REALIZADA` → formulário editável (SERVIDOR)
- `AUTOAVALIACAO_CONCLUIDA` → leitura apenas (SERVIDOR) + formulário de consenso visível para CHEFE
- `CONSENSO_CRIADO` → servidor vê consenso e pode aprovar ou discordar
- `CONSENSO_APROVADO` → apenas leitura para todos

### 5.2 `OcupacoesCriticasDiagnosticoView.vue`

**Perfis que acessam:** CHEFE (edição), GESTOR/ADMIN (visualização)

**Seções:**
1. **Cabeçalho:** unidade + situação
2. **Tabela de ocupações críticas:**
   - Colunas: `Servidor | Competência | Situação de Capacitação`
   - Situação de capacitação: select com opções:
     - `NA` — Não se aplica
     - `AC` — A capacitar
     - `EC` — Em capacitação
     - `C` — Capacitado
     - `I` — Instrutor
   - Autosave (debounce 800ms)
3. **Ação:** botão "Concluir diagnóstico da unidade" (apenas CHEFE, com validação backend)

### 5.3 `MonitoramentoDiagnosticoView.vue`

**Perfis que acessam:** GESTOR, ADMIN, CHEFE (apenas sua unidade)

**Seções:**
1. **Cabeçalho:** nome/descrição do processo
2. **Tabela de unidades:**
   - Colunas: `Unidade | Situação | Localização atual | Ações`
   - Filtros por situação
   - Badge colorido por situação (`AUTOAVALIACAO_EM_ANDAMENTO` → amarelo, `CONCLUIDO` → azul, `VALIDADO` → verde)
3. **Ações por linha:**
   - GESTOR: "Validar" / "Devolver" (com modal de observações/justificativa)
   - ADMIN: "Homologar" (com modal de observações)
4. **Ações em bloco:** checkbox para seleção + "Validar selecionados" (quando disponível no backend)
5. **Link:** cada linha navega para `DiagnosticoUnidadeView` ao clicar

### 5.4 `ConsensoDiagnosticoView.vue`

**Perfis que acessam:** CHEFE (criar/editar), SERVIDOR (aprovar)

**Seções:**
1. **Tabela de competências:** importância + domínio preenchidos pela chefia (editável para CHEFE)
2. **Campo motivo de reabertura:** exibido apenas quando servidor já aprovou e chefia reabre
3. **Ações para CHEFE:** "Salvar consenso"
4. **Ações para SERVIDOR:** "Aprovar consenso" / "Registrar discordância" (leva de volta à autoavaliação)

### 5.5 `DiagnosticoUnidadeView.vue`

**Perfis que acessam:** GESTOR, ADMIN

**Seções:**
1. **Resumo da unidade:** nome, sigla, situação, data de conclusão
2. **Servidores e avaliações:** tabela com consenso final por competência
3. **Ocupações críticas:** tabela com situação de capacitação
4. **Histórico de movimentações:** timeline das transições
5. **Ações:** "Validar" / "Devolver" / "Homologar" com modais de confirmação

---

## 6. Fase 4 — Integração com SubprocessoView e breadcrumb

- Ajustar `SubprocessoView.vue` para exibir informações de contexto de diagnóstico (status de progresso da equipe)
- Adicionar breadcrumb para as views de diagnóstico: `Painel > Processo > Unidade > Diagnóstico > [tela atual]`
- Garantir que `useInvalidacaoNavegacao` invalide as queries de diagnóstico em transições relevantes

---

## 7. Fase 5 — Testes

### 7.1 Unitários (Vitest)
- Composables: `useAutoavaliacaoDiagnostico`, `useConsensoDiagnostico`, `useFluxoDiagnostico`
- Service: `diagnosticoService.ts` (mock do axios)
- Lógica de cálculo de gap no frontend (se replicada)

### 7.2 De componente
- `AutoavaliacaoDiagnosticoView` — renderização por perfil (SERVIDOR vs CHEFE)
- `OcupacoesCriticasDiagnosticoView` — interação com selects, autosave
- `MonitoramentoDiagnosticoView` — filtros, ações em bloco

### 7.3 E2E (Playwright)
- Fluxo completo: SERVIDOR preenche autoavaliação → CHEFE cria consenso → SERVIDOR aprova → CHEFE preenche ocupações → CHEFE conclui → GESTOR valida → ADMIN homologa
- Fluxo de devolução: GESTOR devolve → CHEFE corrige → GESTOR valida

---

## 8. Sequência de Entrega Recomendada

```
Semana 1:
  [x] Pré-requisitos backend (relatório 02 — itens 2.1 a 2.5)
  [x] diagnostico-competencias.ts
  [x] diagnosticoService.ts
  [x] diagnostico.routes.ts + registro no router

Semana 2:
  [x] useDiagnosticoContexto.ts
  [x] useAutoavaliacaoDiagnostico.ts
  [x] AutoavaliacaoDiagnosticoView.vue (SERVIDOR)
  [x] ConsensoDiagnosticoView.vue (CHEFE)

Semana 3:
  [x] useOcupacoesCriticasDiagnostico.ts
  [x] OcupacoesCriticasDiagnosticoView.vue
  [x] useFluxoDiagnostico.ts (concluir)
  [x] Testes unitários e de componente das telas acima

Semana 4:
  [x] useMonitoramentoDiagnostico.ts
  [x] MonitoramentoDiagnosticoView.vue
  [x] DiagnosticoUnidadeView.vue
  [x] Ações de validar/devolver/homologar
  [x] Testes E2E do fluxo completo
```

---

## 9. Convenções a Seguir

| Aspecto | Convenção |
|---|---|
| Componentes | `PascalCase` — `AutoavaliacaoDiagnosticoView.vue` |
| Composables | `camelCase` — `useAutoavaliacaoDiagnostico.ts` |
| Services | `camelCase` — `diagnosticoService.ts` |
| Stores | `use{Nome}Store` — se necessária store global |
| Chaves de query | Arrays: `['diagnostico', 'autoavaliacao', codSubprocesso]` |
| Autosave | `useDebounceFn` com 800ms antes de chamar o service |
| Erros | Usar o padrão de toast/alert já existente no projeto |
| Textos | Centralizar em `constants/textos.ts` (seção `diagnostico`) |
| Ids de testes | `data-testid="autoavaliacao-competencia-{codigo}"` etc. |

---

## 10. Riscos e Mitigações

| Risco | Mitigação |
|---|---|
| Backend sem criação automática do Diagnostico | Validar em ambiente de teste antes de iniciar as views |
| Autosave concorrente (múltiplos servidores) | Idempotência garantida pelo backend com chave por servidor+competência |
| Performance da tabela de ocupações (muitas linhas) | Paginação virtual ou lazy rendering se necessário |
| Regra C13 não definida | Implementar sem a regra; adicionar placeholder com aviso visual |
| Rotas com params ambíguos | Usar `props: true` ou factory de props no router para garantir tipagem |
