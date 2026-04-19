# Plano de melhoria do cache organizacional

## Objetivo

Manter os dados organizacionais vindos das views como snapshots completos em memoria, com uma unica fonte de leitura,
invalidacao previsivel e sem caches redundantes.

## Premissas

Os volumes sao pequenos e cabem confortavelmente em memoria:

1. cerca de 1k usuarios;
2. cerca de 250 unidades;
3. menos de 500 pares perfil-unidade.

Portanto, a meta nao e economizar memoria. A meta e simplicidade operacional: snapshot completo, filtro em memoria,
invalidacao clara e testes de budget protegendo regressao.

## Ja concluido

1. `UnidadeHierarquiaService` monta arvore, mapa pai-filhos, mapa filho-pai e elegibilidade a partir do snapshot de
   unidades.
2. Elegibilidade usa o snapshot de responsabilidades para verificar responsavel efetivo.
3. `ResponsavelUnidadeService.todasPossuemResponsavelEfetivo` usa o snapshot de responsabilidades.
4. `UsuarioPerfilCacheService` monta autorizacoes a partir dos snapshots de perfis e unidades.
5. O cache nao expoe mais entidade JPA para `VW_USUARIO_PERFIL_UNIDADE`; usa `UsuarioPerfilLeitura`.
6. Escritas locais relevantes solicitam invalidacao organizacional:
   - `criarAtribuicaoTemporaria`;
   - `adicionarAdministrador`;
   - `removerAdministrador`;
   - `definirMapaVigente`.
7. A invalidacao organizacional ficou explicita em `CacheOrganizacaoService`, sem evento de backend.
8. `useCacheSync` nao fecha mais o `EventSource` em erro transitorio.
9. `CACHE_USUARIO_PERFIS` foi removido.
10. `CacheAquecimento` e `AgendadorRefreshCache` aquecem apenas snapshots completos; caches derivados sao limpos e
    reconstruidos sob demanda a partir dos snapshots.
11. `ValidadorDadosOrganizacionais` usa snapshots para leituras estruturais de unidades e responsabilidades, mantendo
    SQL direto apenas para diagnosticos que precisam inspecionar inconsistencias das views.
12. Consultas DTO de usuario (`buscarConsultaPorTitulo`, `buscarConsultasPorUnidadeLotacao` e `pesquisarPorNome`)
    filtram o snapshot completo de usuarios; consultas que precisam de entidade JPA inicializada continuam no repo.

## Verificacoes executadas

Comandos executados nesta rodada:

```bash
./gradlew :backend:test --tests sgc.comum.cache.AgendadorRefreshCacheTest --tests sgc.comum.cache.CacheAquecimentoTest --tests sgc.comum.cache.CacheOrganizacaoServiceTest --tests sgc.organizacao.service.CacheViewsOrganizacaoServiceTest --tests sgc.organizacao.service.UsuarioPerfilCacheServiceTest --tests sgc.organizacao.service.UnidadeHierarquiaServiceTest --tests sgc.organizacao.service.ResponsavelUnidadeServiceTest --tests sgc.organizacao.service.UnidadeServiceTest --tests sgc.organizacao.UsuarioServiceTest --tests sgc.organizacao.ValidadorDadosOrganizacionaisTest --tests sgc.organizacao.ValidadorDadosOrganizacionaisExtraCoverageTest --tests sgc.integracao.OrganizacaoViewsQueryBudgetIntegrationTest --tests sgc.integracao.ProcessoSubprocessoViewsQueryBudgetIntegrationTest --tests sgc.e2e.E2eSecurityConfigTest --tests sgc.organizacao.EventosControllerTest
npm run test:unit -- useCacheSync
npm run typecheck
```

Todos passaram. O Gradle foi executado em uma unica invocacao por rodada, sem dois `:backend:test` simultaneos.

## Definicao de pronto

1. Todo cache em `CacheConfig` tem consumidor real.
2. Snapshots organizacionais sao a fonte preferencial das leituras estruturais.
3. Escritas locais invalidam snapshots, derivados e stores frontend por um caminho unico e simples.
4. O frontend permanece inscrito no SSE apos erro transitorio.
5. Testes unitarios, budgets de queries e typecheck passam.
