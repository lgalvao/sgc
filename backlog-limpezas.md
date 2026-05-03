# Backlog de limpezas do frontend

## Estado atual

- Gate de cruft: **ok**
- Score atual: **3022**
- Violacoes: **0**
- Avisos: **0**
- Baseline atual do `fallow`:
  - `dead-code`: **0 issues**
  - duplicacao: **6 clone groups**, **153 linhas**, **0.3%**
  - health score: **98 / A**
  - dead files: **0.0%**
  - dead exports: **0.0%**

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
  - `frontend/src/constants/index.ts`
  - `frontend/src/constants/situacoes.ts`
  - `frontend/src/utils/styleUtils.ts`
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
- limpeza de exports pequenos:
  - `frontend/src/composables/useWebStorage.ts`
  - `frontend/src/utils/index.ts`
  - `frontend/src/services/processo/types.ts`
  - `frontend/src/utils/date/validation.ts`

### Fatiamentos estruturais ja feitos

- `dateUtils.ts` fatiado em `frontend/src/utils/date/`
- `apiError.ts` fatiado em `frontend/src/utils/apiError/`
- `processoService.ts` fatiado em `frontend/src/services/processo/`
- `useAcesso.ts` fatiado em `frontend/src/composables/acesso/`
- `stores/subprocesso.ts` fatiado em `frontend/src/stores/subprocesso/`

### Rodada ampla encerrada agora

- limpeza de APIs orfas em services:
  - `frontend/src/services/analiseService.ts`
  - `frontend/src/services/atividadeService.ts`
  - `frontend/src/services/atribuicaoTemporariaService.ts`
  - `frontend/src/services/painelService.ts`
  - `frontend/src/services/subprocessoServiceContexto.ts`
  - `frontend/src/services/subprocessoServiceMapa.ts`
  - `frontend/src/services/subprocessoServiceBase.ts`
- poda das suites herdadas que so sustentavam essa superficie morta:
  - `frontend/src/services/__tests__/analiseService.spec.ts`
  - `frontend/src/services/__tests__/atividadeService.spec.ts`
  - `frontend/src/services/__tests__/atribuicaoTemporariaService.spec.ts`
  - `frontend/src/services/__tests__/painelService.spec.ts`
  - `frontend/src/services/__tests__/subprocessoService.spec.ts`
  - `frontend/src/__tests__/views/PainelView.spec.ts`
  - `frontend/src/__tests__/views/PainelViewCoverage.spec.ts`
  - `frontend/src/views/__tests__/AtividadesCadastroView.spec.ts`
  - `frontend/src/components/__tests__/InputData.spec.ts`
- remocao de tipos mortos:
  - `frontend/src/types/dtos.ts`
  - `frontend/src/types/mapa-modelos.ts`
  - `frontend/src/types/organizacao.ts`
- remocao do waiver obsoleto de `frontend/src/types/dtos.ts`
- baseline do `fallow dead-code` zerado

### Waivers removidos nesta campanha

- `frontend/src/utils/dateUtils.ts`
- `frontend/src/utils/apiError.ts`
- `frontend/src/services/processoService.ts`
- `frontend/src/composables/useAcesso.ts`
- `frontend/src/stores/subprocesso.ts`
- `frontend/src/components/comum/BuscadorUsuarios.vue`
- `frontend/src/components/mapa/CriarCompetenciaModal.vue`
- `frontend/src/types/dtos.ts`

## Em andamento agora

Esses pontos seguem como fronteira viva no worktree e nao devem ser atacados no escuro:

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

## Prioridade 0 - fechar a rodada viva do mapa

### Modais do mapa

- consolidar a fronteira `frontend/src/components/mapa/modais/`
- decidir se `CompetenciaAtividadeItem.vue` fica como extracao legitima ou sobra temporaria
- reduzir acoplamento entre:
  - `CompetenciaEdicaoModal.vue`
  - `MapaModaisRoot.vue`
  - `useCadastroAtividadesMutacoes.ts`
- apagar compatibilidades e sobras assim que a consolidacao estabilizar

