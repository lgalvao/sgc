# Auditoria Arquitetural do Frontend

- Score total: **35** (atencao)
- Arquivos de producao: **265**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **0**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **0**
- Bolsas de dependencias/estado largas: **0**
- Superficies exportadas amplas: **9**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **0**
- Hubs centrais com sinais: **0**

## Hotspots

1. `frontend/src/stores/subprocesso/index.ts` [store]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 1 categorias / 3 imports arquiteturais
2. `frontend/src/composables/useMapaSugestoes.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
3. `frontend/src/composables/useBuscadorUsuarios.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
4. `frontend/src/App.vue` [outro]
   - score: 8
   - sinais: 
   - fan-out: 2 categorias / 4 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

