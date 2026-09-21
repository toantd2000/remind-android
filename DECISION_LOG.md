# Technical Decision & Scenario Log

This document records the solidified technical decisions and behavioral scenarios agreed upon during development. **Do not modify these core logics without explicit instruction or a new design review.**

## 1. Alarm Ringing & Mission Workflow

### Scenario: Starting a Mission
- **Expected Behavior**: Alarm sound and vibration must stop immediately. Auto-silence timer must be suspended.
- **Technical Decision**:
    - UI calls `reminderRingManager.mute(id)`.
    - `ReminderService` observes the `mutedReminderIds` via a reactive Flow.
    - When an ID enters the muted list, `ReminderService` calls `stopCurrentRinging()` and cancels the `autoSilenceJob`.

### Scenario: Abandoning/Backing from Mission
- **Expected Behavior**: Alarm sound and vibration must resume immediately. Auto-silence timer must restart from the beginning.
- **Technical Decision**:
    - `MissionRingingViewModel.abandonMission()` calls `reminderRingManager.unmute(id)` **immediately** upon the user clicking "Back" or "Close".
    - This ensures the sound starts even before the navigation transition (popBackStack) completes.
    - `ReminderService` detects the unmute and calls `startRinging()` and `setupAutoSilence()`.

### Scenario: Completing an Alarm (Dismiss/Snooze/Auto-Silence)
- **Expected Behavior**: The alarm must be removed from the ringing queue and the muted list. Service should stop if no more alarms are ringing.
- **Technical Decision**:
    - `ReminderRingManager.dequeueReminder(id)` must also clear the ID from `mutedReminderIds` to prevent memory leaks and state "ghosting".
    - `ReminderService` monitors the `ringingReminderId`. If it becomes `null` and it has previously started ringing, it calls `stopForeground` and `stopSelf`.

## 2. Media Player Management

### Safe-Start Pattern
- **Decision**: All `MediaPlayer` control (start, stop, release) must happen on the **Main Thread**.
- **Preparation**: `MediaPlayer.prepare()` must happen on an **IO Thread** to avoid UI jank (ANR).
- **Integrity Check**: Before `player.start()`, always check `isActive` (or `coroutineContext.isActive`) to ensure the ringing session wasn't cancelled during the asynchronous preparation phase.

## 3. Database & KSP

### DAO Query Patterns
- **Decision**: Methods returning `Flow` for reactive updates and `suspend` methods for one-shot updates must be clearly separated.
- **Consistency**: Use `@Transaction` for methods that fetch complex relations (like `PopulatedReminder`) to ensure data consistency across multiple tables.

## 4. Alarm Scheduling & PendingIntent Identity

### Scenario: Coexistence of Main Alarm and Snooze
- **Problem**: Android distinguishes `PendingIntent` only by Component, Action, and RequestCode. Extras are ignored. Sharing these fields between Main and Snooze alarms causes one to overwrite or cancel the other.
- **Decision**:
    - **Unique Actions**: Use `ACTION_TRIGGER_ALARM` for main occurrences and `ACTION_TRIGGER_SNOOZE` for snoozes.
    - **Unique Request Codes**: Use `alarm.id.hashCode()` for main and `alarm.id.hashCode() + 1,000,000,000` for snooze.

## 5. UI & Navigation

### Scenario: Exiting the App from Home Screen
- **Expected Behavior**: Pressing "Back" on the home screen should show an Exit Dialog. The Exit Dialog contains a heartfelt wish and polite exit message. If ads are enabled/active, the message explicitly instructs the user to support the app by interacting with the native ad displayed below, and the native ad is rendered in its padded container. If the user is an ad-free supporter, the ad and the support plea are completely hidden.
- **Technical Decision**:
    - **Back Interception**: Use `BackHandler` in `AlarmListScreen` and collect the real-time reactive `isAdFreeActive` Flow.
    - **Exit App Dialog Polish**: Cleaned up all rewarded ad triggers, simulator dialogues, and countdown buttons from the Exit App Dialog completely.
    - **Conditional Ad-Free Layout**: Wrap the AdMob `NativeAdView` inside a conditional Compose block checking `!isAdFreeActive`, and conditionally set the body description string dynamically based on the ad-free active status.
    - **Context Unwrapping**: Use a helper extension `Context.findActivity()` to safely unwrap `baseContext` until `Activity` is resolved for finishing.

## 6. Ad System & Supporter Mechanisms

### Scenario: Supporter Ad-Free Status
- **Expected Behavior**: When a user watches a rewarded ad or plays the simulator fallback (which is restricted exclusively to emulators), they get 24 hours of ad-free status. All native ads and other promotional components throughout the app must be immediately and dynamically hidden.
- **Technical Decision**:
    - **Persistent State**: Store the ad-free period expiration timestamp (`ADS_DISABLED_UNTIL`) using `AlarmPreferencesDataSource` (DataStore).
    - **Centralized Ad Suppression**: Inside `AdMobManagerImpl`, inject `AlarmPreferencesDataSource` to observe `adsDisabledUntil`. If `currentTimeMillis < adsDisabledUntil`, reject all native/banner loading immediately.
    - **Emulator-Restricted Simulator**: In `SettingsScreen`, clicking "Watch Ad" pre-loads AdMob rewarded ads (`SUPPORT_REWARDED`). If the ad is loaded, the real ad is displayed. If not loaded:
        - On an emulator (`DeviceUtils.isEmulator() == true`), we trigger `RewardedAdSimulatorDialog` running a beautiful 5s countdown fallback.
        - On a physical device (`DeviceUtils.isEmulator() == false`), we show a lightweight toast (`rewarded_ad_not_ready`) indicating that the ad is not ready yet, and do not show any simulator interface.

