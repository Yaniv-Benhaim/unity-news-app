---
name: offline-first-cache
description: Use when changing article loading, caching, or repository behavior.
---

# Offline-First Cache Skill

Use this skill before touching `features/news/data` cache or repository code.

## Architecture Rules

- Room is the source of truth for rendered articles.
- Remote refresh writes into Room.
- UI observes Room and keeps showing cached data when refresh fails.
- Each filter selection has a stable cache key.

## Required Edge Cases

- Remote success replaces the matching query cache.
- Remote failure preserves cached articles and marks stale state.
- Different filter values do not share cached result sets.
- Reordered filter keys and selected values produce the same cache key.
- Cancellation is rethrown and never swallowed as a normal failure.

## Reusable Helpers

- Use `core:data` for stable cache-key helpers.
- Do not duplicate JSON hash logic inside repositories or DAOs.
