## 2024-05-22 - H2 Schema Mismatch in Tests
**Learning:** The project uses `schema.sql` for H2 tests with `ddl-auto: none`, but the schema definition was out of sync with JPA Entities (`usuario_codigo` vs `usuario_titulo` in `MOVIMENTACAO`). This caused `data.sql` insertions to fail silently or explicitly during context load.
**Action:** Always verify `src/test/resources/db/schema.sql` when encountering `ScriptStatementFailedException` in tests, rather than assuming the Entity mapping is the source of truth for the test DB.

## 2024-05-22 - WebMvcTest and Global Filters
**Learning:** `WebMvcTest` scans `@Component` beans like global filters (`FiltroAutenticacaoSimulado`). If these filters have dependencies (e.g., `GerenciadorJwt`, Repositories), they must be mocked using `@MockitoBean` in the test class, otherwise the ApplicationContext fails to load.
**Action:** When troubleshooting `ApplicationContext` failures in slicing tests, check for implicitly loaded beans like Filters and their dependencies.
