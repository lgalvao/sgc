## 2025-12-12 - JPQL Projection vs Entity Fetching
**Learning:** Found that `ProcessoService` was fetching full `Processo` and `Unidade` entities just to check for ID existence/conflicts, causing N+1 queries due to lazy loading of `@ManyToMany` collections.
**Action:** Implemented JPQL projection queries (e.g., `SELECT u.codigo FROM ...`) in `ProcessoRepo` to fetch only the required IDs. This eliminates entity hydration and lazy loading, reducing N+1 queries to a single efficient query. I also learned to be careful with Checkstyle line length limits when writing custom queries.
