# Auditoria de cruft do frontend

Gerado em: 2026-05-26T00:22:07.512Z
Score total: 348 (critico)

## Resumo

- Arquivos de producao: 243
- Arquivos de teste/story: 211
- any explicito em producao: 0
- checks de null em producao: 21
- fallbacks defensivos em producao: 48
- blocos catch em producao: 53
- casts duplos em producao: 1
- acessos diretos a storage em producao: 0
- exports suspeitos em producao: 12

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/views/CadastroView.vue | view | 455 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/PainelView.vue | view | 222 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/ProcessoCadastroView.vue | view | 444 | 12 | catchBlocks: 6 |
| frontend/src/views/UnidadeView.vue | view | 359 | 12 | fallbacksDefensivos: 2, catchBlocks: 3 |
| frontend/src/views/MapaView.vue | view | 499 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/HistoricoView.vue | view | 115 | 9 | checksNull: 2, fallbacksDefensivos: 1, catchBlocks: 1 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 165 | 9 | fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/utils/statusHelpers.ts | utils | 64 | 8 | exportsSuspeitos: 2 |
| frontend/src/composables/useNotificacoesAdminQuery.ts | composable | 21 | 7 | fallbacksDefensivos: 1, exportsSuspeitos: 1 |
| frontend/src/services/unidadeService.ts | service | 117 | 7 | fallbacksDefensivos: 1, exportsSuspeitos: 1 |
| frontend/src/components/mapa/CompetenciaCard.vue | component | 212 | 6 | fallbacksDefensivos: 2 |
| frontend/src/composables/useProcessoForm.ts | composable | 108 | 6 | fallbacksDefensivos: 2 |
| frontend/src/stores/mapas.ts | store | 256 | 6 | checksNull: 3 |
| frontend/src/stores/subprocesso/index.ts | store | 108 | 6 | checksNull: 3 |
| frontend/src/utils/date/parsing.ts | utils | 71 | 6 | checksNull: 2, catchBlocks: 1 |
| frontend/src/utils/textoFormatado.ts | utils | 30 | 6 | fallbacksDefensivos: 2 |
| frontend/src/views/AdministradoresView.vue | view | 251 | 6 | checksNull: 1, catchBlocks: 2 |
| frontend/src/views/AtribuicaoTemporariaView.vue | view | 428 | 6 | catchBlocks: 3 |
