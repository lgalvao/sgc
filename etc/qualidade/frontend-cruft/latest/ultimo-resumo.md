# Auditoria de cruft do frontend

Gerado em: 2026-05-04T01:03:00.023Z
Score total: 2324 (critico)

## Resumo

- Arquivos de producao: 215
- Arquivos de teste/story: 195
- any explicito em producao: 0
- checks de null em producao: 23
- fallbacks defensivos em producao: 66
- blocos catch em producao: 63
- casts duplos em producao: 2
- acessos diretos a storage em producao: 1
- exports suspeitos em producao: 10

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|
| service | 0 | 0 |
| store | 2 | 2 |
| composable | 6 | 1 |
| view | 5 | 3 |
| component | 10 | 3 |
| router | 0 | 0 |
| utils | 1 | 1 |

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/constants/textos.ts | outro | 452 | 438 | - |
| frontend/src/views/MapaView.vue | view | 442 | 194 | fallbacksDefensivos: 2 |
| frontend/src/components/comum/TreeTable.vue | component | 360 | 190 | - |
| frontend/src/views/CadastroView.vue | view | 423 | 165.5 | fallbacksDefensivos: 2 |
| frontend/src/axios-setup.ts | outro | 248 | 147 | checksNull: 1, fallbacksDefensivos: 1, storageDireto: 1 |
| frontend/src/views/ProcessoCadastroView.vue | view | 395 | 141.5 | fallbacksDefensivos: 4, catchBlocks: 6 |
| frontend/src/components/atividades/ImportarAtividadesModal.vue | component | 321 | 138.5 | fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/components/atividades/AtividadeItem.vue | component | 303 | 104.5 | - |
| frontend/src/composables/useFluxoSubprocesso.ts | composable | 221 | 103.5 | catchBlocks: 1 |
| frontend/src/stores/mapas.ts | store | 184 | 84 | checksNull: 2, catchBlocks: 2 |
| frontend/src/utils/treeUtils.ts | utils | 153 | 70.5 | fallbacksDefensivos: 3, exportsSuspeitos: 3 |
| frontend/src/stores/perfil.ts | store | 155 | 34.5 | catchBlocks: 1 |
| frontend/src/components/processo/ModalAcaoBloco.vue | component | 234 | 30 | fallbacksDefensivos: 1 |
| frontend/src/views/ProcessoDetalheView.vue | view | 210 | 29 | fallbacksDefensivos: 7, catchBlocks: 4 |
| frontend/src/composables/useMapaCompetenciasMutacoes.ts | composable | 154 | 23 | catchBlocks: 3 |
| frontend/src/App.vue | outro | 165 | 22.5 | - |
| frontend/src/views/AtribuicaoTemporariaView.vue | view | 283 | 20.5 | catchBlocks: 2 |
| frontend/src/components/processo/SubprocessoCards.vue | component | 216 | 18 | - |
| frontend/src/components/unidade/UnidadeTreeNode.vue | component | 203 | 14.5 | fallbacksDefensivos: 1 |
| frontend/src/views/NotificacoesAdminView.vue | view | 271 | 14.5 | catchBlocks: 2 |
