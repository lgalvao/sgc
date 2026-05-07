# Auditoria de cruft do frontend

Gerado em: 2026-05-07T17:18:11.158Z
Score total: 2941.5 (critico)

## Resumo

- Arquivos de producao: 220
- Arquivos de teste/story: 226
- any explicito em producao: 0
- checks de null em producao: 26
- fallbacks defensivos em producao: 71
- blocos catch em producao: 68
- casts duplos em producao: 2
- acessos diretos a storage em producao: 1
- exports suspeitos em producao: 11

## Arquivos acima do budget

| Camada | Acima do target | Acima do hard |
|---|---:|---:|
| service | 0 | 0 |
| store | 2 | 2 |
| composable | 8 | 1 |
| view | 8 | 4 |
| component | 15 | 5 |
| router | 0 | 0 |
| utils | 2 | 1 |

## Top hotspots

| Arquivo | Camada | Linhas | Score | Sinais |
|---|---|---:|---:|---|
| frontend/src/constants/textos.ts | outro | 474 | 471 | - |
| frontend/src/views/MapaView.vue | view | 450 | 206 | fallbacksDefensivos: 2 |
| frontend/src/components/feedback/FeedbackModal.vue | component | 370 | 205 | - |
| frontend/src/components/atividades/ImportarAtividadesModal.vue | component | 358 | 194 | fallbacksDefensivos: 1, catchBlocks: 2 |
| frontend/src/components/comum/TreeTable.vue | component | 359 | 188.5 | - |
| frontend/src/views/CadastroView.vue | view | 434 | 182 | fallbacksDefensivos: 2 |
| frontend/src/composables/useFluxoSubprocesso.ts | composable | 251 | 148.5 | catchBlocks: 1 |
| frontend/src/axios-setup.ts | outro | 248 | 147 | checksNull: 1, fallbacksDefensivos: 1, storageDireto: 1 |
| frontend/src/views/ProcessoCadastroView.vue | view | 398 | 143 | fallbacksDefensivos: 3, catchBlocks: 6 |
| frontend/src/components/atividades/AtividadeItem.vue | component | 303 | 104.5 | - |
| frontend/src/views/FeedbacksAdminView.vue | view | 365 | 89.5 | fallbacksDefensivos: 3, catchBlocks: 4 |
| frontend/src/stores/mapas.ts | store | 184 | 84 | checksNull: 2, catchBlocks: 2 |
| frontend/src/utils/treeUtils.ts | utils | 153 | 70.5 | fallbacksDefensivos: 3, exportsSuspeitos: 3 |
| frontend/src/components/layout/MainNavbar.vue | component | 262 | 43 | - |
| frontend/src/stores/perfil.ts | store | 160 | 42 | catchBlocks: 1 |
| frontend/src/views/AtribuicaoTemporariaView.vue | view | 317 | 37.5 | catchBlocks: 2 |
| frontend/src/views/NotificacoesAdminView.vue | view | 312 | 37 | catchBlocks: 3 |
| frontend/src/components/processo/ModalAcaoBloco.vue | component | 234 | 30 | fallbacksDefensivos: 1 |
| frontend/src/App.vue | outro | 178 | 29 | - |
| frontend/src/views/ProcessoDetalheView.vue | view | 210 | 29 | fallbacksDefensivos: 7, catchBlocks: 4 |