## 7. LiteVer Design System 2.0.0 & Material 3 Refactoring

### Scenario: Lean M3 Architecture & Component Defaults Pattern
- **Expected Behavior**: Eliminate redundant pass-through wrapper components (`LiteverButton`, `LiteverScaffold`, `LiteverTopAppBar`, `LiteverTextField`, `LiteverCard`, etc.) while ensuring consistent theme styling, colors, and accessibility defaults across all feature screens.
- **Technical Decision**:
    - **Native Composable Direct Usage**: Adopt standard Jetpack Compose Material 3 composables (`Button`, `OutlinedTextField`, `Scaffold`, `AlertDialog`, `IconButton`, `Surface`) directly in all feature modules (`:features:alarms`, `:features:settings`, `:features:mission`, `:features:today`, `:app`).
    - **Styling via LiteVer Defaults**: Apply `LiteVerButtonDefaults` (e.g. `primaryColors()`, `secondaryColors()`, `tonalColors()`, `destructiveColors()`, `outlinedColors()`, `ContentPadding`) and `LiteVerTextFieldDefaults` (e.g. `outlinedColors()`, `shape`) directly to native M3 composable arguments.
    - **App-Specific Wrappers Preservation**: Maintain true app-level custom composables inside `:core:designsystem` (`ReMindTopAppBar`, `ReMindAlertDialog`, `ReMindBottomBar`, `ReMindLoadingIconButton`, `ReMindSettingIcon`, `ReMindSettingsGroup`, `ReMindSettingsItem`, `ReMindTimePickerDialog`) acting as the single source of truth for ReMind-specific UX patterns.

### Scenario: Spacing Tokens vs Border Stroke Discipline
- **Expected Behavior**: All layout distances, paddings, and item spacings must follow the unified design token system, while graphic outline strokes remain technically precise.
- **Technical Decision**:
    - **Semantic Spacing Scale**: Enforce `LiteverTheme.spacing` tokens (`tiny` 2dp, `extraSmall` 4dp, `small` 8dp, `smallMedium` 12dp, `medium` 16dp, `mediumLarge` 20dp, `large` 24dp, `extraLarge` 32dp, `doubleLarge` 48dp, `tripleLarge` 64dp) for all layout modifiers (`padding`, `contentPadding`, `spacedBy`). Eliminate all hardcoded `.dp` values.
    - **Border Stroke Exemption**: Technical border strokes (such as `BorderStroke(1.dp, color)` or `HorizontalDivider(thickness = 1.dp)`) represent graphic line weights rather than layout spacing and are strictly preserved as explicit DP values without being falsely coerced into spacing tokens.

### Scenario: Root Scaffold Inset Consumption & Nested Scaffolds
- **Expected Behavior**: Application supports full edge-to-edge rendering without double-padding artifacts or layout clipping when screens with their own TopAppBar/Scaffold are nested inside the root navigation host.
- **Technical Decision**:
    - **Root Scaffold Inset Management**: The root `Scaffold` in `:app` (`MainActivity.kt`) manages top-level system bars insets and bottom navigation bar placement.
    - **Nested Scaffold Zero-Inset Override**: Child screens declaring their own `Scaffold` (for localized top app bars, floating action buttons, or snackbars) explicitly specify `contentWindowInsets = WindowInsets(0, 0, 0, 0)` or selectively consume insets via `WindowInsets.safeDrawing.only(...)`. This completely prevents nested scaffolds from re-consuming system insets and creating unwanted gaps.

## 8. LiteVer Design System 2.1.0 & Opinionated Lv* Components Adoption

### Scenario: Package Segregation & Modular Domain Namespaces
- **Expected Behavior**: Component classes and defaults must be segregated into dedicated functional packages (`components.button.*`, `components.textfield.*`, `components.chip.*`, `components.dialog.*`, `components.snackbar.*`) to optimize compilation, eliminate package cluttering, and support tree-shaking.
- **Technical Decision**:
  - Update all import references across all 6 modules from flat `components.*` to explicit sub-packages.
  - Declare deprecated aliases where applicable during transition, fully adopting new namespaces across all feature composables.

