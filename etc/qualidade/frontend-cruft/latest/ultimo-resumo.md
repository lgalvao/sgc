# Auditoria de cruft do frontend

Gerado em: 2026-06-08T03:01:06.876Z
Score total: 556 (critico)

## Resumo

- Arquivos de producao: 285
- Arquivos de teste/story: 233
- any explicito em producao: 0
- checks de null em producao: 31
- fallbacks defensivos em producao: 76
- blocos catch em producao: 71
- casts duplos em producao: 1
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 29

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/composables/useUnidadeQuery.ts | composable | 112 | 20 | exportsSuspeitos: 5 |
| frontend/src/views/useAutoavaliacaoDiagnosticoView.ts | view | 317 | 18 | checksNull: 2, fallbacksDefensivos: 4, catchBlocks: 1 |
| frontend/src/views/useDiagnosticoUnidadeView.ts | view | 301 | 17 | checksNull: 6, fallbacksDefensivos: 1, catchBlocks: 1 |
| frontend/src/composables/useMapaQuery.ts | composable | 138 | 16 | exportsSuspeitos: 4 |
| frontend/src/views/ConsensoDiagnosticoView.vue | view | 368 | 15 | checksNull: 2, fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/composables/useMapaTela.ts | composable | 457 | 13 | checksNull: 1, fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 177 | 13 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/composables/useMonitoramentoDiagnostico.ts | composable | 57 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/usePainelTela.ts | composable | 167 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/useConfiguracoes.ts | composable | 105 | 11 | fallbacksDefensivos: 1, catchBlocks: 2, exportsSuspeitos: 1 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 52 | 11 | fallbacksDefensivos: 1, exportsSuspeitos: 2 |
| frontend/src/views/HistoricoView.vue | view | 118 | 11 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/UnidadesView.vue | view | 234 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/components/diagnostico/DiagnosticoEquipePainel.vue | component | 492 | 10 | catchBlocks: 5 |
| frontend/src/composables/useUnidadeTela.ts | composable | 203 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/OcupacoesCriticasDiagnosticoView.vue | view | 231 | 9 | fallbacksDefensivos: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/composables/useAtribuicaoTemporariaTela.ts | composable | 414 | 8 | catchBlocks: 4 |
| frontend/src/composables/useCadastroTela.ts | composable | 480 | 8 | fallbacksDefensivos: 2, catchBlocks: 1 |
