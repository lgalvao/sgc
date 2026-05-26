# Auditoria Arquitetural do Frontend

- Score total: **20** (bom)
- Arquivos de producao: **243**
- Views com vazamento de estrategia de cache: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Ocorrencias de `forcar` em producao: **0**

## Hotspots

1. `frontend/src/composables/useInvalidacaoNavegacao.ts` [composable]
   - score: 20
   - sinais: invalidacaoExplicita

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- contratos de view devem ser orientados a caso de uso;
- evitar `forcar`, `stale`, `snapshot`, `invalidar` e `xxxEmCache` na borda consumida pela view;
- reduzir hubs centrais antes de expandir APIs locais.

