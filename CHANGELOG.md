# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Reminder Toggle Action on Edit Screen:** Added dedicated action button ("Tắt nhắc nhở" / "Bật nhắc nhở") at the bottom of `ReminderEditScreen` for existing reminders.
- **Shared AutoSilenceBottomSheet:** Consolidated `AutoSilenceBottomSheet` and `AutoSilenceBottomSheetContent` into `:core:designsystem` with customizable duration options and standardized descriptive text.
- **Flexible Reminder Snooze:** Added quick snooze chips (+5m, +10m, +15m) and custom minute entry dialog (`LvAlertDialog`) on the ringing screen.
- **Notification Quick Actions:** Added 5-minute quick snooze action and completion action to heads-up reminder notifications.
- **Inline Snooze Configuration:** Integrated snooze enable toggle and quick interval selector (5, 10, 15, 30 min) directly into the Add/Edit Reminder screen.
- **Sound Preview Playback Control:** Added interactive Play/Stop audio preview button (`ReMindSettingIcon`) at the trailing end of the Volume slider row in both `AlarmEditScreen` and `ReminderEditScreen`.

### Changed
- **Unified Reminder Lifecycle Management (`isEnabled` as Single Source of Truth):** Removed redundant `isCompleted` column across database (Room Migration 7->8), domain model, data mapper, and schedulers. One-time reminders naturally complete by disabling themselves (`isEnabled = false`), while recurring reminders advance their trigger timestamp while maintaining `isEnabled = true`.
- **Streamlined Reminder Card UI:** Removed inline `LiteverSwitch` from `ReminderCard`, preventing accidental toggles. Toggling reminders is now available via the 3-dot overflow menu bottom sheet ("Tắt nhắc nhở" / "Bật nhắc nhở") and the bottom of the edit screen.
- **Shared Permission Warning Banner:** Consolidated `PermissionWarningBanner` into `:core:designsystem` for shared use across both `:features:alarms` and `:features:reminders`. Added semantic severity handling: displays an error surface (`errorContainer`, `LvSemantic.Destructive`) when critical permissions are missing, and a warning surface (`warningContainer`, `ReMindIcons.Warning`, `LvSemantic.Warning`) with a "Remind me later" (`permission_warning_banner_remind_later`) dismissal action button when only non-critical permissions are missing. Tailored copy independently: Alarms reference alarm reliability, while Reminders specifically reference reminder precision and background stability ("Nhắc nhở" semantics).
- **Configuration-Aware Resource Queries:** Replaced `LocalContext.current.getString()` with `stringResource()` in `ReminderEditScreen.kt` for lifecycle- and configuration-aware string resolution, and replaced `Uri.parse()` with `toUri()`.
- **Compose Parameter Guidelines Compliance:** Reordered parameters in `ReminderEditRoute` and `ReminderEditScreen` so `modifier: Modifier = Modifier` is the first optional parameter.
- **Decoupled Volume & Vibration Rows:** Separated volume slider and vibration toggle into two distinct, dedicated UI rows in both `AlarmEditScreen` and `ReminderEditScreen` (`ReminderAlertConfigSection`), eliminating cramped layouts and offering clean, independent controls.
- **Dual Notification Modes:** Streamlined alert modes into "Ringing" (`NotificationStyle.FULL_SCREEN`, default) and "Normal Notification" (`NotificationStyle.STANDARD`) with clear descriptive subtitles.
- **Alert Sound & Vibration:** Ringtone selection is available for both modes. Volume slider is confined to Ringing mode (since Android system notification volume governs standard notifications), while Normal Notification provides a dedicated vibration switch.
- **Ringing-Specific Configurations:** Confined Auto-silence and STREAM_ALARM switch exclusively to "Ringing" mode.
- **Standardized Navigation & Title Label:** Standardized bottom navigation tab label and top bar title to "Nhắc nhở" (Reminders).

