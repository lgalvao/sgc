# Backlog de limpezas do frontend

## Estado atual

- Gate de cruft: **ok**
- Score atual: **3594**
- Violacoes: **0**
- Baseline do `fallow`:
  - dead files: **0**
  - dead export pct: **9.3%**
  - health score: **94.6 / A**
  - unused dep: **1** (`zod`)

## Ja concluido nesta rodada

- configuracao util do `fallow` em `frontend/.fallowrc.jsonc`
- remocao de arquivos mortos reais:
  - `frontend/src/services/alertaService.ts`
  - `frontend/src/services/diagnosticoService.ts`
  - `frontend/src/utils/csv.ts`
  - `frontend/src/utils/validators.ts`
- remocao dos testes que so sustentavam esses arquivos
- remocao de dependencias orfas:
  - `papaparse`
  - `@types/papaparse`
- poda de `frontend/src/components/layout/MainNavbar.vue`
- consolidacao dos modais de observacao de cadastro:
  - `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  - `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`

## Guardrails para as proximas rodadas

- manter contratos publicos e `data-testid`
- nao recriar regra de acesso no frontend
- preferir helper local ou componente local antes de abrir nova camada
- apagar sobra liberada na mesma rodada
- rodar sempre:
  - `node etc/scripts/sgc.js frontend cruft validar`
  - `npx fallow dead-code -r frontend`
  - `npx fallow dupes -r frontend`
  - `npx fallow health -r frontend`

## Prioridade 0 - ganhos pequenos e seguros

### 1. Remover API publica sobrando em arquivos pequenos

Atacar primeiro exports mortos sem abrir refatoracao grande:

- `frontend/src/composables/useLocalStorage.ts`
  - `useLocalStorageMultiple`
  - `removeFromLocalStorage`
  - `removeMultipleFromLocalStorage`
- `frontend/src/constants/situacoes.ts`
  - `LABELS_SITUACAO`
- `frontend/src/utils/styleUtils.ts`
  - `badgeClass`
  - `iconeTipo`
- `frontend/src/utils/formatters.ts`
  - `formatNumeroCsvBR`

### 2. Verificar dependencia orfa

- `zod`
  - hoje o `fallow` aponta `unused-dep:zod`
  - confirmar se ainda existe uso real no frontend antes de remover

## Prioridade 1 - hotspots com melhor custo/beneficio

Esses pontos aparecem ao mesmo tempo como **waiver**, **arquivo acima do target** ou **high complexity**.

### 1. `frontend/src/utils/dateUtils.ts`

- sinais:
  - waiver ativo
  - `parseDate` com complexidade alta
  - varios exports mortos
- corte recomendado:
  - separar parsing
  - separar formatacao
  - separar calculos/validacoes

### 2. `frontend/src/utils/apiError.ts`

- sinais:
  - waiver ativo
  - `normalizeError` e `mapStatusToKind` como hotspots
  - exports mortos (`existsOrFalse`, `getOrNull`)
- corte recomendado:
  - separar classificacao HTTP
  - separar formatacao para UI
  - remover helpers sem consumidor

### 3. `frontend/src/services/processoService.ts`

- sinais:
  - waiver ativo
  - exports mortos (`obterProcessoPorCodigo`, `buscarSubprocessosElegiveis`, `buscarSubprocessos`)
  - duplicacao com `src/types/processo.ts`
- corte recomendado:
  - fatiar leituras x acoes
  - remover API publica sem consumidor
  - endurecer contratos na borda

### 4. `frontend/src/composables/useAcesso.ts`

- sinais:
  - waiver ativo
  - hotspot em `acaoPrincipalCadastro`
- corte recomendado:
  - separar acesso de cadastro
  - separar acesso de mapa
  - separar acoes em bloco

### 5. `frontend/src/stores/subprocesso.ts`

- sinais:
  - waiver ativo
  - ainda e o store de suporte mais sensivel
- corte recomendado:
  - separar cache curto
  - separar consultas derivadas
  - manter a decisao de reaproveitamento dentro do store

## Prioridade 2 - waivers grandes que ainda pedem poda estrutural

### Views

- `frontend/src/views/MapaView.vue`
  - criterio de remocao do waiver: extrair orquestracao, secoes de tela e interacoes de mutacao
- `frontend/src/views/CadastroView.vue`
  - criterio de remocao do waiver: separar orquestracao, secoes visuais e regras locais
- `frontend/src/views/NotificacoesAdminView.vue`
  - criterio de remocao do waiver: separar busca, listagem e acoes
- `frontend/src/views/ProcessoCadastroView.vue`
  - sinais extras:
    - `handleApiErrors` e hotspot de complexidade
    - duplicacao com `UnidadesView.vue`
- `frontend/src/views/AtribuicaoTemporariaView.vue`
  - separar formulario e listagem

### Componentes

- `frontend/src/components/unidade/ArvoreUnidades.vue`
- `frontend/src/components/comum/TreeTable.vue`
- `frontend/src/components/comum/BuscadorUsuarios.vue`
- `frontend/src/components/atividades/ImportarAtividadesModal.vue`
- `frontend/src/components/atividades/AtividadeItem.vue`
- `frontend/src/components/processo/ModalAcaoBloco.vue`
- `frontend/src/components/processo/ProcessoFormFields.vue`
- `frontend/src/components/processo/SubprocessoCards.vue`
- `frontend/src/components/mapa/CriarCompetenciaModal.vue`
- `frontend/src/components/unidade/UnidadeTreeNode.vue`

### Outros

- `frontend/src/axios-setup.ts`
  - separar interceptors, sessao e normalizacao de erro
- `frontend/src/types/dtos.ts`
  - separar DTOs por dominio
- `frontend/src/constants/textos.ts`
  - dividir por modulo ou fluxo

## Prioridade 3 - duplicacoes apontadas pelo fallow

Atacar so quando o corte for pequeno e nao criar abstracao artificial.

### Boas candidatas

- `frontend/src/components/mapa/MapaEdicaoModais.vue`
  x `frontend/src/components/mapa/MapaFluxoModais.vue`
- `frontend/src/services/painelService.ts`
  - dois blocos repetidos
- `frontend/src/services/relatoriosService.ts`
  - dois blocos repetidos

### Candidatas com cuidado

- `frontend/src/views/ProcessoCadastroView.vue`
  x `frontend/src/views/UnidadesView.vue`
- `frontend/src/components/unidade/ArvoreUnidades.vue`
  x `frontend/src/views/ProcessoCadastroView.vue`

## Prioridade 4 - limpeza de exports mortos em services e types

Esses pontos ainda nao sao prova automatica de remocao imediata, mas merecem auditoria dirigida:

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

1. `dateUtils.ts`
2. `apiError.ts`
3. `processoService.ts`
4. `useAcesso.ts`
5. `stores/subprocesso.ts`
6. `MapaEdicaoModais.vue` x `MapaFluxoModais.vue`
7. `ProcessoCadastroView.vue`
8. `MapaView.vue`
9. `CadastroView.vue`
10. limpeza progressiva de exports mortos em services/types

## Comandos uteis

```bash
node etc/scripts/sgc.js frontend cruft validar
npx fallow dead-code -r frontend
npx fallow dupes -r frontend
npx fallow health -r frontend
pnpm -C frontend run typecheck
pnpm -C frontend run test:unit
```

## Leitura curta do estado atual

- o `fallow` ainda tem bastante valor
- a fase mais barata de remocao de arquivos mortos ja foi feita
- o proximo ganho real esta em:
  - reduzir superficie publica sobrando
  - podar duplicacao localizada
  - fatiar hotspots reais de complexidade
  - remover waivers que ja estiverem maduros para cair
