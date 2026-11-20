---
name: remind-architecture
description: >-
  Enforces ReMind Android project-specific Clean Architecture, multi-module dependency graph,
  namespace conventions, Room database migration rules, and Gradle STOP protocol.
  Use when creating or modifying modules, Domain/Data logic, Room entities, or Gradle files.
metadata:
  author: Litever-Android
  version: "2.0"
---

# ReMind Multi-Module & Clean Architecture Specification

## 1. Module Dependency Graph & Namespace Rule

Base package name: `vn.io.litever.remind`.
Every module namespace and source path MUST follow `vn.io.litever.remind.<module_path_with_dots>`:

- **`:app`** (`vn.io.litever.remind.app`): Application entry point, root navigation, DI assembly.
- **`:features:<name>`** (`vn.io.litever.remind.features.<name>`): UI layer only (`ui` package: Compose Screens, ViewModels, `UiState`).
  - **Allowed dependencies:** `:core:domain`, `:core:model`, `:core:designsystem`, `:core:common`, `:core:analytics`, `:core:ads:api`.
  - **FORBIDDEN:** Never depend on `:core:data`, `:core:database`, `:core:network`, or another `:features:*` module.
- **`:core:model`** & **`:core:domain`**: Pure business models, Repository interfaces, and single-responsibility UseCases.
  - Keep free of Android UI, Room (`@Entity`), or Retrofit/Serialization annotations.
- **`:core:data`**: Repository implementations (`OfflineFirst*Repository`) and Mappers (`Entity.toDomain()` / `Domain.toEntity()`).
- **`:core:database`**: Room Database, DAOs, Room Entities, and explicit SQL `Migration(old, new)`.
- **`:core:alarm`**: Exact alarm scheduling, `BroadcastReceiver`, `PendingIntent`, and foreground alarm services.
- **`:core:designsystem`**: App-owned theme entry point (`ReMindTheme` directly configuring `MaterialTheme` + token `CompositionLocal`s from `:litever-designsystem`) and ReMind-specific composite UI components.

## 2. Strict Data & Domain Constraints

1. **Exception Boundary Isolation:**
   - Inside `RepositoryImpl` (`:core:data`), catch all framework/IO exceptions (`SQLiteException`, `HttpException`, `IOException`) and map them to pure Domain Exceptions before returning `Result<T>`. Never leak framework exceptions to UseCases or ViewModels.
2. **Room Database Migration (Zero Data Loss):**
   - Whenever adding, modifying, or removing columns/tables in `:core:database`, you MUST increment the Room database `version` and provide an explicit `Migration(oldVersion, newVersion)` with SQL statements (`ALTER TABLE`, `CREATE TABLE`, etc.).
   - **NEVER** use `fallbackToDestructiveMigration()` in production code.
3. **Testability by Construction:**
   - Always inject `CoroutineDispatcher` via constructor (using qualifiers from `:core:common`) rather than hardcoding `Dispatchers.IO` or `Dispatchers.Default` inside UseCases or Repositories.

## 3. Scaffolding a New Module & Gradle STOP Protocol

When creating a new module:
1. Clone `compileSdk`, `minSdk`, and plugin aliases from an existing sibling module (e.g., `:features:reminders/build.gradle.kts`).
2. Create `.gitignore` with `/build` inside the new module root.
3. Use Version Catalog (`gradle/libs.versions.toml`) exclusively. Do NOT upgrade existing library versions unless explicitly requested.
4. Register in `settings.gradle.kts`, `:app/build.gradle.kts`, and update `ARCHITECTURE_MAP.md`.
5. **STOP Protocol:** If a Gradle configuration change causes a Gradle Sync error that cannot be trivially resolved in the newly added file, **DO NOT guess versions or modify `:app/build.gradle.kts` blindly**. Stop immediately, report the exact error output, and hand control back to the user.
