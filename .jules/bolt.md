## 2025-02-18 - N+1 in JPA Bidirectional Relationships
**Learning:** Even if the "owning" side of a relationship (or the inverse side) is fetched in one query, the other side of the relationship on related entities loaded in a *different* query remains uninitialized. Accessing it checks the database, causing N+1.
**Action:** When you have both sides of a relationship loaded in memory from different queries, use ID-based lookups (Sets/Maps) to correlate them instead of navigating the lazy relationship getter.
## 2024-05-22 - Fix N+1 in Process Initialization
**Learning:** Found N+1 query pattern where units were fetched one-by-one inside a loop during revision process initialization.
**Action:** Optimized to batch fetch all units before the loop using findAllById.
## 2025-02-18 - Redundant fetching in validation methods
**Learning:** Helper methods used for validation often re-fetch data that is already available in the calling context (e.g., `findById` calls). This is especially costly when the query involves joins or is executed at the end of a transaction where data is already in memory.
**Action:** Pass the necessary data (lists, entities) as arguments to the validation method instead of passing just the ID and letting the method re-fetch it.
## 2025-02-18 - Caching Read-Only Views
**Learning:** Entities mapped to database views (like `Unidade` via `VW_UNIDADE`) are effectively read-only for the application. Standard JPA write caching/eviction isn't needed or possible.
**Action:** For expensive calculations on view-based entities (like recursive hierarchy traversal), use `@Cacheable` without `@CacheEvict` (since the app doesn't write to it), but ensure the cache strategy matches the data freshness requirements (e.g., TTL or external invalidation if the view changes underlying data frequently).