### Removed
- **Redundant `isCompleted` Field:** Dropped `isCompleted` column from Room database table `reminders` via `MIGRATION_7_8`, domain model `Reminder`, DAOs, repositories, and schedulers.
- **Completed Reminders Filter & Bulk Actions:** Excised obsolete completed reminders filter and batch delete completed actions from ViewModel and screens.
- **Duplicate AutoSilence Implementations:** Removed duplicated `AutoSilenceBottomSheet.kt` in `:features:alarms` and `ReminderAutoSilenceBottomSheetContent` in `:features:reminders`.
- **Dead Parameters & Callbacks:** Removed 9 legacy trigger callbacks, unused `reminderId`, and `onNavigateToSnoozeSettings` from `ReminderEditRoute`, `ReminderEditScreen`, and `remindersGraph`.
- **Snooze Repeat Limits:** Completely excised `snoozeRepeatCount` and `currentSnoozeCount` across domain model, database mapper, receivers, and UI.
- **Urgency Selector in UI:** Removed cluttered `UrgencyLevel` selector from the Add/Edit reminder configuration section.
- **Reminder List Filters:** Completely excised filter chips (`ReminderFilterChips`) and filter logic from the reminder list screen, presenting an uncluttered direct list with automatic sorting.

## [2.2.0] - 2026-09-30

### Removed
- **Reminder Checklist Feature:** Completely eliminated all checklist and subtask components from the Reminder feature:
  - Removed `ChecklistItem` domain model and related checklist progress calculations from `Reminder`.
  - Dropped `ChecklistItemEntity`, `ChecklistItemDao`, and multi-table relation `PopulatedReminder` from Room database.
  - Removed checklist inline toggle actions (`ACTION_REMINDER_CHECKLIST_TOGGLE`) from notifications, broadcast receivers, and manifest.
  - Purged periodic recurring checklist reset mechanisms (`ChecklistCycleUtil`) from sync and background alarm managers.
  - Removed checklist builder UI, ringing checklist viewer, expandable progress card views, and all checklist string resources.
  - Refactored test suites (`ReminderDatabaseChallengerTest`, `ReminderSnoozeChallengerTest`, `ReminderPermissionChallengerTest`) to focus cleanly on core reminder behaviors.
- **Debug Database Seeding:** Removed `RemindDatabaseCallback` and automatic sample alarm/mission insertion on debug database creation, ensuring the database starts completely clean.

## [2.1.0] - 2026-09-29

### Added
- **Explicit Trigger Types (FIXED_TIME & INTERVAL):** Separated reminders into two distinct operational paradigms:
  - `FIXED_TIME`: Static, non-drifting time targets (e.g. medicine reminders at "08:00", "20:00").
  - `INTERVAL`: Dynamic recurring intervals (e.g. drink water every 120 minutes) bounded within customizable active `timeWindows` (`startTime`, `endTime`, `activeDays`).
- **Multi-day Recurrence (repeatValues):**
  - Monthly recurrence: Supports selecting multiple specific days of the month (`monthlyDays: List<Int>`).
  - Yearly recurrence: Supports selecting multiple dates in a year (`yearlyDates: List<ReminderMonthDay>`).
  - Weekly recurrence: Supports selecting multiple days of the week (`repeatDays: List<DayOfWeek>`).
- **Urgency & Action Constraints:** Added `UrgencyLevel` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), `Checklist` (subtasks with completion status & ordering), customizable `SnoozeConfig`, and `isPersistentAlert` mode for full-screen alarm behavior.
- **Checklist In-Place Interaction:** Added expandable checklist view directly on `ReminderCard` with instant check/uncheck toggling synced to SQLite.
- **Checklist Recurring Cycle Auto-Reset:** Automatically resets completed checklist items when entering a new cycle (daily, weekly, monthly, yearly, or interval interval trigger) backed by `lastChecklistResetTime` in Room Database.
- **Room Database Migration v7:** Added `MIGRATION_6_7` to add `lastChecklistResetTime` column to `reminders` table.

