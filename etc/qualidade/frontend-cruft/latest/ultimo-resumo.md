# Auditoria de cruft do frontend

Gerado em: 2026-05-27T23:29:04.451Z
Score total: 438 (critico)

## Resumo

- Arquivos de producao: 268
- Arquivos de teste/story: 211
- any explicito em producao: 1
- checks de null em producao: 18
- fallbacks defensivos em producao: 52
- blocos catch em producao: 61
- casts duplos em producao: 1
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 26

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/composables/useUnidadeQuery.ts | composable | 108 | 20 | exportsSuspeitos: 5 |
| frontend/src/composables/useProcessoCadastroCarga.ts | composable | 123 | 19 | anyExplicito: 1, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/composables/useMapaQuery.ts | composable | 136 | 16 | exportsSuspeitos: 4 |
| frontend/src/composables/usePainelTela.ts | composable | 165 | 14 | fallbacksDefensivos: 4, catchBlocks: 1 |
| frontend/src/composables/useCadastroTela.ts | composable | 386 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/useConfiguracoes.ts | composable | 106 | 11 | fallbacksDefensivos: 1, catchBlocks: 2, exportsSuspeitos: 1 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 52 | 11 | fallbacksDefensivos: 1, exportsSuspeitos: 2 |
| frontend/src/views/HistoricoView.vue | view | 119 | 11 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/views/UnidadesView.vue | view | 234 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/composables/useMapaTela.ts | composable | 431 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/composables/useUnidadeTela.ts | composable | 203 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 165 | 9 | fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/composables/useAtribuicaoTemporariaTela.ts | composable | 416 | 8 | catchBlocks: 4 |
| frontend/src/composables/useProcessoMutacoes.ts | composable | 160 | 8 | catchBlocks: 4 |
| frontend/src/composables/useUnidadesQuery.ts | composable | 23 | 8 | exportsSuspeitos: 2 |
| frontend/src/utils/statusHelpers.ts | utils | 64 | 8 | exportsSuspeitos: 2 |
| frontend/src/services/unidadeService.ts | service | 117 | 7 | fallbacksDefensivos: 1, exportsSuspeitos: 1 |
| frontend/src/components/mapa/CompetenciaCard.vue | component | 212 | 6 | fallbacksDefensivos: 2 |
