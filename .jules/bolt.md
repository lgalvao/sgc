## 2024-05-22 - Test Schema Inconsistency
**Learning:** The test schema (`backend/src/test/resources/db/schema.sql`) was missing indexes present in the production schema (`backend/src/main/resources/db/schema.sql`). This can lead to performance tests (like H2-based ones) not accurately reflecting production behavior, or worse, hiding missing index issues.
**Action:** When adding indexes, always verify and update both schema files.

## 2024-05-22 - Broken Legacy Tests
**Learning:** `ConfiguracaoFacadeTest` was failing compilation due to a partial refactoring (Entities to DTOs) that wasn't updated in the test. This blocked running the test suite.
**Action:** Be prepared to perform "Good Samaritan" fixes on unrelated broken tests to unblock the build pipeline.
