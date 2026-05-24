# Auditoria de cruft do frontend

Gerado em: 2026-05-24T16:03:20.935Z
Score total: 393 (critico)

## Resumo

- Arquivos de producao: 240
- Arquivos de teste/story: 200
- any explicito em producao: 0
- checks de null em producao: 21
- fallbacks defensivos em producao: 61
- blocos catch em producao: 55
- casts duplos em producao: 2
- acessos diretos a storage em producao: 1
- exports suspeitos em producao: 8

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/axios-setup.ts | outro | 273 | 15 | checksNull: 1, fallbacksDefensivos: 1, storageDireto: 1 |
| frontend/src/composables/useWebStorage.ts | composable | 35 | 12 | checksNull: 1, catchBlocks: 1, castsDuplos: 1 |
| frontend/src/views/CadastroView.vue | view | 453 | 12 | fallbacksDefensivos: 4 |
| frontend/src/views/ProcessoCadastroView.vue | view | 441 | 12 | catchBlocks: 6 |
| frontend/src/views/UnidadeView.vue | view | 361 | 12 | fallbacksDefensivos: 2, catchBlocks: 3 |
| frontend/src/views/MapaView.vue | view | 502 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/views/processoDetalheAcoes.ts | view | 173 | 10 | fallbacksDefensivos: 2, catchBlocks: 2 |
| frontend/src/components/processo/SubprocessoResumoHeader.vue | component | 173 | 9 | fallbacksDefensivos: 3 |
| frontend/src/components/unidade/useArvoreSelecao.ts | component | 178 | 9 | fallbacksDefensivos: 3 |
| frontend/src/composables/useDiagnosticoOrganizacionalAlert.ts | composable | 144 | 9 | fallbacksDefensivos: 3 |
| frontend/src/services/processo/mapeadores.ts | service | 38 | 9 | fallbacksDefensivos: 3 |
| frontend/src/utils/formatters.ts | utils | 79 | 9 | fallbacksDefensivos: 3 |
| frontend/src/utils/treeUtils.ts | utils | 115 | 9 | fallbacksDefensivos: 3 |
| frontend/src/views/feedbacksAdminApresentacao.ts | view | 165 | 9 | fallbacksDefensivos: 1, catchBlocks: 3 |
| frontend/src/components/comum/ModalConfirmacao.vue | component | 144 | 8 | castsDuplos: 1 |
| frontend/src/utils/statusHelpers.ts | utils | 64 | 8 | exportsSuspeitos: 2 |
| frontend/src/services/unidadeService.ts | service | 117 | 7 | fallbacksDefensivos: 1, exportsSuspeitos: 1 |
| frontend/src/components/mapa/CompetenciaCard.vue | component | 212 | 6 | fallbacksDefensivos: 2 |
| frontend/src/composables/useProcessoForm.ts | composable | 108 | 6 | fallbacksDefensivos: 2 |
| frontend/src/stores/mapas.ts | store | 256 | 6 | checksNull: 3 |
