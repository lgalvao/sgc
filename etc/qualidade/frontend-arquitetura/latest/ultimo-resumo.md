# Auditoria Arquitetural do Frontend

- Score total: **1136** (critico)
- Arquivos de producao: **243**
- Views com vazamento de estrategia de cache: **1**
- Views com chamadas diretas a service: **12**
- Views com fan-out arquitetural alto: **10**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **10**
- Superficies exportadas amplas: **27**
- Arquivos com mistura de camadas arquiteturais: **13**
- Hubs centrais com sinais: **4**

## Hotspots

1. `frontend/src/views/ProcessoCadastroView.vue` [view]
   - score: 85
   - sinais: serviceDireto, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 9 imports arquiteturais
2. `frontend/src/composables/useInvalidacaoNavegacao.ts` [composable]
   - score: 79
   - sinais: invalidacaoExplicita, estrategiaCache, invalidacaoArquitetural, fanoutAlto
   - fan-out: 2 categorias / 7 imports arquiteturais
3. `frontend/src/views/AtribuicaoTemporariaView.vue` [view]
   - score: 66
   - sinais: serviceDireto, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 6 imports arquiteturais
4. `frontend/src/stores/perfil.ts` [store]
   - score: 58
   - sinais: serviceDireto, bolsaDependenciasLarga, superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 8 imports arquiteturais
5. `frontend/src/views/processoDetalheAcoes.ts` [view]
   - score: 51
   - sinais: serviceDireto, bolsaDependenciasLarga, superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 5 imports arquiteturais
6. `frontend/src/composables/useFluxoSubprocesso.ts` [composable]
   - score: 47
   - sinais: serviceDireto, estrategiaCache, invalidacaoArquitetural, superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 6 imports arquiteturais
7. `frontend/src/views/MapaView.vue` [view]
   - score: 46
   - sinais: misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 16 imports arquiteturais
8. `frontend/src/views/CadastroView.vue` [view]
   - score: 40
   - sinais: misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 14 imports arquiteturais
9. `frontend/src/composables/useMapas.ts` [composable]
   - score: 39
   - sinais: estrategiaCache, invalidacaoArquitetural, superficieAmpla, acoplamentoStoreAlto
   - fan-out: 2 categorias / 2 imports arquiteturais
10. `frontend/src/views/RelatorioMapasView.vue` [view]
   - score: 38
   - sinais: serviceDireto, misturaCamadas, acoplamentoStoreAlto
   - fan-out: 3 categorias / 4 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

