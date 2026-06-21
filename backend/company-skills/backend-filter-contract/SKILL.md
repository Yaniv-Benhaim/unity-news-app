---
name: backend-filter-contract
description: Use when changing backend-owned filters or AIDL filter requests.
---

# Backend Filter Contract Skill

Use this skill when adding a backend-owned filter.

## Required Steps

1. Add filter metadata in the backend repository.
2. Interpret the new key in backend domain filtering.
3. Keep unknown filter keys as no-ops unless the contract explicitly says otherwise.
4. Keep frontend and backend AIDL DTOs identical.
5. Add tests for the new filtering rule.

## Boundary Rule

The UI app renders filter controls from metadata. It must not need code changes for a new option under an existing filter type.
