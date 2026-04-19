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

## Ainda falta fazer

## 1. Revisar `ValidadorDadosOrganizacionais`

`ValidadorDadosOrganizacionais` ainda consulta `UnidadeRepo` e `ResponsabilidadeRepo` diretamente. Parte disso e
aceitavel, porque o diagnostico precisa detectar inconsistencias e duplicidades das views. Mesmo assim, vale separar:

1. leituras estruturais que podem vir do snapshot;
2. SQL diagnostico que precisa continuar direto na view para detectar duplicidade/inconsistencia.

## 2. Revisar consultas de usuario

`UsuarioService` ainda mantem buscas diretas por titulo, lotacao e pesquisa textual. Isso pode ser correto, mas deve
ficar explicito:

1. pesquisa textual deve continuar no banco para preservar limite, ordenacao e semantica;
2. consultas estruturais simples podem ser avaliadas contra o snapshot se isso simplificar o codigo;
3. nao trocar consulta por filtro em memoria quando o resultado precisa de entidade JPA inicializada.

## 3. Fechar verificacoes

Antes de encerrar a frente:

```bash
./gradlew :backend:test --tests sgc.comum.cache.AgendadorRefreshCacheTest
./gradlew :backend:test --tests sgc.comum.cache.CacheAquecimentoTest
./gradlew :backend:test --tests sgc.comum.cache.CacheOrganizacaoServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.CacheViewsOrganizacaoServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.UsuarioPerfilCacheServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.UnidadeHierarquiaServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.ResponsavelUnidadeServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.UnidadeServiceTest
./gradlew :backend:test --tests sgc.organizacao.UsuarioServiceTest
./gradlew :backend:test --tests sgc.integracao.OrganizacaoViewsQueryBudgetIntegrationTest
./gradlew :backend:test --tests sgc.integracao.ProcessoSubprocessoViewsQueryBudgetIntegrationTest
npm run test:unit -- useCacheSync
npm run typecheck
```

Rodar comandos Gradle sequencialmente. Nao executar dois `:backend:test` ao mesmo tempo, porque ambos escrevem em
`backend/build/test-results`.

## Definicao de pronto

1. Todo cache em `CacheConfig` tem consumidor real.
2. Snapshots organizacionais sao a fonte preferencial das leituras estruturais.
3. Escritas locais invalidam snapshots, derivados e stores frontend por um caminho unico e simples.
4. O frontend permanece inscrito no SSE apos erro transitorio.
5. Testes unitarios, budgets de queries e typecheck passam.