### Scenario: Adoption of Opinionated LvButton & LvIconButton
- **Expected Behavior**: Action buttons and icon buttons must automatically follow LiteVer's signature squircle shape (6.dp) and standard semantic palettes without verbose manual configuration.
- **Technical Decision**:
  - Replace raw M3 `Button`, `OutlinedButton`, `TextButton`, and `IconButton` with `LvButton` and `LvIconButton`.
  - Style buttons using high-level `type` (`LvButtonType.Filled`, `Outlined`, `Text`, `Tonal`) and `semantic` (`LvSemantic.Primary`, `Secondary`, `Tertiary`, `Neutral`, `Success`, `Destructive`, `Warning`).
  - Retain standard Material 3 accessibility, interaction sources, and ripple animations under the hood.

### Scenario: Adoption of Opinionated LvTextField
- **Expected Behavior**: Input fields must enforce standard 6.dp squircle corners, active semantic borders, and ergonomic String-based labels and placeholders, while supporting error state messages out of the box.
- **Technical Decision**:
  - Replace `OutlinedTextField` + `LiteVerTextFieldDefaults` with `LvTextField(type = LvTextFieldType.Outlined, label = "...", placeholder = "...", semantic = ...)`.
  - Simplify input state rendering in forms (`AlarmEditScreen`, `AlarmMessageScreen`, `PhraseSelectionScreen`, `LocationSearchScreen`, `MathMissionContent`).
  - Manage dynamic clear buttons using trailing icon lambdas without triggering unnecessary recompositions.

### Scenario: Adoption of LvAlertDialog for System Dialogs
- **Expected Behavior**: Dialogs must use moderate squircle corners (10.dp) rather than Material 3's excessively rounded 28.dp corners, with standardized confirm and dismiss button styling.
- **Technical Decision**:
  - Adopt `LvAlertDialog` in `ReMindAlertDialog` (`:core:designsystem`) and `DurationSelectionDialog` (`:features:settings`).
  - Standardize button placement using `LvButton(type = LvButtonType.Text, semantic = LvSemantic.Secondary)` for dismiss and `semantic = LvSemantic.Primary` for confirm.

### Scenario: Standardized LvSnackbarHost Integration
- **Expected Behavior**: In-app notifications and snackbars must support semantic colors (Success, Destructive, Warning, Neutral) and squircle corners.
- **Technical Decision**:
  - Integrate `LvSnackbarHost` directly in `MainActivity.kt` root `Scaffold`, utilizing `LvSnackbarVisuals` to carry semantic metadata.

## 9. Palette Color System & Selection Grid

### Scenario: Litever RED as Default Palette
- **Expected Behavior**: New app installations and fallback defaults must use Litever's signature RED color scheme rather than the legacy brown scheme.
- **Technical Decision**:
  - Set default preference key value in `AlarmPreferencesDataSource` to `"RED"`.
  - Set default state in `SettingsUiState` and `ReMindTheme` to `"RED"`.
  - Align all token definitions in `Color.kt` to the Red palette values.

### Scenario: 8-Cell 2-Row Color Palette Grid
- **Expected Behavior**: Users can choose from 7 pre-packaged Litever color schemes (Red, Orange, Yellow, Green, Blue, Indigo, Violet) or device wallpaper dynamic coloring. The picker must be rendered as an 8-cell 2-row grid of `IconButton`s, with `primaryContainer` backgrounds, centered `primary` text, and prominent selection indication.
- **Technical Decision**:
  - Implement a 2-row grid of `IconButton`s in `GeneralSettingsScreen`.
  - Background is bound to each palette's `primaryContainer` in the active theme mode (Light/Dark).
  - Text in the center is bound to each palette's `primary` color.
  - Active selection is identified by a 2.dp `primary` border and an `Icons.Rounded.Check` indicator.

## 10. Today Screen Widgets & Cards

### Scenario: Non-interactive Next Alarm Status Card (2-Column Layout, Rich Details & Positive Rest Message)
- **Expected Behavior**:
  - When an upcoming alarm exists: The card adopts a 2-column layout:
    - **Column 1**: Square alarm icon box (76.dp squircle with `primaryContainer` and centered `Alarm` icon).
    - **Column 2**:
      - Row 1: "Next Alarm" title on the left with a subtle countdown badge ("Alarm in X hours Y mins") on the right.
      - Row 2: Prominent alarm time typography (`titleLarge` bold + AM/PM) -> mission icons row -> repeat schedule on the right.
      - Row 3: Alarm label in italic style.
    - Strictly non-interactive (no switch, no action menu, no click callback).
  - When no upcoming alarms exist: The card presents a soothing and positive rest message ("Peaceful Rest" / "No upcoming alarms scheduled. Relax and enjoy your peaceful rest!") alongside the empty-state illustration matching the current Light/Dark theme.
- **Technical Decision**:
  - Compute next alarm status reactively in `TodayViewModel` using `calculateNextAlarm(enabledAlarms)` from `:core:model`, providing the nearest `Alarm` object.
  - Implement `TodayNextAlarmView` as a pure presentation component with squircle corners and 2-column balanced structure, strictly non-interactive.
  - Order components in `TodayScreen` using `Arrangement.spacedBy(LiteverTheme.spacing.medium)` without redundant Spacers: Weather $\rightarrow$ TodayNextAlarmView $\rightarrow$ NativeAdView $\rightarrow$ TodayQuoteView (at the very bottom).

