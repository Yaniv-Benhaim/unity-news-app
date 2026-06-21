---
name: presentation-module-structure
description: Use when adding or refactoring Compose presentation code.
---

# Presentation Module Structure Skill

Use this skill when Compose files start to mix screen routing, component rendering, state models, and UI helpers.

## Required Packages

- `screen`: route-level composables and screen sections.
- `components`: reusable pieces inside the feature, such as cards, empty states, and banners.
- `filters`: filter controls and filter-specific UI.
- `model`: UI state and UI-only mapping models.

## File Rules

- A root screen should choose which state branch to render; it should not contain cards, mappers, image helpers, or filter internals.
- Components should be stateless whenever possible.
- Feature components stay in the feature module.
- Truly reusable primitives move to `core:ui`.

## Compose Review Checklist

- No file should need more than one mental model to understand.
- Dynamic backend filters must render by `FilterType`, not hard-coded filter keys.
- Image loading must use shared `core:ui` primitives.
- Empty, loading, stale, and error states must be explicit.
