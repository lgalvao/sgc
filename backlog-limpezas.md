# Backlog de limpezas do frontend

## Estado atual (auditado em 2026-05-03)

- Gate de cruft: **ok**
- Score atual: **3022**
- Violacoes gate: **0**
- Avisos gate: **0**
- Baseline `fallow`:
  - `dead-code`: **0 issues**
  - duplicacao: **6 clone groups**, **153 linhas**, **0.8%**
  - health score: **90.9 (bom)**
  - dead files: **0.0%**
  - dead exports: **0.0%**
- Lint: **2 erros** (`no-explicit-any`), **39 warnings** (variaveis nao usadas e outros)
- Testes unitarios: **1186 testes** em **146 arquivos**, todos passando

## Ja concluido de verdade

### Infra e baseline

- configuracao do `fallow` em `frontend/.fallowrc.jsonc`
- estabilizacao do loop de validacao:
  - `node etc/scripts/sgc.js frontend cruft validar`
  - `npx fallow dead-code -r frontend`
  - `npx fallow dupes -r frontend`
  - `npx fallow health -r frontend`

### Remocao de arquivos completamente mortos

Esses arquivos foram verificados como ausentes no worktree atual:

- `frontend/src/services/alertaService.ts`
- `frontend/src/services/diagnosticoService.ts`
- `frontend/src/utils/csv.ts`
- `frontend/src/utils/validators.ts`
- `frontend/src/constants/index.ts`
- `frontend/src/constants/situacoes.ts`
- `frontend/src/utils/styleUtils.ts`
- remocao de testes que so sustentavam esses arquivos mortos
- remocao de dependencias orfas: `papaparse`, `@types/papaparse`, `zod`

### Cortes pequenos concluidos

