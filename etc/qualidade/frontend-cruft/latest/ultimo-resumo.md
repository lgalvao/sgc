# Auditoria de cruft do frontend

Gerado em: 2026-06-22T23:32:06.933Z
Score total: 626 (critico)

## Resumo

- Arquivos de producao: 290
- Arquivos de teste/story: 236
- any explicito em producao: 0
- checks de null em producao: 45
- fallbacks defensivos em producao: 76
- blocos catch em producao: 92
- casts duplos em producao: 1
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 29

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/views/ConsensoDiagnosticoView.vue | view | 405 | 27 | checksNull: 10, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/useDiagnosticoUnidadeView.ts | view | 441 | 23 | checksNull: 5, fallbacksDefensivos: 1, catchBlocks: 5 |
| frontend/src/components/diagnostico/SubprocessoDiagnosticoPainel.vue | component | 465 | 20 | catchBlocks: 10 |
| frontend/src/composables/useUnidadeQuery.ts | composable | 112 | 20 | exportsSuspeitos: 5 |
| frontend/src/composables/useSituacaoCapacitacaoDiagnostico.ts | composable | 147 | 17 | checksNull: 1, fallbacksDefensivos: 5 |
| frontend/src/views/useAutoavaliacaoDiagnosticoView.ts | view | 299 | 17 | checksNull: 2, fallbacksDefensivos: 3, catchBlocks: 2 |
| frontend/src/composables/useMapaQuery.ts | composable | 138 | 16 | exportsSuspeitos: 4 |
| frontend/src/composables/useMapaTela.ts | composable | 464 | 13 | checksNull: 1, fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/views/RelatorioDiagnosticoGapsView.vue | view | 167 | 13 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 177 | 13 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/composables/useDiagnosticoUnidade.ts | composable | 60 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/usePainelTela.ts | composable | 186 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/SubprocessoView.vue | view | 307 | 12 | fallbacksDefensivos: 2, catchBlocks: 3 |
| frontend/src/composables/useConfiguracoes.ts | composable | 105 | 11 | fallbacksDefensivos: 1, catchBlocks: 2, exportsSuspeitos: 1 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 52 | 11 | fallbacksDefensivos: 1, exportsSuspeitos: 2 |
| frontend/src/views/HistoricoView.vue | view | 118 | 11 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/RelatorioDiagnosticoSituacaoCapacitacaoView.vue | view | 167 | 11 | checksNull: 1, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/views/UnidadesView.vue | view | 234 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/composables/useUnidadeTela.ts | composable | 200 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
