# Auditoria Arquitetural do Frontend

- Score total: **131** (critico)
- Arquivos de producao: **243**
- Views com vazamento de estrategia de cache: **3**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **16**
- Ocorrencias de `forcar` em producao: **0**

## Hotspots

1. `frontend/src/composables/useInvalidacaoNavegacao.ts` [composable]
   - score: 40
   - sinais: invalidacaoExplicita
2. `frontend/src/composables/useCadastroOrquestracao.ts` [composable]
   - score: 18
   - sinais: invalidacaoExplicita, booleanoPosicional
3. `frontend/src/composables/useCacheSync.ts` [composable]
   - score: 15
   - sinais: invalidacaoExplicita
4. `frontend/src/components/processo/SubprocessoCards.vue` [component]
   - score: 12
   - sinais: booleanoPosicional
5. `frontend/src/composables/useFluxoSubprocesso.ts` [composable]
   - score: 8
   - sinais: booleanoPosicional
6. `frontend/src/stores/unidade.ts` [store]
   - score: 8
   - sinais: booleanoPosicional
7. `frontend/src/components/comum/EditorTextoRico.vue` [component]
   - score: 4
   - sinais: booleanoPosicional
8. `frontend/src/views/subprocessoAcoesAdministrativas.ts` [view]
   - score: 4
   - sinais: booleanoPosicional
9. `frontend/src/components/processo/TabelaProcessos.vue` [component]
   - score: 4
   - sinais: booleanoPosicional
10. `frontend/src/views/subprocessoCarregamento.ts` [view]
   - score: 4
   - sinais: booleanoPosicional

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- contratos de view devem ser orientados a caso de uso;
- evitar `forcar`, `stale`, `snapshot`, `invalidar` e `xxxEmCache` na borda consumida pela view;
- reduzir hubs centrais antes de expandir APIs locais.

