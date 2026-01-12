# Bolt's Journal

This journal records critical performance learnings.

## 2025-01-26 - [Dead Code & Batch Inserts]
**Learning:** Found two common anti-patterns in `mapa` services:
1. Loops performing single `save()` calls instead of `saveAll()`, causing N+1 database roundtrips during bulk operations (like map copying).
2. "Zombie" logic in `SubprocessoMapaService` that fetched large datasets and created DTOs but discarded them immediately.
**Action:** Always check for `save()` inside loops and replace with `saveAll()`. Verify that data fetched is actually used.
