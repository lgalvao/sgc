## 2024-05-22 - Preventing N+1 in JPA/Hibernate
**Learning:** JPA's default lazy loading behavior often leads to N+1 problems when accessing nested collections.
**Action:** Use 'JOIN FETCH' in JPQL queries to preload related entities. When multiple levels of nesting are involved (and Hibernate forbids multiple bag fetches), fetch the leaf nodes (Activities with Knowledge) separately and join them in memory using a Map.
## 2024-05-22 - Optimizing N+1 with existing repositories
**Learning:** Sometimes the necessary repository methods (like one with 'JOIN FETCH') already exist but aren't being used. Always check the repository interface before writing new queries.
**Action:** When fixing N+1, look for existing methods like 'findBy...With...' that might have been added for other contexts but fit the current need perfectly.
