## 2024-05-22 - Identifying N+1 in JPA
**Learning:** Checking for N+1 queries manually is crucial when relying on ORMs. Even with 'LEFT JOIN FETCH' on the primary collection, nested associations (like 'unidadeSuperior' on 'Unidade') can still trigger secondary queries if not explicitly fetched or batch loaded.
**Action:** Always verify the full depth of the object graph needed by the view/DTO and adjust JPQL queries to fetch all required levels or use EntityGraphs.