## Prioridade 1 - hotspots fortes confirmados pelo `fallow health`

### 1. `frontend/src/utils/apiError/normalizer.ts`

- `normalizarErro` segue como hotspot mais forte
- melhor corte:
  - classificacao
  - leitura de payload
  - normalizacao final

### 2. `frontend/src/utils/date/parsing.ts`

- `analisarData` e `analisarStringData` continuam quentes
- fronteira boa para separar parse, validacao e heuristicas

### 3. `frontend/src/views/ProcessoCadastroView.vue`

- `handleApiErrors` segue pesado
- ainda conversa com duplicacoes e fluxo de cadastro

### 4. `frontend/src/axios-setup.ts`

- `handleResponseError` continua grande
- alvo bom para cortar sem mudar contrato HTTP

### 5. `frontend/src/views/cadastroDisponibilizacao.ts`

- `disponibilizarCadastro` ainda concentra pre-validacao, execucao e tratamento

### 6. `frontend/src/components/processo/ProcessoFormFields.vue`

- `focarPrimeiroErro` apareceu como hotspot critico
- corte pequeno pode melhorar bastante sem espalhar regra

### 7. `frontend/src/views/LoginView.vue`

- `performInitialLogin` entrou no topo do health
- vale revisar depois dos hotspots acima

## Prioridade 2 - duplicacoes reais ainda abertas

Atacar so quando houver corte pequeno e contrato claro.

### Boas candidatas

- `frontend/src/components/mapa/modais/MapaDevolucaoModal.vue`
  x `frontend/src/components/mapa/modais/MapaSugestoesEnvioModal.vue`
- `frontend/src/services/relatoriosService.ts`
  - dois blocos repetidos internos
- `frontend/src/services/processo/types.ts`
  x `frontend/src/types/processo.ts`

### Candidatas com cuidado

- `frontend/src/views/ProcessoCadastroView.vue`
  x `frontend/src/views/UnidadesView.vue`
- `frontend/src/components/unidade/ArvoreUnidades.vue`
  x `frontend/src/views/ProcessoCadastroView.vue`
- `frontend/src/components/cadastro/CadastroObservacaoModal.vue`
  x `frontend/src/components/cadastro/cadastroObservacaoModalModel.ts`
  - duplicacao pequena; so mexer se cair junto com outra rodada

## Prioridade 3 - waivers e hotspots estruturais ainda vivos

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
- `frontend/src/constants/textos.ts`
- `frontend/src/utils/treeUtils.ts`
- `frontend/src/App.vue`

## Nao e mais prioridade

Esses itens saem do backlog ativo porque ja foram resolvidos nesta campanha:

- dead files restantes em `constants/` e `styleUtils.ts`
- exports mortos pequenos de `useWebStorage`, `date/validation`, `utils/index`, `services/processo/types`
- auditoria de services orfos (`analiseService`, `atividadeService`, `atribuicaoTemporariaService`, `painelService`, `subprocessoServiceContexto`, `subprocessoServiceMapa`)
- limpeza de tipos mortos em `types/dtos.ts`, `types/mapa-modelos.ts`, `types/organizacao.ts`

## Ordem recomendada de execucao

1. fechar a rodada viva dos modais do mapa
2. podar `apiError/normalizer.ts`
3. podar `date/parsing.ts`
4. atacar `ProcessoCadastroView.vue`
5. atacar `axios-setup.ts`
6. atacar `cadastroDisponibilizacao.ts`
7. resolver duplicacoes pequenas de `relatoriosService.ts` e `services/processo/types.ts`
8. voltar aos waivers grandes ainda ativos

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

- a campanha esta indo **muito bem**
- a rodada ampla acabou de remover o grosso das APIs orfas e dos tipos mortos
- o `fallow dead-code` ficou **zerado**
- o backlog agora sai do modo "apagar sobra obvia" e entra em modo "fechar fronteiras vivas e hotspots reais"
- o proximo passo natural e **fechar a rodada dos modais do mapa** sem conflitar com o trabalho vivo nessa area
