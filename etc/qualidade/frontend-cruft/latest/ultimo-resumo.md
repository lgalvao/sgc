# Auditoria de cruft do frontend

Gerado em: 2026-06-06T12:19:31.492Z
Score total: 560 (critico)

## Resumo

- Arquivos de producao: 282
- Arquivos de teste/story: 221
- any explicito em producao: 0
- checks de null em producao: 29
- fallbacks defensivos em producao: 76
- blocos catch em producao: 75
- casts duplos em producao: 1
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 29

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/views/AutoavaliacaoDiagnosticoView.vue | view | 522 | 22 | checksNull: 2, fallbacksDefensivos: 4, catchBlocks: 3 |
| frontend/src/views/DiagnosticoUnidadeView.vue | view | 443 | 21 | checksNull: 6, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/composables/useUnidadeQuery.ts | composable | 112 | 20 | exportsSuspeitos: 5 |
| frontend/src/composables/useMapaQuery.ts | composable | 138 | 16 | exportsSuspeitos: 4 |
| frontend/src/views/ConsensoDiagnosticoView.vue | view | 380 | 15 | checksNull: 2, fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/composables/useCadastroTela.ts | composable | 500 | 14 | fallbacksDefensivos: 4, catchBlocks: 1 |
| frontend/src/composables/usePainelTela.ts | composable | 166 | 14 | fallbacksDefensivos: 4, catchBlocks: 1 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 177 | 13 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/composables/useMonitoramentoDiagnostico.ts | composable | 57 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/useConfiguracoes.ts | composable | 105 | 11 | fallbacksDefensivos: 1, catchBlocks: 2, exportsSuspeitos: 1 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 52 | 11 | fallbacksDefensivos: 1, exportsSuspeitos: 2 |
| frontend/src/views/HistoricoView.vue | view | 118 | 11 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/UnidadesView.vue | view | 234 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/components/diagnostico/DiagnosticoEquipePainel.vue | component | 492 | 10 | catchBlocks: 5 |
| frontend/src/composables/useMapaTela.ts | composable | 431 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/composables/useUnidadeTela.ts | composable | 203 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/OcupacoesCriticasDiagnosticoView.vue | view | 241 | 9 | fallbacksDefensivos: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/composables/useAtribuicaoTemporariaTela.ts | composable | 414 | 8 | catchBlocks: 4 |