- poda de `frontend/src/components/layout/MainNavbar.vue` (de ~258 para 175 linhas)
- consolidacao dos modais de observacao de cadastro:
  - `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  - `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`
- limpeza de exports em `frontend/src/utils/index.ts` (5 linhas agora)
- consolidacao dos modais do mapa em `frontend/src/components/mapa/modais/`:
  - `CompetenciaAtividadeItem.vue` confirmado como extracao legitima
  - `CriarCompetenciaModal.vue` renomeado para `CompetenciaEdicaoModal.vue`

### Fatiamentos estruturais concluidos

Arquivos originais removidos, substituidos pelas estruturas em pasta:

- `dateUtils.ts` → `frontend/src/utils/date/`
- `apiError.ts` → `frontend/src/utils/apiError/`
- `processoService.ts` → `frontend/src/services/processo/`
- `useAcesso.ts` → `frontend/src/composables/acesso/`
- `stores/subprocesso.ts` → `frontend/src/stores/subprocesso/`

### Waivers removidos (arquivos que saíram do waiver após o fatiamento)

- `frontend/src/utils/dateUtils.ts`
- `frontend/src/utils/apiError.ts`
- `frontend/src/services/processoService.ts`
- `frontend/src/composables/useAcesso.ts`
- `frontend/src/stores/subprocesso.ts`
- `frontend/src/components/comum/BuscadorUsuarios.vue`
- `frontend/src/components/mapa/CriarCompetenciaModal.vue`

### Baseline do `fallow dead-code` zerado

## Pendente — itens falsamente marcados como concluidos em versoes anteriores

O backlog anterior afirmava que os itens abaixo estavam feitos. A auditoria de 2026-05-03
mostrou que nenhum deles foi executado. Estao todos pendentes.

### Services que permanecem ativos (nao eram orfaos)

Os services abaixo continuam em uso por views e composables ativos. A afirmacao anterior
de que foram "limpos de APIs orfas" e falsa — os arquivos existem, tem importadores reais
e os testes correspondentes passam:

- `frontend/src/services/analiseService.ts` (usado por MapaView.vue e CadastroView.vue)
- `frontend/src/services/atividadeService.ts` (usado por useAtividadeForm, useCadastroAtividadesMutacoes, useConhecimentoMutacoes)
- `frontend/src/services/atribuicaoTemporariaService.ts` (usado por AtribuicaoTemporariaView.vue)
- `frontend/src/services/painelService.ts` (usado por PainelView.vue e RelatorioAndamentoView.vue)
- `frontend/src/services/subprocessoServiceBase.ts` (modulo interno, exportado via subprocessoService.ts)
- `frontend/src/services/subprocessoServiceContexto.ts` (idem)
- `frontend/src/services/subprocessoServiceMapa.ts` (idem)

### Specs que permanecem ativos (nao eram "herancas")

Todos esses arquivos existem, passam nos testes e cobrem codigo real:

- `frontend/src/services/__tests__/analiseService.spec.ts`
- `frontend/src/services/__tests__/atividadeService.spec.ts`
- `frontend/src/services/__tests__/atribuicaoTemporariaService.spec.ts`
- `frontend/src/services/__tests__/painelService.spec.ts`
- `frontend/src/services/__tests__/subprocessoService.spec.ts`
- `frontend/src/__tests__/views/PainelView.spec.ts`
- `frontend/src/__tests__/views/PainelViewCoverage.spec.ts`
- `frontend/src/views/__tests__/AtividadesCadastroView.spec.ts`
- `frontend/src/components/__tests__/InputData.spec.ts`

### Tipos que permanecem ativos

- `frontend/src/types/dtos.ts` — ainda tem 3 importadores:
  - `frontend/src/services/usuarioService.ts`
  - `frontend/src/services/processo/types.ts`
  - `frontend/src/services/processo/mapeadores.ts`
- `frontend/src/types/mapa-modelos.ts` — re-exportado via `frontend/src/types/mapa.ts`
- `frontend/src/types/organizacao.ts` — re-exportado via `frontend/src/types/tipos.ts`

O waiver de `frontend/src/types/dtos.ts` foi removido do arquivo de waivers, mas o
arquivo em si nunca foi removido. O tipo ainda e necessario enquanto houver importadores.

## Sobras imediatas a apagar (codigo morto detectado pelo lint)

Variaveis e funcoes atribuidas mas nunca usadas — sobras diretas da consolidacao dos modais.
Apagar na proxima rodada, sem risco de quebrar comportamento:

### `frontend/src/views/MapaView.vue` (8 itens)

Todos importados via destructuring de composables mas sem uso no template ou script:

- `sincronizarSugestoesMapa` (de useMapaSugestoes)
- `carregarSugestoesParaVisualizacao` (de useMapaSugestoes)
- `carregarSugestoesParaEdicao` (de useMapaSugestoes)
- `fecharModalValidar` (de useMapaAnaliseFluxo)
- `fecharModalDevolucao` (de useMapaAnaliseFluxo)
- `fecharModalExcluirCompetencia` (de useMapaCompetenciasMutacoes)
- `podeConfirmarDisponibilizacao` (de useMapaDisponibilizacao)
- `handleErrors` (funcao local definida mas nunca chamada)

### `frontend/src/views/ProcessoDetalheView.vue` (2 itens)

- `obterIdBotaoAcao`
- `obterTestIdBotaoAcao`

### `frontend/src/views/SubprocessoView.vue` (1 item)

- `fecharModalReabrir`

### Erros de lint (`any` explicito)

- `frontend/src/composables/useCadastroAtividadesMutacoes.ts` linha 83
- `frontend/src/services/processo/leituras.ts` linha 26

## Guardrails das proximas rodadas

- manter contratos publicos e `data-testid`
- nao recriar regra de acesso no frontend
- preferir helper local ou componente local antes de abrir nova camada
- apagar sobra liberada na mesma rodada
- manter tudo em **portugues brasileiro**
- sempre validar a rodada com o menor conjunto suficiente

## Prioridade 0 — apagar sobras imediatas

Apagar as variaveis e funcoes nao usadas listadas na secao "Sobras imediatas" acima.
Corte seguro: sem risco de quebrar comportamento ou contrato de template.

Tambem remover o `any` explicito em `useCadastroAtividadesMutacoes.ts` e `processo/leituras.ts`.

## Prioridade 1 — types/dtos.ts

Mover os DTOs para seus modulos naturais e remover o arquivo:

- `UnidadeParticipanteDto` e `ProcessoDetalheDto` → mover para `frontend/src/services/processo/types.ts`
- DTOs de autenticacao (`UnidadeDto`, `PerfilUnidadeDto`, `SessaoLoginDto`, `PermissoesSessaoDto`, `FluxoLoginResponseDto`) → mover para dentro de `frontend/src/services/usuarioService.ts` como tipos privados
- Remover `frontend/src/types/dtos.ts` apos migrar todos os importadores

## Prioridade 2 — hotspots confirmados pelo `fallow health` (score 90.9)

Os targets reais do fallow sao os seguintes (ordem por ROI):

### 1. `frontend/src/composables/usePerfil.ts` ⭐ NOVO

- maior impacto: 38 LOC, 7 dependentes diretos
- fallow classifica como "high impact · effort:medium"
- nao estava no backlog anterior — descoberto na auditoria de 2026-05-03

### 2. `frontend/src/utils/date/parsing.ts`

- 2 funcoes complexas sem cobertura de testes (`analisarData`, `analisarStringData`)
- fallow recomenda adicionar testes antes de modificar

### 3. `frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue`

- 3 funcoes complexas sem cobertura de testes
- fallow recomenda adicionar testes antes de modificar

### 4. `frontend/src/components/processo/processoAcoes.ts` ⭐ NOVO

- 2 funcoes complexas sem cobertura de testes
- nao estava no backlog anterior

### 5. `frontend/src/utils/apiError/normalizer.ts`

- hotspot confirmado; arquivo ja pequeno (44 linhas)
- as 3 responsabilidades (classificacao, leitura de payload, normalizacao) ja estao separadas
- avaliar se ainda ha ganho real antes de fatiar

### 6. `frontend/src/views/ProcessoCadastroView.vue`

- `handleApiErrors` segue pesado; view tem waiver em 470 linhas

### 7. `frontend/src/axios-setup.ts`

- `handleResponseError` ainda grande; arquivo tem waiver em 258 linhas

### 8. `frontend/src/views/cadastroDisponibilizacao.ts`

- `disponibilizarCadastro` concentra pre-validacao, execucao e tratamento

## Prioridade 3 — duplicacoes reais (fallow dupes — 6 grupos, 153 linhas)

### Facil (corte claro e seguro)

- `frontend/src/services/relatoriosService.ts`
  - 6 linhas internas duplicadas: logica de blob/download identica em dois metodos
  - extrair funcao privada `baixarPdf`

### Moderado

- `frontend/src/components/mapa/modais/MapaDevolucaoModal.vue`
  x `frontend/src/components/mapa/modais/MapaSugestoesEnvioModal.vue`
  - 22 linhas (script completo); diferem em variante, label e textos
- `frontend/src/services/processo/types.ts`
  x `frontend/src/types/processo.ts`
  - 14 linhas; overlap entre ProcessoDetalheResponseBackend e Processo

### Com cuidado

- `frontend/src/views/ProcessoCadastroView.vue`
  x `frontend/src/views/UnidadesView.vue`
  - 18 linhas; so mexer se cair junto com refatoracao maior das views
- `frontend/src/components/unidade/ArvoreUnidades.vue`
  x `frontend/src/views/ProcessoCadastroView.vue`
  - 9 linhas
- `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  x `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`
  - 9 linhas; so mexer se cair junto com outra rodada

