# Auditoria Arquitetural do Frontend

- Score total: **11** (bom)
- Arquivos de producao: **263**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **0**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **0**
- Superficies exportadas amplas: **5**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **0**
- Hubs centrais com sinais: **0**
- Fachadas puras (composables sem lógica): **0**
- Composables minúsculos (< 30L): **2**
- Famílias pulverizadas (>= 4 membros): **4**

## Hotspots

1. `frontend/src/stores/subprocesso/index.ts` [store]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 1 categorias / 3 imports arquiteturais
2. `frontend/src/composables/useRelatorioAndamentoTela.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 2 categorias / 2 imports arquiteturais
3. `frontend/src/composables/useUnidadeAtual.ts` [composable]
   - score: 1
   - sinais: arquivoMinusculo
   - fan-out: 1 categorias / 1 imports arquiteturais

## Famílias de composables pulverizadas

### Fluxo (5 arquivos, 424 linhas)
- `frontend/src/composables/useFluxoAdministrativoSubprocesso.ts`
- `frontend/src/composables/useFluxoCadastroSubprocesso.ts`
- `frontend/src/composables/useFluxoMapa.ts`
- `frontend/src/composables/useFluxoSubprocesso.ts`
- `frontend/src/composables/useFluxoSubprocessoExecucao.ts`

### Mapa (5 arquivos, 953 linhas)
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts`
- `frontend/src/composables/useMapaOrquestracao.ts`
- `frontend/src/composables/useMapaQuery.ts`
- `frontend/src/composables/useMapaSugestoes.ts`
- `frontend/src/composables/useMapaTela.ts`

### Processo (5 arquivos, 582 linhas)
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


## Exceções documentadas

Arquivos com sinais suprimidos via `@sgc-auditoria ignorar:` com motivo explícito:

- `frontend/src/composables/useBuscadorUsuarios.ts`
  - sinais ignorados: superficieAmpla
  - motivo: Autocomplete com navegação por teclado; contrato coeso consumido exclusivamente por BuscadorUsuarios.vue, extraído para testabilidade
- `frontend/src/composables/useDiagnosticoOrganizacionalQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
- `frontend/src/composables/useFeedbacksAdminQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query — pequeno por design
- `frontend/src/composables/useFluxoSubprocesso.ts`
  - sinais ignorados: fachadaPura, arquivoMinusculo
  - motivo: Fachada de coordenação deliberada: injeta `execucao` como dependência em useFluxoCadastroSubprocesso e useFluxoAdministrativoSubprocesso, isolando os consumers desse detalhe
- `frontend/src/composables/useHistoricoQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
- `frontend/src/composables/useMapaSugestoes.ts`
  - sinais ignorados: superficieAmpla
  - motivo: Dois modais relacionados (visualizar + enviar sugestões); contrato coeso consumido integralmente por useMapaTela
- `frontend/src/composables/usePainelQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
- `frontend/src/composables/useProcessoQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
- `frontend/src/composables/useUnidadesQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

