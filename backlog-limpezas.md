# Backlog de limpezas do frontend

## Estado atual

- Gate de cruft: **ok**
- Score atual: **3103**
- Violacoes: **0**
- Baseline atual do `fallow`:
  - dead files: **3**
  - dead export pct: **6.6%**
  - health score: **95.9 / A**
  - unused dep: **0**

## Ja concluido de verdade

### Infra e baseline

- configuracao util do `fallow` em `frontend/.fallowrc.jsonc`
- estabilizacao do loop de validacao com:
  - `node etc/scripts/sgc.js frontend cruft validar`
  - `npx fallow dead-code -r frontend`
  - `npx fallow dupes -r frontend`
  - `npx fallow health -r frontend`

### Remocao de codigo morto

- remocao de arquivos mortos reais:
  - `frontend/src/services/alertaService.ts`
  - `frontend/src/services/diagnosticoService.ts`
  - `frontend/src/utils/csv.ts`
  - `frontend/src/utils/validators.ts`
- remocao dos testes que so sustentavam esses arquivos
- remocao de dependencias orfas:
  - `papaparse`
  - `@types/papaparse`
  - `zod`

### Cortes pequenos concluidos

- poda de `frontend/src/components/layout/MainNavbar.vue`
- consolidacao dos modais de observacao de cadastro:
  - `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  - `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`

### Fatiamentos estruturais ja feitos

- `dateUtils.ts` fatiado em `frontend/src/utils/date/`
- `apiError.ts` fatiado em `frontend/src/utils/apiError/`
- `processoService.ts` fatiado em `frontend/src/services/processo/`
- `useAcesso.ts` fatiado em `frontend/src/composables/acesso/`
- `stores/subprocesso.ts` fatiado em `frontend/src/stores/subprocesso/`

### Waivers removidos nesta campanha

- `frontend/src/utils/dateUtils.ts`
- `frontend/src/utils/apiError.ts`
- `frontend/src/services/processoService.ts`
- `frontend/src/composables/useAcesso.ts`
- `frontend/src/stores/subprocesso.ts`
- `frontend/src/components/comum/BuscadorUsuarios.vue`
- `frontend/src/components/mapa/CriarCompetenciaModal.vue`

## Em andamento agora

Esses pontos ja aparecem no worktree e precisam ser considerados como rodada viva:

- consolidacao dos modais do mapa em `frontend/src/components/mapa/modais/`
- enxugamento de:
  - `frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue`
  - `frontend/src/components/mapa/modais/MapaModaisRoot.vue`
  - `frontend/src/composables/useCadastroAtividadesMutacoes.ts`
- novo componente em avaliacao:
  - `frontend/src/components/mapa/modais/CompetenciaAtividadeItem.vue`

## Guardrails das proximas rodadas

- manter contratos publicos e `data-testid`
- nao recriar regra de acesso no frontend
- preferir helper local ou componente local antes de abrir nova camada
- apagar sobra liberada na mesma rodada
- manter tudo em **portugues brasileiro**
- sempre validar a rodada com o menor conjunto suficiente

## Prioridade 0 - sobras pequenas e baratas

### Dead files atuais

O `fallow` ainda acusa estes arquivos como mortos:

- `frontend/src/constants/index.ts`
- `frontend/src/constants/situacoes.ts`
- `frontend/src/utils/styleUtils.ts`

### Exports mortos pequenos

- `frontend/src/composables/useWebStorage.ts`
  - `removerDoArmazenamento`
  - `removerMultiplosDoArmazenamento`
- `frontend/src/utils/date/validation.ts`
  - `ehDataValidaEFutura`
- `frontend/src/utils/index.ts`
  - re-export de `flattenTree`
- `frontend/src/services/processo/types.ts`
  - `Subprocesso`
  - `SubprocessoElegivel`
  - `UnidadeParticipante`

## Prioridade 1 - hotspots fortes e ainda vivos

### 1. `frontend/src/utils/apiError/normalizer.ts`

- hotspot mais forte restante
- `normalizarErro` segue com alta complexidade
- alvo bom para separar:
  - classificacao
  - normalizacao
  - leitura de payload

### 2. `frontend/src/utils/date/parsing.ts`

- `analisarData` e `analisarStringData` ainda seguem quentes
- o fatiamento melhorou a fronteira, mas o miolo ainda esta pesado

### 3. `frontend/src/views/ProcessoCadastroView.vue`

- ainda e hotspot real
- `handleApiErrors` segue concentrando ramificacao
- ainda aparece em duplicacao com `UnidadesView.vue`

### 4. `frontend/src/axios-setup.ts`

- continua grande e com hotspot em `handleResponseError`
- segue com waiver

### 5. `frontend/src/views/cadastroDisponibilizacao.ts`

- `disponibilizarCadastro` segue quente
- bom alvo para separar pre-validacao, erro e execucao

## Prioridade 2 - waivers grandes ainda ativos

### Views

- `frontend/src/views/MapaView.vue`
- `frontend/src/views/CadastroView.vue`
- `frontend/src/views/NotificacoesAdminView.vue`
- `frontend/src/views/ProcessoCadastroView.vue`
- `frontend/src/views/AtribuicaoTemporariaView.vue`

### Componentes

- `frontend/src/components/unidade/ArvoreUnidades.vue`
- `frontend/src/components/comum/TreeTable.vue`
- `frontend/src/components/atividades/ImportarAtividadesModal.vue`
- `frontend/src/components/atividades/AtividadeItem.vue`
- `frontend/src/components/processo/ModalAcaoBloco.vue`
- `frontend/src/components/processo/ProcessoFormFields.vue`
- `frontend/src/components/processo/SubprocessoCards.vue`
- `frontend/src/components/unidade/UnidadeTreeNode.vue`
- `frontend/src/components/comum/InlineEditor.vue`
- `frontend/src/components/comum/TreeRowItem.vue`
- `frontend/src/components/mapa/CompetenciaCard.vue`

### Composables / stores / outros

- `frontend/src/stores/mapas.ts`
- `frontend/src/stores/perfil.ts`
- `frontend/src/composables/useFluxoSubprocesso.ts`
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts`
- `frontend/src/composables/useBreadcrumbs.ts`
- `frontend/src/composables/useMapaSugestoes.ts`
- `frontend/src/composables/useCadastroRevisaoSemMudancas.ts`
- `frontend/src/composables/useFluxoMapa.ts`
- `frontend/src/axios-setup.ts`
- `frontend/src/types/dtos.ts`
- `frontend/src/constants/textos.ts`
- `frontend/src/utils/treeUtils.ts`
- `frontend/src/App.vue`

