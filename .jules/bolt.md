# Bolt's Journal

This journal records critical performance learnings.

## 2025-01-26 - [Dead Code & Batch Inserts]
**Learning:** Found two common anti-patterns in `mapa` services:
1. Loops performing single `save()` calls instead of `saveAll()`, causing N+1 database roundtrips during bulk operations (like map copying).
2. "Zombie" logic in `SubprocessoMapaService` that fetched large datasets and created DTOs but discarded them immediately.
**Action:** Always check for `save()` inside loops and replace with `saveAll()`. Verify that data fetched is actually used.

## 2025-01-26 - [Database Filtering in ProcessoConsultaService]
**Learning:** filtering large datasets (Subprocessos) in application memory after fetching all records is a major bottleneck. Moving this logic to JPQL queries with `JOIN FETCH` drastically reduces memory usage and DB load.
**Action:** Always prefer specialized repository methods for filtering over stream filtering for potentially large collections.

## 2025-01-26 - [Nested N+1 in Facades]
**Learning:** Facades coordinating multiple services often hide N+1 issues when they iterate over a list of DTOs and call service methods (like `obterPorCodigo`) inside loops. This is compounded when nested loops exist (e.g., Activities inside Competencies).
**Action:** When refactoring Facades, implement batch methods in the underlying Services (`buscarPorIds`, `atualizarEmLote`) to allow the Facade to fetch all needed data upfront.
