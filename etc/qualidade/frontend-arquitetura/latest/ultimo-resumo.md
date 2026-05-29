# Auditoria Arquitetural do Frontend

- Score total: **0** (excelente)
- Arquivos de producao: **263**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **0**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **0**
- Superficies exportadas amplas: **4**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **0**
- Hubs centrais com sinais: **0**
- Fachadas puras (composables sem lógica): **0**
- Composables minúsculos (< 30L): **0**
- Famílias pulverizadas (>= 4 membros): **4**

## Hotspots

Nenhum hotspot arquitetural detectado.

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
- `frontend/src/composables/useRelatorioAndamentoTela.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: Composable de tela simples — funcionalidade de relatório de andamento não tem complexidade que justifique mais código
- `frontend/src/composables/useUnidadeAtual.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: Abstração deliberada: fornece acesso reativo + setter para unidadeAtualDetalhes; dois consumidores (useBreadcrumbs, useUnidadeTela) tornam o inline inadequado
- `frontend/src/composables/useUnidadesQuery.ts`
  - sinais ignorados: arquivoMinusculo
  - motivo: padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
- `frontend/src/stores/subprocesso/index.ts`
  - sinais ignorados: superficieAmpla
  - motivo: Store dual-context (edicao + cadastro) com três níveis de invalidação (limpar, invalidar, resetar); 14 exports são inerentes ao contrato mínimo de dois contextos independentes

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

