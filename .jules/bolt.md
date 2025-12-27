## 2025-02-18 - N+1 in JPA Bidirectional Relationships
**Learning:** Even if the "owning" side of a relationship (or the inverse side) is fetched in one query, the other side of the relationship on related entities loaded in a *different* query remains uninitialized. Accessing it checks the database, causing N+1.
**Action:** When you have both sides of a relationship loaded in memory from different queries, use ID-based lookups (Sets/Maps) to correlate them instead of navigating the lazy relationship getter.
