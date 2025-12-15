## 2025-12-12 - JPQL Projection vs Entity Fetching
**Learning:** Found that `ProcessoService` was fetching full `Processo` and `Unidade` entities just to check for ID existence/conflicts, causing N+1 queries due to lazy loading of `@ManyToMany` collections.
**Action:** Implemented JPQL projection queries (e.g., `SELECT u.codigo FROM ...`) in `ProcessoRepo` to fetch only the required IDs. This eliminates entity hydration and lazy loading, reducing N+1 queries to a single efficient query. I also learned to be careful with Checkstyle line length limits when writing custom queries.

## 2025-12-14 - Tree Rendering Complexity
**Learning:** Found that the recursive tree component `ArvoreUnidades.vue` had an O(N^2) performance bottleneck. The `getEstadoSelecao` function, called for every rendered node (N), was recursively searching the entire tree (O(N)) via `findUnidadeById` to check descendants' eligibility.
**Action:** Refactored the component to pre-calculate a flat `unitMap` (ID -> Object) and `parentMap` in a single pass (O(N)). Updated helper functions to use O(1) map lookups or return objects directly, reducing the complexity of rendering and selection logic to O(N).
