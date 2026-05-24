# Auditoria de cruft do frontend

Gerado em: 2026-05-24T15:15:22.666Z
Score total: 495 (critico)

## Resumo

- Arquivos de producao: 240
- Arquivos de teste/story: 200
- any explicito em producao: 0
- checks de null em producao: 29
- fallbacks defensivos em producao: 75
- blocos catch em producao: 55
- casts duplos em producao: 2
- acessos diretos a storage em producao: 3
- exports suspeitos em producao: 14

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/views/ProcessoDetalheView.vue | view | 195 | 23 | fallbacksDefensivos: 7, catchBlocks: 1 |
| frontend/src/utils/treeUtils.ts | utils | 153 | 21 | fallbacksDefensivos: 3, exportsSuspeitos: 3 |
| frontend/src/composables/useTemaPreferencia.ts | composable | 48 | 20 | storageDireto: 2 |
| frontend/src/axios-setup.ts | outro | 278 | 15 | checksNull: 1, fallbacksDefensivos: 1, storageDireto: 1 |
| frontend/src/views/ProcessoCadastroView.vue | view | 470 | 15 | fallbacksDefensivos: 1, catchBlocks: 6 |
| frontend/src/composables/useWebStorage.ts | composable | 36 | 14 | checksNull: 2, catchBlocks: 1, castsDuplos: 1 |
| frontend/src/utils/textoFormatado.ts | utils | 37 | 14 | fallbacksDefensivos: 2, exportsSuspeitos: 2 |
| frontend/src/composables/useDiagnosticoOrganizacionalAlert.ts | composable | 134 | 13 | checksNull: 2, fallbacksDefensivos: 3 |
| frontend/src/services/processo/mapeadores.ts | service | 38 | 13 | fallbacksDefensivos: 3, exportsSuspeitos: 1 |
| frontend/src/views/CadastroView.vue | view | 453 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/UnidadeView.vue | view | 361 | 12 | fallbacksDefensivos: 2, catchBlocks: 3 |
| frontend/src/composables/useFeedback.ts | composable | 110 | 11 | fallbacksDefensivos: 3, catchBlocks: 1 |
| frontend/src/views/RelatorioUnidadesSemMapasVigentesView.vue | view | 235 | 11 | checksNull: 1, fallbacksDefensivos: 3 |
| frontend/src/views/LimpezaProcessosView.vue | view | 128 | 10 | checksNull: 4, catchBlocks: 1 |
| frontend/src/views/MapaView.vue | view | 502 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/views/processoDetalheAcoes.ts | view | 173 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/processo/SubprocessoResumoHeader.vue | component | 173 | 9 | fallbacksDefensivos: 3 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/utils/formatters.ts | utils | 79 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 165 | 9 | fallbacksDefensivos: 1, catchBlocks: 3 |
