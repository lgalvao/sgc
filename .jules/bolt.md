## 2024-05-22 - Optimization of Map Adjustment
**Learning:** `SubprocessoFacade.obterMapaParaAjuste` suffered from potential N+1 or inefficient looping when mapping Competence-Activity associations because `Competencia.getAtividades()` was accessed inside a loop.
**Action:** Used `CompetenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo` to fetch associations as IDs and `CompetenciaRepo.findByMapaCodigoSemFetch` to fetch entities without heavy relations. Passed a `Map<Long, Set<Long>>` to `MapaAjusteMapper` to resolve associations in O(1) without hydrating extra entities.