## Prioridade 4 — waivers estruturais ainda vivos

### Views

- `frontend/src/views/MapaView.vue` (waiver 814)
- `frontend/src/views/CadastroView.vue` (waiver 707)
- `frontend/src/views/NotificacoesAdminView.vue` (waiver 490)
- `frontend/src/views/ProcessoCadastroView.vue` (waiver 470)
- `frontend/src/views/AtribuicaoTemporariaView.vue` (waiver 312)

### Componentes

- `frontend/src/components/unidade/ArvoreUnidades.vue` (waiver 471)
- `frontend/src/components/comum/TreeTable.vue` (waiver 360)
- `frontend/src/components/atividades/ImportarAtividadesModal.vue` (waiver 321)
- `frontend/src/components/atividades/AtividadeItem.vue` (waiver 303)
- `frontend/src/components/processo/ModalAcaoBloco.vue` (waiver 234)
- `frontend/src/components/processo/ProcessoFormFields.vue` (waiver 229, atual 228 — waiver obsoleto)
- `frontend/src/components/processo/SubprocessoCards.vue` (waiver 216)
- `frontend/src/components/unidade/UnidadeTreeNode.vue` (waiver 203)
- `frontend/src/components/comum/InlineEditor.vue` (waiver 197)
- `frontend/src/components/mapa/CompetenciaCard.vue` (waiver 191)
- `frontend/src/components/comum/TreeRowItem.vue` (waiver 181)