### Changed
- **Reminder Architecture & Entities:** Streamlined `ReminderEntity` and domain `Reminder` model, removing legacy location/wifi triggers in favor of a robust time-based trigger system.
- **ViewModel & UI Integration:** Updated `ReminderEditViewModel` and composable screens (`ReminderTriggerEditScreen`, `ReminderEditScreen`, `ReminderTriggerSection`, `ReminderAlertConfigSection`) with intuitive multi-day selector dialogs and responsive reactive state.
- **Draft Checklist Auto-Flush:** Automatically saves any uncommitted checklist draft input text when the user clicks Save without pressing Add (+).

### Fixed
- **Alert Volume & Vibration Stream:** Enforced configured `reminder.action.volume` across both standard notifications and persistent alerts via `AudioAttributes.USAGE_ALARM` and `MediaPlayer.setVolume()`, honoring custom ringtones and vibrator attributes.
- **Checklist Persistence & Primary Key Duplication:** Prevented item replacement collisions in Room by enforcing auto-generated `id = 0L` on new items and using nanoTime for temporary UI keys.
- **Multi-module Test Dependencies:** Fixed missing `libs.junit` dependencies in `:features:today` and `:features:mission` test configurations.
- **Test Suite Hygiene:** Cleaned up orphaned location/wifi tests, ensuring 100% test pass across all project modules.

## [2.0.0] - 2026-09-23

### Added
- **Play Store Screenshot Automation (Fastlane Screengrab):** Added `ScreenshotsTest` instrumented test suite using `tools.fastlane:screengrab:2.1.1` and `UiAutomatorScreenshotStrategy` to automate capturing 6 core Play Store screenshots (`1_alarm_list`, `2_reminder_tab`, `3_ringing`, `4_mission_typing`, `5_mission_memory`, `6_alarm_message`).
- **In-Memory Mock Isolation & Multi-Language Strings:** Completely eliminated database dependencies from screenshot tests by rendering stateless Composables with in-memory mock data. Localized all mock text into English (`values/strings.xml`) and Vietnamese (`values-vi/strings.xml`) under `app/src/androidTest`, seamlessly switching via `LocaleTestRule`.
- **Fastlane Screengrab Lane & Configuration:** Created `fastlane/Screengrabfile` and added `:screenshots` lane to `fastlane/Fastfile` for automated build and capture across phone and tablet form factors.
- **AlarmSettingRow Component:** Created `AlarmSettingRow` in `:features:alarms` leveraging Material 3 `ListItem` and `ReMindSettingIcon` for consistent setting rows in `AlarmEditScreen`.
- **8-Cell Palette Grid:** Added an 8-cell 2-row color palette selector (`IconButton`s with `primaryContainer` backgrounds, centered `primary` text, selection borders, and checkmark indicators) in `GeneralSettingsScreen`.
- **Litever Palette Integration:** Integrated all 7 predefined Litever color palettes (Red, Orange, Yellow, Green, Blue, Indigo, Violet) and Dynamic wallpaper coloring.
- **Opinionated Lv* Suite:** Adopted high-level opinionated components from `litever-designsystem` v2.1.0: `LvButton`, `LvIconButton`, `LvTextField`, `LvAlertDialog`, `LvChip`, and `LvSnackbarHost` / `LvSnackbar`.
- **Semantic Tokens:** Added neutral semantic color tokens (`neutral`, `onNeutral`, `neutralContainer`, `onNeutralContainer`) to `ColorScheme` in `:core:designsystem` mapped to `LiteverTheme.colors`.
- **Root Snackbar Presentation:** Integrated `LvSnackbarHost` with `SnackbarHostState` inside the root `MainActivity` scaffold for standardized semantic snackbar support.
- **Today Next Alarm Status Card:** Added `TodayNextAlarmView` on `TodayScreen` to display upcoming alarm countdown or empty state illustration when all alarms are off/empty (non-interactive, shared via `:core:model` and `:core:designsystem`).
- **TodayScreen Cache-First & Auto-Refresh on Expiry:** Enforced cache-first strategy on `TodayScreen` initialization (`force = false`) to eliminate redundant network calls while ensuring automatic refresh on `ON_RESUME` when cache TTL (1 hour for weather, calendar day for briefing) expires. Resolved infinite loading deadlock by decoupling network loading (`isRefreshing`) from background AI processing (`isProcessing`), bypassing stale processing cache in repositories, and introducing automatic background polling.

