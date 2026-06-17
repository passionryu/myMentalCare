# Backend Directory Structure

This document defines the backend directory policy for `apps/server`.

## Direction

The backend uses a vertical slice structure by feature. A feature owns its application use cases, feature-specific ports, request/response DTOs, policies, readers, recorders, and bootstrap adapters under the same feature package.

The goal is to keep use case flow, external boundaries, and feature ownership visible from the directory tree.

This policy is currently applied to `aichat`, `auth`, `member`, `inquiry`, and `mypage`. Cross-cutting outbound notification code is grouped under `notification`.

## Application Module

Use this structure for feature-owned application code.

```text
modules/application/src/main/kotlin/com/mymentalcare/server/application/<feature>
├── usecase
├── request
├── response
├── port
├── reader
├── recorder
└── policy
```

- `usecase`: orchestrates application use cases and implements input ports.
- `request`: application command/query request models.
- `response`: application response models returned to bootstrap or other use cases.
- `port`: feature-specific input/output ports.
- `reader`: read-side responsibility objects used by use cases.
- `recorder`: write/history/event responsibility objects used by use cases.
- `policy`: decisions, rules, factories, generators, and fallback policies.

Shared cross-feature ports may remain in `application/port`. Feature-specific ports must live in `<feature>/port`.

Feature-specific exceptions may remain in the feature package root when they are referenced across several subpackages or bootstrap exception handlers.

## Bootstrap Module

Use this structure for application entrypoints and external adapters.

```text
modules/bootstrap/mymentalcare/src/main/kotlin/com/mymentalcare/server/bootstrap/<feature>
├── web
│   ├── request
│   └── response
└── adapter
    └── <provider-or-technology>
```

- `web`: Spring MVC controllers, exception handlers, and response mappers.
- `web/request`: HTTP payloads only.
- `web/response`: HTTP response DTOs only.
- `adapter/<provider-or-technology>`: external system adapters such as `openai` or `redis`.

Bootstrap DTOs must not leak into the application module. Convert them at the bootstrap boundary.

Shared HTTP-only models that are not owned by a single feature, such as `ApiErrorResponse`, live under `bootstrap/common/web`.

## Domain And Infrastructure

Keep domain and persistence grouped by feature.

```text
modules/domain/src/main/kotlin/com/mymentalcare/server/domain/<feature>
modules/infrastructure/persistence/src/main/kotlin/com/mymentalcare/server/infrastructure/persistence/<feature>
```

- `domain/<feature>` owns entities, value objects, and domain enums.
- `infrastructure/persistence/<feature>` owns JPA entities, Spring Data repositories, and persistence adapters.

Infrastructure adapters should depend on application ports, not use cases.

## Rules

- Do not add feature-specific files to generic buckets such as `application/port` or `bootstrap/<feature>` root.
- Keep controller payloads separate from application request/response models.
- Put business decisions in `policy`, not in controller or adapter code.
- Put external provider details in `bootstrap/<feature>/adapter/<provider-or-technology>`.
- Keep common HTTP response models in `bootstrap/common/web`, not under a feature package.
- Add new feature packages using the same structure before adding files.
