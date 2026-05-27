# Auditoria Arquitetural do Frontend

- Score total: **1035** (critico)
- Arquivos de producao: **262**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **6**
- Views com server state caseiro: **1**
- Views com fan-out arquitetural alto: **3**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **1**
- Bolsas de dependencias/estado largas: **9**
- Superficies exportadas amplas: **32**
- Arquivos com mistura de camadas arquiteturais: **10**
- Arquivos com server state caseiro: **2**
- Hubs centrais com sinais: **2**

## Hotspots

1. `frontend/src/composables/useInvalidacaoNavegacao.ts` [composable]
   - score: 230
   - sinais: invalidacaoExplicita, estrategiaCache, invalidacaoArquitetural, fanoutAlto, acoplamentoStoreAlto
   - fan-out: 2 categorias / 7 imports arquiteturais
2. `frontend/src/composables/useMapaTela.ts` [composable]
   - score: 55
   - sinais: superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 16 imports arquiteturais
3. `frontend/src/composables/useCadastroTela.ts` [composable]
   - score: 49
   - sinais: superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 14 imports arquiteturais
4. `frontend/src/composables/useSubprocessoTela.ts` [composable]
   - score: 41
   - sinais: estrategiaCache, superficieAmpla, misturaCamadas, fanoutAlto, acoplamentoStoreAlto
   - fan-out: 3 categorias / 7 imports arquiteturais
5. `frontend/src/views/AdministradoresView.vue` [view]
   - score: 30
   - sinais: serviceDireto
   - fan-out: 2 categorias / 4 imports arquiteturais
6. `frontend/src/views/useAcoesBlocoProcesso.ts` [view]
   - score: 29
   - sinais: serviceDireto, superficieAmpla, misturaCamadas
   - fan-out: 3 categorias / 3 imports arquiteturais
7. `frontend/src/views/FeedbacksAdminView.vue` [view]
   - score: 28
   - sinais: serviceDireto, serverStateCaseiro
   - fan-out: 2 categorias / 2 imports arquiteturais
8. `frontend/src/views/cadastroDisponibilizacao.ts` [view]
   - score: 27
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 0 categorias / 0 imports arquiteturais
9. `frontend/src/composables/useAtribuicaoTemporariaTela.ts` [composable]
   - score: 25
   - sinais: serviceDireto, superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 6 imports arquiteturais
10. `frontend/src/composables/useUnidadeTela.ts` [composable]
   - score: 25
   - sinais: superficieAmpla, misturaCamadas, fanoutAlto
   - fan-out: 3 categorias / 6 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

