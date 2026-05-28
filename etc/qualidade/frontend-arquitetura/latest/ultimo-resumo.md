# Auditoria Arquitetural do Frontend

- Score total: **348** (critico)
- Arquivos de producao: **264**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **1**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **1**
- Bolsas de dependencias/estado largas: **8**
- Superficies exportadas amplas: **23**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **1**
- Hubs centrais com sinais: **2**

## Hotspots

1. `frontend/src/composables/useMapas.ts` [composable]
   - score: 19
   - sinais: invalidacaoExplicita, superficieAmpla
   - fan-out: 1 categorias / 2 imports arquiteturais
2. `frontend/src/stores/perfil.ts` [store]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 2 categorias / 4 imports arquiteturais
3. `frontend/src/views/subprocessoAcoesAdministrativas.ts` [composable]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 2 categorias / 3 imports arquiteturais
4. `frontend/src/views/mapaAnaliseFluxo.ts` [composable]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 1 categorias / 1 imports arquiteturais
5. `frontend/src/stores/organizacao.ts` [store]
   - score: 18
   - sinais: serviceDireto, serverStateCaseiro
   - fan-out: 1 categorias / 1 imports arquiteturais
6. `frontend/src/composables/acesso/cadastro.ts` [composable]
   - score: 18
   - sinais: superficieAmpla
   - fan-out: 1 categorias / 1 imports arquiteturais
7. `frontend/src/views/cadastroAnaliseFluxo.ts` [composable]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 0 categorias / 0 imports arquiteturais
8. `frontend/src/views/mapaDisponibilizacao.ts` [composable]
   - score: 18
   - sinais: bolsaDependenciasLarga, superficieAmpla
   - fan-out: 0 categorias / 0 imports arquiteturais
9. `frontend/src/composables/useSubprocessoTela.ts` [composable]
   - score: 13
   - sinais: estrategiaCache
   - fan-out: 3 categorias / 7 imports arquiteturais
10. `frontend/src/stores/perfilAutenticacao.ts` [store]
   - score: 12
   - sinais: serviceDireto
   - fan-out: 1 categorias / 1 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

