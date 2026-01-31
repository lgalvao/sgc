## 2025-02-21 - Recursive Hierarchy N+1 Bottleneck
**Learning:** `UnidadeHierarquiaService.buscarIdsDescendentes` fetches ALL units from the DB to build an in-memory tree. `PainelFacade` called this method inside a nested loop for visibility checks (process -> participants -> descendants), causing massive redundant full-table fetches.
**Action:** When implementing hierarchy checks in lists, always pre-fetch the hierarchy once and pass the structure (Map) down to the checking logic. Avoid calling methods that rebuild the tree inside loops.
