## 2024-05-22 - H2 Schema Mismatch in Tests
**Learning:** The project uses `schema.sql` for H2 tests with `ddl-auto: none`, but the schema definition was out of sync with JPA Entities (`usuario_codigo` vs `usuario_titulo` in `MOVIMENTACAO`). This caused `data.sql` insertions to fail silently or explicitly during context load.
**Action:** Always verify `src/test/resources/db/schema.sql` when encountering `ScriptStatementFailedException` in tests, rather than assuming the Entity mapping is the source of truth for the test DB.

## 2024-05-22 - WebMvcTest and Global Filters
**Learning:** `WebMvcTest` scans `@Component` beans like global filters (`FiltroAutenticacaoSimulado`). If these filters have dependencies (e.g., `GerenciadorJwt`, Repositories), they must be mocked using `@MockitoBean` in the test class, otherwise the ApplicationContext fails to load.
**Action:** When troubleshooting `ApplicationContext` failures in slicing tests, check for implicitly loaded beans like Filters and their dependencies.

## 2025-02-18 - Recursive Queries vs Single Fetch
**Learning:** Replacing recursive N+1 queries with a single `findAll` (or `JOIN FETCH`) plus in-memory processing is a significant performance win for hierarchical data, provided the dataset fits in memory (e.g., Org Charts).
**Action:** Look for recursive logic in service methods and replace with "fetch-all + map" pattern.

## 2024-05-22 - N+1 fix in Usuario
**Learning:** `FetchType.EAGER` on `OneToMany` collections is a silent performance killer, especially on frequently loaded entities like `Usuario`. Spring Data JPA finders (like `findByUnidadeLotacaoCodigo`) do not join these collections by default, leading to N+1 queries.
**Action:** Audit all `FetchType.EAGER` usages. When changing to `LAZY`, ensure that specialized methods (like authentication) explicitly initialize the collection within a transaction.

## 2025-12-21 - JPA Test Setup for Bidirectional Relationships
**Learning:** In `@Transactional` integration tests, querying for an entity that is already in the persistence context (Session) returns the existing managed instance. If that instance was created with incomplete data (e.g., a child added to DB but not to the parent's list in memory), subsequent operations that rely on traversing the relationship will see stale/empty data, even if `JOIN FETCH` was used.
**Action:** Always maintain bidirectional consistency in test setup. When creating a child entity, explicitly add it to the parent's collection in the test code.

## 2025-12-22 - Logic Bug masking Performance Issue
**Learning:** Sometimes code with N+1 queries effectively does nothing because of a logic bug (e.g., comparing IDs from different contexts/maps). Fixing the bug reveals the performance cost, so optimization and bug fixing must happen together.
**Action:** When analyzing "useless" loops, check if they are useless by design or by accident. If by accident, assume the fix will introduce load, and optimize preemptively.
