# Auditoria Arquitetural do Frontend

- Score total: **47** (atencao)
- Arquivos de producao: **265**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **0**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **0**
- Superficies exportadas amplas: **9**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **0**
- Hubs centrais com sinais: **0**
- Fachadas puras (composables sem lógica): **1**
- Composables minúsculos (< 30L): **9**
- Famílias pulverizadas (>= 4 membros): **4**

## Hotspots

1. `frontend/src/stores/subprocesso/index.ts` [store]
   - score: 9
   - sinais: palavraForcar, superficieAmpla
   - fan-out: 1 categorias / 3 imports arquiteturais
2. `frontend/src/composables/useMapaSugestoes.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
3. `frontend/src/composables/useBuscadorUsuarios.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
4. `frontend/src/App.vue` [outro]
   - score: 8
   - sinais: 
   - fan-out: 2 categorias / 4 imports arquiteturais
5. `frontend/src/composables/useFluxoSubprocesso.ts` [composable]
   - score: 3
   - sinais: fachadaPura
   - fan-out: 1 categorias / 3 imports arquiteturais
6. `frontend/src/composables/useDiagnosticoOrganizacionalQuery.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais
7. `frontend/src/composables/useProcessoQuery.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais
8. `frontend/src/composables/useHistoricoQuery.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais
9. `frontend/src/composables/useUnidadesQuery.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais
10. `frontend/src/composables/useRelatorioAndamentoTela.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais

## Famílias de composables pulverizadas

### Fluxo (5 arquivos, 423 linhas)
- `frontend/src/composables/useFluxoAdministrativoSubprocesso.ts`
- `frontend/src/composables/useFluxoCadastroSubprocesso.ts`
- `frontend/src/composables/useFluxoMapa.ts`
- `frontend/src/composables/useFluxoSubprocesso.ts`
- `frontend/src/composables/useFluxoSubprocessoExecucao.ts`

### Mapa (5 arquivos, 952 linhas)
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts`
- `frontend/src/composables/useMapaOrquestracao.ts`
- `frontend/src/composables/useMapaQuery.ts`
- `frontend/src/composables/useMapaSugestoes.ts`
- `frontend/src/composables/useMapaTela.ts`

### Processo (5 arquivos, 581 linhas)
- `frontend/src/composables/useProcessoCadastroCarga.ts`
- `frontend/src/composables/useProcessoCadastroTela.ts`
- `frontend/src/composables/useProcessoForm.ts`
- `frontend/src/composables/useProcessoMutacoes.ts`
- `frontend/src/composables/useProcessoQuery.ts`

### Cadastro (4 arquivos, 909 linhas)
- `frontend/src/composables/useCadastroAtividadesMutacoes.ts`
- `frontend/src/composables/useCadastroOrquestracao.ts`
- `frontend/src/composables/useCadastroRevisaoSemMudancas.ts`
- `frontend/src/composables/useCadastroTela.ts`


## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

