# Auditoria Arquitetural do Frontend

- Score total: **1225** (critico)
- Arquivos de producao: **258**
- Views com vazamento de estrategia de cache: **1**
- Views com chamadas diretas a service: **11**
- Views com server state caseiro: **2**
- Views com fan-out arquitetural alto: **9**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **10**
- Superficies exportadas amplas: **29**
- Arquivos com mistura de camadas arquiteturais: **12**
- Arquivos com server state caseiro: **5**
- Hubs centrais com sinais: **4**

## Hotspots

1. `frontend/src/composables/useEstadoOrganizacaoNavegacao.ts` [composable]
   - score: 70
   - sinais: invalidacaoExplicita, estrategiaCache, invalidacaoArquitetural
   - fan-out: 1 categorias / 2 imports arquiteturais
2. `frontend/src/stores/unidade.ts` [store]
   - score: 49
   - sinais: serviceDireto, superficieAmpla, serverStateCaseiro
   - fan-out: 1 categorias / 1 imports arquiteturais
3. `frontend/src/views/MapaView.vue` [view]
   - score: 46
   - sinais: misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 16 imports arquiteturais
4. `frontend/src/stores/mapas.ts` [store]
   - score: 46
   - sinais: serviceDireto, bolsaDependenciasLarga, superficieAmpla, serverStateCaseiro
   - fan-out: 1 categorias / 1 imports arquiteturais
5. `frontend/src/composables/useProcessoCadastroTela.ts` [composable]
   - score: 43
   - sinais: serviceDireto, superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 9 imports arquiteturais
6. `frontend/src/views/CadastroView.vue` [view]
   - score: 40
   - sinais: misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 14 imports arquiteturais
7. `frontend/src/composables/useMapas.ts` [composable]
   - score: 39
   - sinais: estrategiaCache, invalidacaoArquitetural, superficieAmpla, acoplamentoStoreAlto
   - fan-out: 2 categorias / 2 imports arquiteturais
8. `frontend/src/views/RelatorioMapasView.vue` [view]
   - score: 38
   - sinais: serviceDireto, misturaCamadas, acoplamentoStoreAlto
   - fan-out: 3 categorias / 4 imports arquiteturais
9. `frontend/src/views/NotificacoesAdminView.vue` [view]
   - score: 38
   - sinais: serviceDireto, serverStateCaseiro
   - fan-out: 2 categorias / 3 imports arquiteturais
10. `frontend/src/composables/useEstadoMapaNavegacao.ts` [composable]
   - score: 38
   - sinais: invalidacaoExplicita, estrategiaCache, invalidacaoArquitetural
   - fan-out: 1 categorias / 1 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

