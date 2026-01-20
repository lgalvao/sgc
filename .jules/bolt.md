## 2025-02-18 - Batch Deep Copy with IdentityHashMap
**Learning:** When batch inserting entities (`saveAll`) that need to be referenced later by their source ID (e.g., during a deep copy), standard Maps fail because `hashCode/equals` might change after persistence or not distinguish transient instances.
**Action:** Use `IdentityHashMap<NewEntity, SourceId>` to track the relationship between the transient object and its source ID before saving. After `saveAll`, iterate the transient objects (now with IDs populated) and use the map to link them back.