### Removed
- **App-Specific Color Tokens:** Removed over 340 lines of local color definitions in `:core:designsystem/theme/Color.kt` (`primaryLight`, `remindLightColors`, `remindDarkColors`, contrast variants, etc.) in favor of direct consumption of `:litever-designsystem` palettes.

### Changed
- **Centralized Iconography & Dependency Isolation:** Centralized all 92 icons used across the entire application into a single source of truth `ReMindIcons` inside `:core:designsystem`. Completely removed `libs.material.icons.extended` dependency and all direct `androidx.compose.material.icons.*` imports from `:app` and all `:features:*` modules, enforcing clean encapsulation and reducing module compile classpath footprint.
- **GeneralSettingsScreen UI Modernization & Unification:** Completely unified `GeneralSettingsScreen` using standardized `ReMindSettingsItem` rows and dialog popups backed by `LvAlertDialog`. Replaced dense Segmented Buttons (Time Format, Display Mode, Language) and the 8-cell color grid with clean single-choice dialogs. Added a color preview swatch dot to the Primary Color row and grouped Language and Time Format into a cohesive **Language & Region** (`Ngôn ngữ & Vùng`) card.
- **WeatherInfoView Simplification & Translucency:** Streamlined `FullWeatherView` (directly to single `Column` with `clip` & `background`) and `CompactWeatherView` (directly to single `Row` with `clip`, `background` & `clickable`) in `:core:designsystem`, completely eliminating `Card`, `Box`, `Surface` layout wrappers and unused imports. Adjusted weather background opacity to `0.3` in Dark Mode and `0.7` in Light Mode, with standardized text contrast (`onSurface` / `onSurfaceVariant`) for clean visibility across all themes.
- **Default Theme Color:** Changed application default primary color palette from legacy brown to Litever **RED** (`LiteverThemeColor.RED` / `redLightColorScheme` / `redDarkColorScheme`).
- **Dependencies:** Updated `liteverDesignsystem` version to `2.1.0` in `gradle/libs.versions.toml`.
- **Package Modularization:** Aligned all component imports across all modules to use domain-segregated packages (`vn.io.litever.designsystem.components.button.*`, `vn.io.litever.designsystem.components.textfield.*`, `vn.io.litever.designsystem.components.dialog.*`, etc.).
- **Component Adoption Across Modules:**
  - `:core:designsystem`: Refactored `ReMindAlertDialog`, `ReMindBottomBar`, `TimePickerDialog`, `WeatherInfoView`, `RingtoneSelectionScreen`, and `SnoozeSettingsScreen` to use `LvButton` and `LvAlertDialog`.
  - `:features:alarms`: Refactored `AlarmEditScreen`, `AlarmListScreen`, `AlarmMessageScreen`, `AlarmRingingScreen`, `AlarmCard`, and `PermissionWarningBanner` to use `LvButton`, `LvIconButton`, `LvTextField`, and `LvChip`.
  - `:features:settings`: Refactored `AlarmSettingsScreen` (`DurationSelectionDialog` to `LvAlertDialog`) and `PermissionSettingsScreen` to use `LvButton`.
  - `:features:mission`: Refactored `MemoryGameConfigScreen`, `MissionRingingScreen`, `PhraseSelectionScreen`, `TypingMissionConfigScreen`, and `MathMissionContent` to use `LvButton`, `LvIconButton`, and `LvTextField`.
  - `:features:today`: Refactored `LocationSearchScreen` to use `LvTextField` and `LvIconButton` with primary color indicators.

