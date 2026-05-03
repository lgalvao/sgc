# Backlog de limpezas do frontend

## Estado atual

- Gate de cruft: **ok**
- Score atual: **3542**
- Violacoes: **0**
- Baseline do `fallow`:
  - dead files: **0**
  - dead export pct: **9.3%**
  - health score: **94.6 / A**
  - unused dep: **0** (zod removido)

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
  - `zod`
- poda de `frontend/src/components/layout/MainNavbar.vue`
- consolidacao dos modais de observacao de cadastro:
  - `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  - `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`
- **Limpeza de exports mortos (P0):**
  - `useLocalStorage.ts` (poda de `useLocalStorageMultiple`, `removeFromLocalStorage`, etc)
  - `situacoes.ts` (poda de `LABELS_SITUACAO`)
  - `styleUtils.ts` (poda completa, arquivo agora apenas exporta vazio ou foi reduzido)
  - `formatters.ts` (poda de `formatNumeroCsvBR`)
- **Fatiamento de Hotspots (P1):**
  - `dateUtils.ts` fatiado em `frontend/src/utils/date/` (totalmente em português)
  - `apiError.ts` fatiado em `frontend/src/utils/apiError/` (totalmente em português, contratos Axios preservados)
  - `processoService.ts` fatiado em `frontend/src/services/processo/` (removido código morto como `obterProcessoPorCodigo`)
  - `useAcesso.ts` fatiado em `frontend/src/composables/acesso/`
  - `stores/subprocesso.ts` fatiado em `frontend/src/stores/subprocesso/` (separado orquestrador e tipos)
- **Ratchet de Waivers:**
  - removido waiver de `formatters.ts`
  - removido waiver de `diagnosticoService.ts`
  - removido waiver de `dateUtils.ts`
  - removido waiver de `apiError.ts`
  - removido waiver de `processoService.ts`
  - removido waiver de `useAcesso.ts`
  - removido waiver de `stores/subprocesso.ts`

## Guardrails para as proximas rodadas

- manter contratos publicos e `data-testid`
- nao recriar regra de acesso no frontend
- preferir helper local ou componente local antes de abrir nova camada
- apagar sobra liberada na mesma rodada
- usar sempre **português brasileiro** para novo código
- rodar sempre:
  - `node etc/scripts/sgc.js frontend cruft validar`
  - `npx fallow dead-code -r frontend`
  - `npx fallow dupes -r frontend`
  - `npx fallow health -r frontend`

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

1. ~~`dateUtils.ts`~~ (concluído)
2. ~~`apiError.ts`~~ (concluído)
3. ~~`processoService.ts`~~ (concluído)
4. ~~`useAcesso.ts`~~ (concluído)
5. ~~`stores/subprocesso.ts`~~ (concluído)
6. `MapaEdicaoModais.vue` x `MapaFluxoModais.vue` (EM ANDAMENTO)
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