## Prioridade 3 - duplicacoes apontadas pelo fallow

Atacar so quando houver corte pequeno e contrato claro.

### Boas candidatas

- consolidacao dos modais do mapa
  - rodada ja esta em andamento
- `frontend/src/services/painelService.ts`
  - dois blocos repetidos
- `frontend/src/services/relatoriosService.ts`
  - dois blocos repetidos

### Candidatas com cuidado

- `frontend/src/views/ProcessoCadastroView.vue`
  x `frontend/src/views/UnidadesView.vue`
- `frontend/src/components/unidade/ArvoreUnidades.vue`
  x `frontend/src/views/ProcessoCadastroView.vue`

## Prioridade 4 - limpeza dirigida de services e types

Esses pontos ainda nao devem ser removidos no escuro, mas merecem auditoria dedicada:

- `frontend/src/services/analiseService.ts`
  - `listarAnalisesValidacao`
- `frontend/src/services/atividadeService.ts`
  - `listarAtividades`
  - `obterAtividadePorCodigo`
  - `listarConhecimentos`
- `frontend/src/services/atribuicaoTemporariaService.ts`
  - `buscarTodasAtribuicoes`
- `frontend/src/services/painelService.ts`
  - `listarAlertas`
- `frontend/src/services/subprocessoServiceContexto.ts`
  - `listarAtividades`
  - `obterStatus`
  - `buscarSubprocessoDetalhe`
  - `buscarSubprocessoPorProcessoEUnidade`
  - `listarAnalisesCadastro`
  - `listarAnalisesValidacao`
- `frontend/src/services/subprocessoServiceMapa.ts`
  - `obterMapaAjuste`
  - `aceitarCadastroEmBloco`
  - `homologarCadastroEmBloco`
  - `aceitarValidacaoEmBloco`
  - `homologarValidacaoEmBloco`
  - `disponibilizarMapaEmBloco`
- `frontend/src/types/dtos.ts`
  - `ImpactoMapaDto`
  - `AlertaDto`
  - `UsuarioDto`
  - `AtividadeOperacaoResponseDto`
- `frontend/src/types/mapa-modelos.ts`
  - `MapaVisualizacao`
- `frontend/src/types/organizacao.ts`
  - `AtribuicaoTemporaria`

## Ordem recomendada de execucao

1. fechar a rodada em andamento dos modais do mapa
2. remover os dead files restantes (`constants/index.ts`, `situacoes.ts`, `styleUtils.ts`)
3. podar `apiError/normalizer.ts`
4. podar `date/parsing.ts`
5. atacar `ProcessoCadastroView.vue`
6. atacar `axios-setup.ts`
7. limpar exports mortos pequenos em `useWebStorage`, `date/validation`, `services/processo/types`
8. seguir para services e types ainda sinalizados pelo `fallow`

## Comandos uteis

```bash
node etc/scripts/sgc.js frontend cruft validar
npx fallow dead-code -r frontend
npx fallow dupes -r frontend
npx fallow health -r frontend
pnpm -C frontend run typecheck
pnpm -C frontend run test:unit
```

## Leitura curta do momento

- a campanha esta indo **bem**
- houve queda real de score e reducao de waivers
- o backlog anterior estava **otimista demais**
- o foco agora nao e mais apagar obviedades; e fechar:
  - rodada viva dos modais do mapa
  - dead files pequenos que sobraram
  - hotspots ainda quentes de parsing, erro e cadastro
