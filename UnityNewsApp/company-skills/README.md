# Company AI Skills

This folder demonstrates how a company can package repeatable engineering workflows as reusable AI skills.

In a real organization these files would live in an internal Codex skill registry or a shared `.codex/skills` package. They are kept inside this Android project for the take-home so reviewers can inspect the idea without installing private tooling.

## Skills Included

- `android-feature-module`: add a new feature with `domain`, `data`, and `presentation` modules.
- `presentation-module-structure`: keep Compose screens split into screen, components, filters, and model packages.
- `offline-first-cache`: preserve Room-as-source-of-truth behavior.
- `backend-filter-contract`: add backend-driven filters without UI rewrites.
- `android-security-review`: review manifest, IPC, and release hardening changes.

## How A Developer Would Use This

Before changing a feature, the developer asks the AI assistant to use the relevant skill. The skill forces the same architecture, testing, and review checklist every time, which makes scaling the team safer.
