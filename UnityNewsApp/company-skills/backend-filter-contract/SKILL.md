---
name: backend-filter-contract
description: Use when adding or changing backend-driven filter support.
---

# Backend Filter Contract Skill

Use this skill before changing filter metadata, AIDL DTOs, or filtering behavior.

## Contract Rules

- Backend owns filter semantics.
- UI owns rendering controls.
- UI must not branch on backend-specific keys such as `title` or `rating`.
- Filter criteria travel as `Map<String, Set<String>>` in domain and `Map<String, List<String>>` across AIDL.

## Required Steps

1. Add or update backend `FilterSpec` metadata.
2. Update backend filtering use case for any new semantic key.
3. Keep AIDL DTO source files identical in both apps.
4. Add backend domain tests for filtering behavior.
5. Add UI/presentation tests only when rendering behavior changes.

## Verification

Run contract diffs between both projects before finishing:

```bash
diff -qr UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract backend/features/server/data/src/main/aidl/com/unitynews/contract
diff -qr UnityNewsApp/features/news/data/src/main/java/com/unitynews/contract backend/features/server/data/src/main/java/com/unitynews/contract
```