### Fixed
- **Boilerplate Reduction & Visual Uniformity:** Replaced hundreds of repetitive lines of manual shape, color, and padding configuration across all feature screens with unified 6.dp/10.dp squircle geometry and `LvSemantic` palette intents.

## [1.2.0] - 2026-09-11

### Added
- **Design System Integration:** Integrated `litever-designsystem` v2.0.0 via Gradle Composite Build (`includeBuild("../litever-designsystem")`) with dependency substitution for `com.github.toantd2000:litever-designsystem`.
- **Core Components & Wrappers:** Added custom UI components in `:core:designsystem` built on native Material 3: `ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`, `ReMindSettingsGroup`, `ReMindSettingsItem`, and `ReMindTimePickerDialog`.
- **Semantic Tokens:** Added support for semantic color tokens (`warning`, `onWarning`, `success`, `onSuccess`) in `ReMindTheme` aligned with `LiteverTheme.colors`.
- **Testing:** Added comprehensive unit test suites covering UI logic and state models in `:features:mission` (`MathMissionLogicTest`, `MemoryGameBoardTest`, `MissionRingingUiStateTest`, `TypingMissionLogicTest`) and `:features:today` (`TodayUiLogicTest`).

### Changed
- **Dependencies:** Updated `liteverDesignsystem` version to `2.0.0` in `gradle/libs.versions.toml`.
- **UI Architecture:** Migrated all screens across `:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, and `:app` from obsolete pass-through wrappers to native Jetpack Compose Material 3 composables (`Scaffold`, `Button`, `OutlinedTextField`, `AlertDialog`, `IconButton`, etc.) styled with `LiteVerButtonDefaults` and `LiteVerTextFieldDefaults`.
- **Design Tokens:** Replaced 100% of hardcoded `.dp` spacing values throughout all feature modules with standardized `LiteverTheme.spacing` tokens (`tiny`, `extraSmall`, `small`, `smallMedium`, `medium`, `large`, etc.).
- **Window Insets:** Standardized insets consumption across root and nested `Scaffold` composables to avoid double-padding issues on devices with system bars.

### Removed
- **Obsolete Wrappers:** Removed all dependencies on 24 pass-through wrapper components previously provided by `litever-designsystem` v1.x (such as `LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, `LiteverTextField`, `LiteverCard`, `LiteverCircularProgressIndicator`, `LiteverIconButton`, `LiteverSwitch`, etc.).

### Added
- **Master Audit & Stability Hardening:** Comprehensive audit covering all 10 edge cases and architectural friction points across Reminders and Alarms (`REMINDERS_AUDIT_REPORT.md`).
- **Snooze Pinning & Switch Guard:** Snoozed reminders pinned to the top of all filter tabs (`ALL`, `ACTIVE`, `TIME`, `LOCATION`, `WIFI`), and toggle switches hidden while snoozing to prevent unintended cancellation loops.
- **Immediate Checklist DB Persistence:** Ringing checklist completions saved immediately to Room DB to guarantee full preservation across multi-snooze cycles.
- **Permission Warning Banners:** Proactive UI warning banners when critical background/geofence or notification permissions are missing or revoked, preventing silent scheduling failures.
- **Empirical Test Suites:** Added comprehensive unit test suites (`ReminderR1R2EmpiricalChallengerTest`, `ReminderPermissionAndChecklistChallengerTest`) verifying edge cases and sync invariants.

