# Auditoria Arquitetural do Frontend

- Score total: **152** (critico)
- Arquivos de producao: **265**
- Views com vazamento de estrategia de cache: **0**
- Views com chamadas diretas a service: **0**
- Views com server state caseiro: **0**
- Views com fan-out arquitetural alto: **0**
- Acessos diretos a cache de store: **0**
- Chamadas com booleano posicional: **1**
- Bolsas de dependencias/estado largas: **1**
- Superficies exportadas amplas: **16**
- Arquivos com mistura de camadas arquiteturais: **0**
- Arquivos com server state caseiro: **0**
- Hubs centrais com sinais: **2**

## Hotspots

1. `frontend/src/composables/useSubprocessoTela.ts` [composable]
   - score: 13
   - sinais: estrategiaCache
   - fan-out: 3 categorias / 7 imports arquiteturais
2. `frontend/src/composables/useInvalidacaoNavegacao.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 7 imports arquiteturais
3. `frontend/src/stores/perfil.ts` [store]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 4 imports arquiteturais
4. `frontend/src/stores/subprocesso/index.ts` [store]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 1 categorias / 3 imports arquiteturais
5. `frontend/src/composables/useCadastroAtividadesMutacoes.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 3 imports arquiteturais
6. `frontend/src/composables/useFluxoMapa.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 3 imports arquiteturais
7. `frontend/src/composables/useFluxoCadastroSubprocesso.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
8. `frontend/src/composables/useDiagnosticoOrganizacionalAlert.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
9. `frontend/src/composables/useMapaSugestoes.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais
10. `frontend/src/composables/useBuscadorUsuarios.ts` [composable]
   - score: 9
   - sinais: superficieAmpla
   - fan-out: 2 categorias / 2 imports arquiteturais

## Diretrizes acompanhadas

- views nao devem conhecer estrategia de cache;
- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;
- contratos de view devem ser orientados a caso de uso;
- evitar bolsas largas de `dependencias` e `estado`;
- reduzir hubs centrais antes de expandir APIs locais.

