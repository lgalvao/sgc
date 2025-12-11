# Bolt's Journal

## 2024-05-22 - Identifying N+1 in UnidadeService
**Learning:** The `UnidadeService` performs an N+1 query when building the unit hierarchy with eligibility checks. It iterates over all units and calls `unidadeMapaRepo.existsById(u.getCodigo())` for each one when `requerMapaVigente` is true. This can be optimized by fetching all valid unit IDs in a single query.
**Action:** Always check loop bodies for repository calls, especially `existsById` or `findById`, and replace them with bulk fetches using `IN` clauses or fetching all IDs into a Set.
