# Auditoria de cruft do frontend

Gerado em: 2026-05-14T22:07:23.226Z
Score total: 462 (critico)

## Resumo

- Arquivos de producao: 224
- Arquivos de teste/story: 197
- any explicito em producao: 0
- checks de null em producao: 28
- fallbacks defensivos em producao: 70
- blocos catch em producao: 51
- casts duplos em producao: 2
- acessos diretos a storage em producao: 3
- exports suspeitos em producao: 12

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/views/ProcessoDetalheView.vue | view | 195 | 23 | fallbacksDefensivos: 7, catchBlocks: 1 |
| frontend/src/utils/treeUtils.ts | utils | 153 | 21 | fallbacksDefensivos: 3, exportsSuspeitos: 3 |
| frontend/src/views/ProcessoCadastroView.vue | view | 405 | 21 | fallbacksDefensivos: 3, catchBlocks: 6 |
| frontend/src/composables/useTemaPreferencia.ts | composable | 48 | 20 | storageDireto: 2 |
| frontend/src/axios-setup.ts | outro | 248 | 15 | checksNull: 1, fallbacksDefensivos: 1, storageDireto: 1 |
| frontend/src/composables/useWebStorage.ts | composable | 36 | 14 | checksNull: 2, catchBlocks: 1, castsDuplos: 1 |
| frontend/src/composables/useDiagnosticoOrganizacionalAlert.ts | composable | 126 | 13 | checksNull: 2, fallbacksDefensivos: 3 |
| frontend/src/services/processo/mapeadores.ts | service | 38 | 13 | fallbacksDefensivos: 3, exportsSuspeitos: 1 |
| frontend/src/views/CadastroView.vue | view | 454 | 12 | fallbacksDefensivos: 4 |
| frontend/src/composables/useFeedback.ts | composable | 99 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/views/FeedbacksAdminView.vue | view | 398 | 11 | fallbacksDefensivos: 1, catchBlocks: 4 |
| frontend/src/utils/textoFormatado.ts | utils | 37 | 10 | fallbacksDefensivos: 2, exportsSuspeitos: 1 |
| frontend/src/views/LimpezaProcessosView.vue | view | 128 | 10 | checksNull: 4, catchBlocks: 1 |
| frontend/src/components/processo/SubprocessoResumoHeader.vue | component | 173 | 9 | fallbacksDefensivos: 3 |
| frontend/src/utils/formatters.ts | utils | 79 | 9 | fallbacksDefensivos: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/utils/statusHelpers.ts | utils | 64 | 8 | exportsSuspeitos: 2 |
| frontend/src/views/UnidadeView.vue | view | 321 | 8 | fallbacksDefensivos: 2, catchBlocks: 1 |
| frontend/src/services/unidadeService.ts | service | 113 | 7 | fallbacksDefensivos: 1, exportsSuspeitos: 1 |
| frontend/src/views/processoDetalheAcoes.ts | view | 150 | 7 | fallbacksDefensivos: 1, catchBlocks: 2 |