### Changed
- **Synchronization Integrity:** Enforced universal `ReminderSyncManager.sync()` invocations across all single and batch delete/restore operations.
- **Audio Scoping:** Scoped audio playback coroutines in `ReminderEditViewModel` to eliminate coroutine leaks during rapid sound preview changes and screen teardown.
- **Trigger Switching:** Explicit cancellation of previous geofence and Wi-Fi monitoring jobs when switching trigger types in `ReminderEditViewModel`.
- **Shared Audio & Snooze Components:** Extracted `RingtoneSelectionScreen` and `SnoozeSettingsScreen` into `:core:designsystem` for shared usage across Alarms and Reminders.
- **Wi-Fi Picker:** Added automatic detection and selection of currently connected Wi-Fi networks in Reminder Edit.
- **Ringing Navigation Priority:** Priority routing in `MainActivity` ensuring active Alarms take precedence over Reminders.

## [1.1.7] - 2026-07-15

### Changed
- **UI:** Standardized `TailwindColors`, unified `ReMindSettingIcon` component, updated bottom bar and button dimensions.
- **Infrastructure:** Updated AGP and cleaned up `MainActivity`.

### Fixed
- **UI:** Fixed a compilation error in `ReminderTimeConfigSection` where `@Composable` functions were illegally called inside `joinToString` lambdas.
- **Localization:** Fixed language localization issues in time picker dialogs.

## [1.1.6] - 2026-05-30

### Added
- **UI:** Added smooth item placement animations in the alarm list using `Modifier.animateItem()`.

### Changed
- **Update History:** Moved changelog parsing logic to ViewModel and migrated `UpdateHistoryScreen` to use `LiteverTheme`.
- **Infrastructure:** Updated dependencies and synchronized UI state management for better stability.

### Fixed
- **UI:** Fixed a compilation error in `ReminderTimeConfigSection` where `@Composable` functions were illegally called inside `joinToString` lambdas.
- **UI:** Fixed a bug in `AlarmCard` where the click animation (ripple) and the left accent bar would leak outside the rounded corners.
- **Localization:** Fixed a bug where Dialogs were showing the wrong language by resolving `stringResource` outside the Dialog scope.

## [1.1.5] - 2026-05-20

### Added
- **Audio Focus & Streams:** Added Alarm Stream toggle and fixed Audio Focus logic for alarms.
- **Notification Updates:** Updated notification content and importance when ringing.
- **Settings Restructure:** Reorganized the settings screen into three distinct groups: App Settings, Support & Community, and About & Legal.
- **Support Developer Refactoring:** Converted the full-width premium card into a standard settings list item with a Heart icon under the Support category. Clicking it triggers a beautiful custom modal dialog with actions to Watch Ad or Donate. Decoupled the settings item subtitle (short & concise) from the dialog content (displays the full, heartfelt thank-you message).
- **Rewarded Ad Support:** Implemented AdMob Rewarded Ads support (`SUPPORT_REWARDED`) to temporarily disable ads for 24 hours in exchange for watching an ad.
- **Ad Placement Serialization Mapping:** Tagged the `SUPPORT_REWARDED` enum value with `@SerialName("SUPPORT_REWARD")` to automatically support the new ads configuration JSON placement key without renaming or breaking any existing references.
- **Supporter Reward Tuning:** Added a dynamic build-type check that automatically limits the granted ad-free supporter duration to **30 seconds** on Debug variants instead of the standard 24 hours to support rapid, iterative developer verification.
- **Ad Simulator Fallback:** Created a Compose-based interactive countdown `RewardedAdSimulatorDialog` as a graceful fallback when real ads are not loaded.
- **FAQ Stub:** Added future plans FAQ dialog to notify users of upcoming updates.
- **Exit Dialog Upgrades:** Re-embedded Native Ads (`EXIT_NATIVE`) into `ExitAppDialog` inside the original `Box` container structure. Upgraded both the exit and support developer dialogs with highly sincere, heartfelt messages and a warm daily wish.
- **Conditional Ad-Free Exit UI:** Connected supporter reward state (`isAdFreeActive`) to the exit dialog, dynamically removing the support plea and hiding native ads entirely when the user is enjoying their ad-free status.

