# Auditoria de cruft do frontend

Gerado em: 2026-06-25T00:05:29.159Z
Score total: 624 (critico)

## Resumo

- Arquivos de producao: 299
- Arquivos de teste/story: 239
- any explicito em producao: 0
- checks de null em producao: 45
- fallbacks defensivos em producao: 78
- blocos catch em producao: 92
- casts duplos em producao: 0
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 29

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/components/diagnostico/SubprocessoDiagnosticoPainel.vue | component | 457 | 20 | catchBlocks: 10 |
| frontend/src/composables/useUnidadeQuery.ts | composable | 112 | 20 | exportsSuspeitos: 5 |
| frontend/src/views/useDiagnosticoUnidadeView.ts | view | 443 | 20 | checksNull: 5, catchBlocks: 5 |
| frontend/src/views/ConsensoDiagnosticoView.vue | view | 210 | 18 | checksNull: 4, fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/composables/useSituacaoCapacitacaoDiagnostico.ts | composable | 147 | 17 | checksNull: 1, fallbacksDefensivos: 5 |
| frontend/src/views/useAutoavaliacaoDiagnosticoView.ts | view | 299 | 17 | checksNull: 2, fallbacksDefensivos: 3, catchBlocks: 2 |
| frontend/src/composables/useMapaQuery.ts | composable | 138 | 16 | exportsSuspeitos: 4 |
| frontend/src/composables/useMapaTela.ts | composable | 464 | 13 | checksNull: 1, fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 177 | 13 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/components/diagnostico/ConsensoDiagnosticoTabela.vue | component | 235 | 12 | checksNull: 6 |
| frontend/src/composables/useDiagnosticoUnidade.ts | composable | 60 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/usePainelTela.ts | composable | 186 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/SubprocessoView.vue | view | 291 | 12 | fallbacksDefensivos: 2, catchBlocks: 3 |
| frontend/src/composables/useConfiguracoes.ts | composable | 105 | 11 | fallbacksDefensivos: 1, catchBlocks: 2, exportsSuspeitos: 1 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 52 | 11 | fallbacksDefensivos: 1, exportsSuspeitos: 2 |
| frontend/src/views/HistoricoView.vue | view | 118 | 11 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/RelatorioDiagnosticoGapsView.vue | view | 144 | 11 | checksNull: 1, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/views/RelatorioDiagnosticoSituacaoCapacitacaoView.vue | view | 167 | 11 | checksNull: 1, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/views/UnidadesView.vue | view | 234 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/composables/useUnidadeTela.ts | composable | 200 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
