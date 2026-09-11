# Project: ReMind Android - LiteVer Design System 2.0.0 Migration

## Architecture
- **Multi-module Android application**: `:app`, `:core:*` (`:core:designsystem`, `:core:model`, `:core:data`, `:core:alarm`, etc.), `:features:*` (`:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`).
- **Design System Dependency**:
  - Direct dependency on `com.github.toantd2000:litever-designsystem` is isolated strictly inside `:core:designsystem` (`api(libs.litever.designsystem)`).
  - Feature modules access design system APIs transitively via `implementation(project(":core:designsystem"))`.
  - Composite build integration connects local `../litever-designsystem` project `:designsystem` via Gradle `includeBuild`.
- **Design Philosophy (v2.0.0 Lean Architecture)**:
  - Removal of 24 pass-through wrapper components (`LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, etc.).
  - Direct adoption of Jetpack Compose Material 3 (M3) native composables (`Button`, `OutlinedTextField`, `Scaffold`, `AlertDialog`, etc.).
  - Styling via `LiteVerButtonDefaults`, `LiteVerTextFieldDefaults`.
  - Design tokens via `LiteverTheme.spacing` (`none`, `tiny` 2dp, `extraSmall` 4dp, `small` 8dp, `smallMedium` 12dp, `medium` 16dp, `mediumLarge` 20dp, `large` 24dp, `extraLarge` 32dp, `doubleLarge` 48dp, `tripleLarge` 64dp), `LiteverTheme.shapes`, and `LiteverTheme.colors` (including semantic `warning`, `success`).
  - App-specific wrappers maintained in `:core:designsystem` (`ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`, `ReMindSettingsGroup`, `ReMindSettingsItem`, `ReMindTimePickerDialog`).

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Gradle includeBuild & Dependency Substitution | Add includeBuild for `../litever-designsystem` substituting `com.github.toantd2000:litever-designsystem` -> `:designsystem` | M1 | R1 |
| 2 | Version Catalog Bump | Update `liteverDesignsystem` to `2.0.0` in `gradle/libs.versions.toml` | M1 | R1 |
| 3 | Core Theme & Semantic Token Alignment | Align `ReMindTheme` with `LiteverTheme`, supporting semantic colors (`warning`, `success`) | M2 | R2 |
| 4 | Core Custom Component Wrappers | Refactor `ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon` to native M3 | M2 | R2 |
| 5 | Core Missing Component Provision | Provide `ReMindSettingsGroup`, `ReMindSettingsItem`, and `ReMindTimePickerDialog` in `:core:designsystem` | M2 | R2 |
| 6 | Core Screens Refactor | Refactor `RingtoneSelectionScreen`, `SnoozeSettingsScreen`, `WeatherInfoView`, `BrandLogo`, `MissionSelectionBottomSheet` | M2 | R2 |
| 7 | Core Spacing Token Migration | Replace hardcoded `.dp` in `:core:designsystem` with `LiteverTheme.spacing.*` | M2 | R2 |
| 8 | Feature Alarms M3 & Defaults Refactor | Refactor `AlarmEditScreen`, `AlarmListScreen`, `AlarmMessageScreen`, `AlarmRingingScreen` to M3 + LiteVer Defaults | M3 | R3 |
| 9 | Feature Alarms Components Refactor | Refactor `AlarmCard`, `ExitAppDialog`, `MissedAlarmDialog`, `PermissionWarningBanner` to M3 | M3 | R3 |
| 10 | Feature Alarms Spacing Token Migration | Replace hardcoded `.dp` in `:features:alarms` with `LiteverTheme.spacing.*` | M3 | R3 |
| 11 | Feature Settings M3 & Defaults Refactor | Refactor `SettingsScreen`, `AlarmSettingsScreen`, `LicensesScreen`, `PermissionSettingsScreen`, `AttributionsScreen`, `GeneralSettingsScreen` | M4 | R3 |
| 12 | Feature Settings Spacing Token Migration | Replace hardcoded `.dp` in `:features:settings` with `LiteverTheme.spacing.*` | M4 | R3 |
| 13 | Feature Mission M3 & Defaults Refactor | Refactor `TypingMissionConfigScreen`, `PhraseSelectionScreen`, `MissionRingingScreen`, `MemoryGameConfigScreen`, `TypingMissionContent` | M5 | R3 |
| 14 | Feature Mission Spacing Token Migration | Replace hardcoded `.dp` in `:features:mission` with `LiteverTheme.spacing.*` | M5 | R3 |
| 15 | Feature Today M3 & Spacing Refactor | Refactor `TodayScreen`, `LocationSearchScreen` to M3 and `LiteverTheme.spacing.*` | M6 | R3 |
| 16 | App Module Refactor | Refactor `MainActivity.kt` scaffold and theme configuration | M6 | R3 |
| 17 | Per-Module Conventional Commits | Create modular Git commits conforming to Conventional Commits standard | M1-M7 | R4 |
| 18 | LOGWORK.md Update | Add Phase 7 to Roadmap and append `[TDR-061]` | M7 | R5 |
| 19 | LEARNING_JOURNAL.md Update | Prepend `[2026-09-09]` learning entry on `includeBuild` & M3 Defaults | M7 | R5 |
| 20 | ARCHITECTURE_MAP.md Update | Update module graph, composite build link, review log | M7 | R5 |
| 21 | CHANGELOG.md Update | Update `[Unreleased]` with v2.0.0 migration details | M7 | R5 |
| 22 | DECISION_LOG.md Update | Add Section 7 with architectural decisions & UI scenarios | M7 | R5 |
| 23 | Full Compilation & Audit Verification | Verify `./gradlew assembleDebug` / `compileDebugKotlin` 100% clean | M7 | Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | M1: Gradle Configuration & includeBuild Integration | `settings.gradle.kts`, `gradle/libs.versions.toml` | none | DONE (7f2a6cc) |
| 2 | M2: Core Design System & Theme Alignment | `:core:designsystem` components, theme, screens, spacing | M1 | DONE (004a1e2) |
| 3 | M3: Feature Alarms Refactor | `:features:alarms` UI screens, components, spacing | M2 | DONE (29de0c6) |
| 4 | M4: Feature Settings Refactor | `:features:settings` UI screens, components, spacing | M2 | DONE (505362d) |
| 5 | M5: Feature Mission Refactor | `:features:mission` UI screens, components, spacing | M2 | DONE (814a94a) |
| 6 | M6: Feature Today & App Refactor | `:features:today`, `:app` UI screens, spacing | M2 | DONE (6cd2707, 54e383b) |
| 7 | M7: Documentation & Full Verification | `LOGWORK.md`, `LEARNING_JOURNAL.md`, `ARCHITECTURE_MAP.md`, `CHANGELOG.md`, `DECISION_LOG.md`, full build | M3, M4, M5, M6 | DONE |

## Interface Contracts
### `:core:designsystem` ↔ Feature Modules (`:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`)
- **Theme & Tokens**:
  - `LiteverTheme.spacing`: `small` (8dp), `smallMedium` (12dp), `medium` (16dp), `large` (24dp), etc.
  - `LiteverTheme.shapes`: M3 shapes.
  - `LiteverTheme.colors`: M3 colors + semantic `warning`, `onWarning`, `success`, `onSuccess`.
- **Component Defaults**:
  - `LiteVerButtonDefaults`: `primaryColors()`, `secondaryColors()`, `tonalColors()`, `destructiveColors()`, `outlinedColors()`, `textColors()`, `ContentPadding`.
  - `LiteVerTextFieldDefaults`: `outlinedColors()`, `colors()`, `shape`.
- **ReMind Custom Wrappers**:
  - `ReMindTopAppBar(title, navigationIconType, onNavigationClick, actionIcon, onActionClick, ...)`
  - `ReMindAlertDialog(title, text, confirmButtonText, onConfirm, dismissButtonText, onDismiss, isDestructive, ...)`
  - `ReMindBottomBar(...)`
  - `ReMindLoadingIconButton(...)`
  - `ReMindSettingIcon(...)`
  - `ReMindSettingsGroup(...)`, `ReMindSettingsItem(...)`
  - `ReMindTimePickerDialog(...)`

## Code Layout
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `core/designsystem/src/main/java/vn/io/litever/remind/core/designsystem/`
  - `theme/` (`Theme.kt`, `Color.kt`)
  - `components/` (`ReMindTopAppBar.kt`, `ReMindAlertDialog.kt`, `ReMindBottomBar.kt`, `ReMindLoadingIconButton.kt`, `ReMindSettingIcon.kt`, `BrandLogo.kt`, `WeatherInfoView.kt`, `MissionSelectionBottomSheet.kt`, `SettingsComponents.kt`, `TimePickerDialog.kt`)
  - `ringtone/RingtoneSelectionScreen.kt`
  - `snooze/SnoozeSettingsScreen.kt`
- `features/alarms/src/main/java/vn/io/litever/remind/features/alarms/ui/`
- `features/settings/src/main/java/vn/io/litever/remind/features/settings/ui/`
- `features/mission/src/main/java/vn/io/litever/remind/features/mission/ui/`
- `features/today/src/main/java/vn/io/litever/remind/features/today/ui/`
- `app/src/main/java/vn/io/litever/remind/app/`
- Root documentation files:
  - `LOGWORK.md`
  - `LEARNING_JOURNAL.md`
  - `ARCHITECTURE_MAP.md`
  - `CHANGELOG.md`
  - `DECISION_LOG.md`
