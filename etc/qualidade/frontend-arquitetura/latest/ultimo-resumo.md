# Auditoria Arquitetural do Frontend

- Score total: **524** (critico)
- Arquivos de producao: **262**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **4**
- Views com server state caseiro: **1**
- Views com fan-out arquitetural alto: **1**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **1**
- Bolsas de dependencias/estado largas: **9**
- Superficies exportadas amplas: **25**
- Arquivos com mistura de camadas arquiteturais: **1**
- Arquivos com server state caseiro: **2**
- Hubs centrais com sinais: **1**

## Hotspots

1. `frontend/src/views/AdministradoresView.vue` [view]
   - score: 30
   - sinais: serviceDireto
   - fan-out: 2 categorias / 4 imports arquiteturais
2. `frontend/src/views/FeedbacksAdminView.vue` [view]
   - score: 28
   - sinais: serviceDireto, serverStateCaseiro
   - fan-out: 2 categorias / 2 imports arquiteturais
3. `frontend/src/views/cadastroDisponibilizacao.ts` [composable]
   - score: 27
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 0 categorias / 0 imports arquiteturais
4. `frontend/src/components/atividades/ImportarAtividadesModal.vue` [component]
   - score: 24
   - sinais: serviceDireto
   - fan-out: 2 categorias / 4 imports arquiteturais
5. `frontend/src/composables/useFluxoSubprocessoExecucao.ts` [composable]
   - score: 22
   - sinais: estrategiaCache, invalidacaoArquitetural
   - fan-out: 2 categorias / 4 imports arquiteturais
6. `frontend/src/composables/useCadastroOrquestracao.ts` [composable]
   - score: 21
   - sinais: invalidacaoExplicita, estrategiaCache, invalidacaoArquitetural
   - fan-out: 2 categorias / 3 imports arquiteturais
7. `frontend/src/views/RelatorioAndamentoView.vue` [view]
   - score: 20
   - sinais: serviceDireto, misturaCamadas
   - fan-out: 3 categorias / 3 imports arquiteturais
8. `frontend/src/composables/usePerfil.ts` [composable]
   - score: 20
   - sinais: superficieAmpla
   - fan-out: 1 categorias / 1 imports arquiteturais
9. `frontend/src/composables/useMapas.ts` [composable]
   - score: 19
   - sinais: invalidacaoExplicita, superficieAmpla
   - fan-out: 1 categorias / 2 imports arquiteturais
10. `frontend/src/stores/perfil.ts` [store]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 2 categorias / 4 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