### Changed
- **Design System:** Upgraded `litever-designsystem` to `v1.0.5` and standardized buttons.
- **Settings Dialog Refinements:** Removed the redundant close button from the Support Developer dialog using a custom `confirmButton = {}` layout wrapper.
- **Exit Dialog Polish:** Removed the rewarded ad button ("Watch ad to support...") from `ExitAppDialog` completely, ensuring a lightweight and non-disruptive exit confirmation flow.
- **Navigation Tweaks:** Hidden the unimplemented Alarm Settings menu item from the settings tab.

### Fixed
- **UI:** Fixed a compilation error in `ReminderTimeConfigSection` where `@Composable` functions were illegally called inside `joinToString` lambdas.
- **Navigation Crash:** Prevented Navigation crash by waiting for NavHost initialization in MainActivity.
- **Lifecycle Fixes:** Bound RingingScreen visibility state to lifecycle events to ensure alarm can wake up screen again after snooze.
- **UI:** Fixed preview and cleanup code in MissedAlarmDialog.

## [1.1.4] - 2026-05-12

### Added
- **Exit Verification:** Added an elegant exit dialog to confirm app closure.
- **Improved Missions:** Modernized the Typing Mission UI with "Typing for Memory" insights to enhance cognitive reinforcement.
- **Draft Support:** Implemented a draft pattern for alarm creation with change detection to prevent accidental loss of settings.
- **UI Components:** Added `ReMindLoadingIconButton` for better interaction feedback during reload actions.

### Changed
- **UX Refinement:** Optimized the "Add Alarm" flow for a smoother configuration experience.
- **Performance:** modularized advertisement logic using Firebase Remote Config for more dynamic and stable control.
- **Design System:** Bumped `litever-designsystem` to `v1.0.4`.

### Fixed
- **UI:** Fixed a compilation error in `ReminderTimeConfigSection` where `@Composable` functions were illegally called inside `joinToString` lambdas.
- **Cloning Stability:** Fixed a bug where missions and private phrases were duplicated during alarm cloning.
- **Localization:** Improved translations and fixed activity finish logic in the exit dialog when using language overrides.

## [1.1.3] - 2026-05-09

### Added
- **AI-Powered Insights:** Integrated real-time AI status tracking for weather and reminder hints.
- **Smart Refresh:** Added a smart refresh mechanism that automatically polls for updates when AI is processing.
- **Location Awareness:** Implemented automatic location detection for precise weather forecasts.
- **Attributions & Credits:** A new screen to properly credit open-source libraries and illustrations (Storyset).
- **Personal Thanks:** Included a heartfelt thank you message to the resources and community.

### Changed
- **UI Enhancements:**
    - Grouped reminders into a single elegant card for better focus.
    - Improved Next Alarm header visibility and styling.
    - Redesigned Remind screen with a cleaner top bar and better scrolling.
- **Settings Reorganization:** Moved "Open Source Licenses" into the new Attributions screen for a cleaner Settings layout.
- **Performance:** Reduced advertisement cache duration for fresher content.

## [1.1.2] - 2026-05-08

### Added
- **Custom Ringtone Selection:** Users can now pick any audio file from their device to use as an alarm sound.
- **Permission Optimization:** Implemented using Storage Access Framework (SAF), eliminating the need for `READ_EXTERNAL_STORAGE` permission while maintaining persistent access across reboots.

### Fixed
- **UI:** Fixed a compilation error in `ReminderTimeConfigSection` where `@Composable` functions were illegally called inside `joinToString` lambdas.
- **Alarm Skip Logic:** Fixed a bug where skipped alarms were incorrectly marked as "Missed due to power off" if the alarm time passed while the app was closed.
- **Database Maintenance:** Implemented automatic cleanup of expired "Skip once" statuses. Alarms will now correctly reset their skipped state once the skipped occurrence has passed, ensuring both DB and UI stay consistent.

## [1.1.1] - 2026-05-05