### Composables / stores / outros

- `frontend/src/stores/mapas.ts` (waiver 184)
- `frontend/src/stores/perfil.ts` (waiver 155)
- `frontend/src/composables/useFluxoSubprocesso.ts` (waiver 174/312 — entrada duplicada no waiver)
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts` (waiver 154)
- `frontend/src/composables/useBreadcrumbs.ts` (waiver 141)
- `frontend/src/composables/useMapaSugestoes.ts` (waiver 132)
- `frontend/src/composables/useCadastroRevisaoSemMudancas.ts` (waiver 130)
- `frontend/src/composables/useFluxoMapa.ts` (waiver 122)
- `frontend/src/axios-setup.ts` (waiver 258)
- `frontend/src/constants/textos.ts` (waiver 450)
- `frontend/src/utils/treeUtils.ts` (waiver 153)
- `frontend/src/App.vue` (waiver 165)

## Ordem recomendada de execucao

1. apagar variaveis mortas em MapaView.vue, ProcessoDetalheView.vue, SubprocessoView.vue
2. corrigir `any` em useCadastroAtividadesMutacoes.ts e processo/leituras.ts
3. mover tipos de `types/dtos.ts` e remover o arquivo
4. extrair helper em `relatoriosService.ts` (6 linhas, baixo risco)
5. atacar `usePerfil.ts` (novo hotspot forte)
6. adicionar testes para `date/parsing.ts` e `CompetenciaEdicaoModal.vue` antes de modificar
7. remover waiver obsoleto de `ProcessoFormFields.vue` (arquivo esta abaixo do limite)
8. corrigir entrada duplicada de `useFluxoSubprocesso.ts` no arquivo de waivers
9. atacar duplicacoes moderadas e waivers grandes

## Comandos uteis

```bash
node etc/scripts/sgc.js frontend cruft validar
npx fallow dead-code -r frontend
npx fallow dupes -r frontend
npx fallow health -r frontend
npm --prefix frontend run typecheck
npm --prefix frontend run test:unit
npx eslint frontend/src --ext .vue,.ts
```

## Leitura curta do momento (2026-05-03)

- baseline de cruft esta **ok**, gate sem violacoes
- `fallow dead-code` esta **zerado** — nenhum arquivo ou export morto
- `fallow health` marca **90.9** (bom, nao 98 como afirmava o backlog anterior)
- lint tem **2 erros** e **39 warnings** reais — principalmente variaveis nao usadas
- a "rodada ampla" anterior foi **parcialmente executada**: fatiamentos concluidos, mas
  services e specs que foram ditos "removidos" continuam ativos e saudaveis
- proximo passo natural e **apagar as sobras do mapa** (variaveis mortas no MapaView) e
  **limpar dtos.ts** — ambos sao cortes seguros e de impacto imediato
