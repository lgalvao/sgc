## 2024-05-22 - Optimization of Map Adjustment

**Learning:** `SubprocessoFacade.obterMapaParaAjuste` suffered from potential N+1 or inefficient looping when mapping
Competence-Activity associations because `Competencia.getAtividades()` was accessed inside a loop.
**Action:** Used `CompetenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo` to fetch associations as IDs and
`CompetenciaRepo.findByMapaCodigoSemFetch` to fetch entities without heavy relations. Passed a `Map<Long, Set<Long>>` to
`MapaAjusteMapper` to resolve associations in O(1) without hydrating extra entities.

## 2024-05-23 - Missing Indexes on Alert Filters

**Learning:** `sgc.alerta` was missing indexes on `unidade_destino_codigo` and `processo_codigo`, causing full table
scans for common dashboard queries. `sgc.alerta_usuario` was missing an index on `usuario_titulo`.
**Action:** Always verify `schema.sql` against `JpaRepository` queries (especially `findBy...`) to ensure foreign keys
and filter columns are indexed.

## 2025-02-18 - N+1 on Movimentacao

**Learning:** `MovimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc` was a derived query causing N+1 selects for `Unidade` (origin and destination) because they are EAGER by default.
**Action:** Replaced with `@Query` using `LEFT JOIN FETCH`. Always check derived queries involving entities with EAGER relationships.
