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

### Scenario: Quick Alarm Lifecycle & Deletion Rules
- **Expected Behavior**:
    - Khi click vào item Quick Alarm trên danh sách, không mở màn hình chỉnh sửa báo thức thông thường (`AlarmEditScreen`).
    - Khi tắt báo thức nhanh bằng nút switch trên danh sách, hệ thống tự động xóa vĩnh viễn báo thức khỏi CSDL và hủy lịch trong `AlarmScheduler`.
    - Khi hoàn thành rung chuông (người dùng bấm Tắt chuông / hoàn thành nhiệm vụ - `dismissAlarm`), báo thức nhanh tự động bị xóa vĩnh viễn khỏi CSDL và hủy lịch.
- **Technical Decision**:
    - Trong `AlarmCard.kt`: Kiểm tra `isQuickAlarm`, nếu đúng thì gắn badge tia sét nhận diện và trong `AlarmListScreen` chặn điều hướng `onAlarmClick`.
    - Trong `AlarmListViewModel.toggleAlarm`: Khi `alarm.isQuickAlarm == true`, điều hướng trực tiếp gọi `deleteAlarm(alarm)` thay vì chỉ cập nhật trạng thái `isEnabled`.
    - Trong `AlarmControllerImpl.dismissAlarm`: Khi `alarm.isQuickAlarm == true`, thực hiện `alarmScheduler.cancel(alarm)` và `alarmRepository.deleteAlarm(alarm)`.

## 5. UI & Navigation

### Scenario: Exiting the App from Home Screen
- **Expected Behavior**: Pressing "Back" on the home screen should show an Exit Dialog. The Exit Dialog contains a heartfelt wish and polite exit message. If ads are enabled/active, the message explicitly instructs the user to support the app by interacting with the native ad displayed below, and the native ad is rendered in its padded container. If the user is an ad-free supporter, the ad and the support plea are completely hidden.
- **Technical Decision**:
    - **Back Interception**: Use `BackHandler` in `AlarmListScreen` and collect the real-time reactive `isAdFreeActive` Flow.
    - **Exit App Dialog Polish**: Cleaned up all rewarded ad triggers, simulator dialogues, and countdown buttons from the Exit App Dialog completely.
    - **Conditional Ad-Free Layout**: Wrap the AdMob `NativeAdView` inside a conditional Compose block checking `!isAdFreeActive`, and conditionally set the body description string dynamically based on the ad-free active status.
    - **Context Unwrapping**: Use a helper extension `Context.findActivity()` to safely unwrap `baseContext` until `Activity` is resolved for finishing.

### Scenario: Inline Reminder Trigger Condition Configuration & Interval Recurrence Support
- **Expected Behavior**: When creating or editing a reminder, the trigger conditions (Fixed Time vs Interval, recurrence schedule, time window) are configured directly on `ReminderEditScreen` without navigating to a separate screen. The Tab Selector clearly distinguishes "Mốc thời gian" (`FIXED_TIME`) and "Lặp định kỳ" (`INTERVAL`) with distinct visual icons (`AccessTime` vs `Timer`). Both trigger types support rich recurrence scheduling (One-time with date picker, Weekly with day-of-week pills, Monthly with 1..31 day selector, Yearly with month-day dates).
- **Technical Decision**:
    - **Single Screen Editing**: Embedded `ReminderTriggerSection` directly into `ReminderEditScreen`'s LazyColumn, passing all state and callbacks from `ReminderEditViewModel`.
    - **Tab Differentiation**: Updated `ReminderTriggerTypeSelector` to use `R.string.trigger_fixed_time` and `R.string.trigger_interval` with respective semantic icons.
    - **Interval Recurrence Parity**: Integrated `ReminderRepeatConfigSection` for both `FIXED_TIME` and `INTERVAL`. Updated `TimeTrigger.getNextIntervalOccurrence()` with `isDateValid` and `getNextValidDate` to evaluate recurrence days before calculating interval steps and bounded time windows.
    - **Extensibility**: Kept component modularized so future complex triggers (Geofence, Wi-Fi) can easily be accommodated with a dedicated sub-screen when needed.

### Scenario: Reminder Card Rich Information & Next Trigger Countdown Display
- **Expected Behavior**: Each card on the Reminder list displays comprehensive metadata (target times/interval frequency, recurrence schedule like weekdays or month days, active time windows, snooze status, urgency, and a dynamic relative countdown indicating when the next reminder will trigger or if disabled). The layout must remain compact, structured, and visually clean.
- **Technical Decision**:
    - **Visual Hierarchy (3-zone layout)**:
        - *Top Meta Badges (`FlowRow`)*: Combines `TriggerBadge` (times/frequency + recurrence pattern), `TimeWindowBadge` (active operating hours), `SnoozingBadge` (snooze target time), and priority badges.
        - *Body*: Clean title with 2-line max ellipsis and optional notes snippet.
        - *Footer (`NextTriggerFooter`)*: Shows dynamic next occurrence and relative time (e.g., `Lần tới: Hôm nay 14:00 (sau 2 giờ 15 phút)`, `Lần tới: Ngày mai 08:00 (sau 14 giờ)`, `Báo lại: 10:35 (còn 12 phút)`, `Đã tắt`).
    - **Pure Utility Separation**: Extracted `ReminderCardFormatter` to handle all recurrence, frequency, time window, and relative countdown string formatting deterministically, with 100% unit test coverage in `ReminderCardFormatterTest`.

### Scenario: Unified Reminder Empty State & Today Reminder Status View
- **Expected Behavior**:
    - On `ReminderListScreen`, when there are no reminders, display a rich empty state matching `AlarmListScreen` (using `FeedbackStateView`, theme-aware illustration, `ReMindLogo`, and a primary CTA button `+ Thêm nhắc nhở mới`), while hiding the top overflow menu and floating action button.
    - On `TodayScreen`, display a non-interactive status card (`TodayReminderStatusView`) right below `TodayNextAlarmView` that shows only how many reminders are currently active (or a peaceful empty state card when 0 reminders are active).
- **Technical Decision**:
    - **Empty State Alignment**: Updated `ReminderEmptyState` to use `FeedbackStateView(type = FeedbackStateType.EMPTY)` with `no_alarm_illustration` / `no_alarm_illustration_dark` from `:core:designsystem`, `ReMindLogo`, and `LvButton(semantic = LvSemantic.Primary)`. Added `isLoading` to `ReminderListUiState` to prevent flashing the empty state before Room emits initial data.
    - **Today Reminder Status View**: Injected `ReminderRepository` into `TodayViewModel` to expose `activeReminderCount: StateFlow<Int>` via `countActiveReminders`. Created `TodayReminderStatusView` matching `TodayNextAlarmView`'s 2-state visual hierarchy (`surfaceContainer` with active count badge and `NotificationsActive` icon when `activeReminderCount > 0`, and `surfaceContainerLow` with illustration when `activeReminderCount == 0`).

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

### Scenario: Cache-First Data Fetching, Auto-Refresh on Expiry & Non-Blocking AI Processing
- **Expected Behavior**:
  - Navigating to or opening `TodayScreen` adheres strictly to Cache-First policy: cached weather (1 hour TTL) and reminder briefing (same calendar day TTL) are rendered immediately from DataStore, avoiding redundant network roundtrips.
  - When returning to the app (`ON_RESUME`), `TodayViewModel.onResume()` triggers `refresh(force = false)`, which automatically updates weather data if the 1-hour TTL has elapsed or a new day has arrived.
  - If cached data is in an AI `processing` state, it is not treated as a valid cache, allowing immediate network re-fetch.
  - While AI processing is ongoing (`isProcessing == true`), ViewModel automatically polls the backend every 10 seconds (up to 6 times).
  - The TopAppBar refresh button only displays a spinning loading indicator and disables during an active network request (`isRefreshing`), never locking during background AI processing. Users can trigger a manual refresh at any time when not already refreshing.
  - The location picker in `WeatherInfoView` is disabled only during active network refresh (`!isRefreshing`).


## 11. Play Store Screenshot Automation (Fastlane Screengrab)

### Scenario: Automated Multi-Language Store Screenshots Without DB Side-Effects
- **Expected Behavior**:
  - Automatically captures high-resolution screenshots for 6 critical Play Store listing screens:
    1. Alarm List (`1_alarm_list`)
    2. Reminders Tab (`2_reminder_tab`)
    3. Alarm Ringing (`3_ringing`)
    4. Mission Typing (`4_mission_typing`)
    5. Mission Memory Game (`5_mission_memory`)
    6. Alarm Morning Message (`6_alarm_message`)
  - Supports multiple locales (English `en-US` and Vietnamese `vi-VN`) seamlessly in a single command.
  - Zero database contamination: absolutely no fake rows inserted or queried from Room DB / DAO.
  - Consistent Light Mode theme presentation for clear store listing visibility.
- **Technical Decision**:
  - Adopt `tools.fastlane:screengrab:2.1.1` and `androidx.test.uiautomator:uiautomator:2.3.0` with `UiAutomatorScreenshotStrategy`.
  - Use `createAndroidComposeRule<ComponentActivity>()` to directly render stateless screen composables with pure in-memory mock models.
  - Store localized mock content in `app/src/androidTest/res/values/strings.xml` and `values-vi/strings.xml`, resolving dynamically per locale through `LocaleTestRule`.
  - Maintain `fastlane/Screengrabfile` and Fastfile `:screenshots` lane for single-command end-to-end execution.

## 12. Icon System & Dependency Isolation

### Scenario: Centralized Iconography via ReMindIcons
- **Expected Behavior**: Feature modules (`:app`, `:features:*`) must not depend directly on Material Icons Extended or import `androidx.compose.material.icons.*`. All icons across the application must be accessed through a centralized single source of truth.
- **Technical Decision**:
  - Centralize all used icons into `ReMindIcons` located at `vn.io.litever.remind.core.designsystem.icon.ReMindIcons` in `:core:designsystem`.
  - Use Google NowInAndroid flat-properties convention (`ReMindIcons.Add`, `ReMindIcons.ArrowBack`, `ReMindIcons.Settings`, etc.) returning `ImageVector`.
  - Confine `implementation(libs.material.icons.extended)` exclusively to `:core:designsystem`. Remove this dependency from `:app` and all `:features:*` modules to prevent classpath leaking and enforce strict encapsulation.

## 13. Reminder Architecture: Trigger Separation & Multi-day Recurrence

### Scenario: Trigger Specialization & Complex Recurrence
- **Expected Behavior**:
  - `FIXED_TIME`: Triggers at exact static times without drifting. If user wakes up late or an alarm is missed, the next scheduled occurrence remains fixed at the designated daily/monthly/yearly time slot.
  - `INTERVAL`: Calculates repeating periods based on elapsed minutes (e.g. every 2 hours) and only fires within user-defined active `timeWindows` (`startTime`, `endTime`, `activeDays`).
  - `repeatValues` Multi-day Recurrence:
    - Monthly: User can select multiple days in a month (e.g., 5th and 20th).
    - Yearly: User can select multiple specific dates in a year (e.g., family birthdays, annual anniversaries).
    - Weekly: User can select multiple days of the week.
  - Alert Urgency & Persistence:
    - `LOW`, `MEDIUM`, `HIGH`: Standard notification stream with appropriate channel priorities.
    - `CRITICAL` or `isPersistentAlert = true`: Escalates to full-screen high-priority alert / alarm behavior.
- **Technical Decision**:
## 14. Deprecation & Complete Removal of Checklist Subtasks
- **Context**: The Checklist/subtask subsystem added substantial architectural overhead (`PopulatedReminder` joins, periodic cycle reset listeners, notification action handlers, and extended UI layouts). Product direction prioritized high-reliability time triggering over secondary task list management.
- **Decision**:
  - Completely deprecate and excise checklist components across all layers (Model, Room DB, Repository, Notification/Alarm Manager, and Jetpack Compose UI).
  - DAOs now return `ReminderEntity` directly without multi-table relation mapping.
  - Notification and broadcast receivers handle only pure reminder triggers and snoozes.


## 15. Flexible Snooze & Dual Alert Styles (Normal Notification vs Ringing)

### Scenario: Flexible Snooze & Complete Removal of Snooze Count Limit
- **Expected Behavior**:
  - Ringing screen offers quick snooze chips (+5m, +10m, +15m) alongside a "Custom" ("Tùy chỉnh") button that opens a dialog to enter any custom snooze duration.
  - Snoozing has no maximum repetition count (snoozeRepeatCount and currentSnoozeCount are completely removed from models, UI, and logic). Snoozing is available indefinitely as long as snooze is enabled.
  - In the heads-up notification (status bar / banner), only two quick actions are presented: "Báo lại 5 phút" (Snooze 5m) and "Hoàn thành" (Done).
- **Technical Decision**:
  - Excised snoozeRepeatCount from ReminderAction and currentSnoozeCount from Reminder.
  - Updated ReminderReceiver and ReminderNotificationManager to support EXTRA_SNOOZE_MINUTES = 5 for notification quick action.
  - Ringing screen uses LvAlertDialog and LiteverTextField from :core:designsystem for the custom snooze picker.

### Scenario: Dual Alert Modes (Normal vs Ringing)
- **Expected Behavior**:
  - Alerts are categorized into "Thông báo thường" (NotificationStyle.STANDARD) and "Đổ chuông" (NotificationStyle.FULL_SCREEN).
  - Default style when creating new reminders is "Đổ chuông" (FULL_SCREEN).
  - Both modes open the full-screen ringing UI on trigger.
  - Mode differentiation:
    - "Đổ chuông" runs ReminderAlertService to play continuous audio loop until dismissed or snoozed, and supports "Tự động tắt tiếng" (Auto-silence) and "Kênh báo thức" (STREAM_ALARM).
    - "Thông báo thường" does not run continuous foreground alert service, and supports ringtone and vibration configuration (volume follows the system notification stream).
  - In Add/Edit Reminder Screen:
    - The obsolete UrgencyLevel selector (SOFT/MEDIUM/HARD) is completely removed from the UI.
    - Notification Style selector is prominently placed at the top with descriptive subtitles explaining each mode.
    - Ringtone selection applies to both modes (dynamically resolved via `RingtoneManager`).
    - Volume Slider, Auto-silence, and STREAM_ALARM switch are exclusively shown under "Đổ chuông".
    - "Thông báo thường" displays a clean standalone Vibration switch (without volume slider, as notification sound volume is governed by Android system notification volume).
    - Snooze configuration is displayed as a setting row (leading `ReMindIcons.Snooze`, "Báo lại" title, summary subtitle showing interval or "Tắt", and trailing chevron) matching the "Tự động tắt chuông" row inside `ReminderAlertConfigSection`. Tapping the row opens the shared `SnoozeBottomSheet` (in `:core:designsystem`) to configure Enable/Disable and Interval (1, 3, 5, 10, 30 minutes), while omitting the Repeat Count section since reminders have no snooze count limit.

## 16. Reminder List Screen Simplification & Tab Labeling

### Scenario: Direct Reminder List & Removal of Obsolete Filters
- **Expected Behavior**:
  - Bottom navigation tab label and top bar title are standardized to "Nhắc nhở" (Reminders).
  - The reminder list screen presents reminders directly without any filter chips bar (ALL, ACTIVE, COMPLETED, TIME, LOCATION, WIFI).
  - Reminders are automatically sorted with snoozed active reminders pinned at the top, followed by remaining reminders in descending order of `createdAt`.
  - When no reminders exist, a clean empty state illustration and prompt are rendered.
- **Technical Decision**:
  - Completely excised `ReminderFilterChips`, `ReminderFilterType`, `setFilter`, and `filterCounts`.
  - Streamlined `ReminderListUiState` to hold the unified, sorted list of reminders.
  - Simplified `ReminderEmptyState` to render the default empty state without branching on filter types.

## 17. Decoupled Volume Slider and Vibration Toggle with Sound Preview Play/Stop Control

### Scenario: Separation of Volume & Vibration Rows in Edit Screens
- **Expected Behavior**:
  - In both `AlarmEditScreen` and `ReminderEditScreen` (`ReminderAlertConfigSection`), Volume and Vibration controls are decoupled into two distinct, dedicated UI rows instead of being cramped into a single row.
  - The Volume row contains:
    - Leading Volume icon (`VolumeOff` when 0, `VolumeUp` otherwise).
    - Centered Volume `Slider`.
    - Trailing Play/Preview button (`ReMindSettingIcon` displaying `PlayArrow` when stopped, and `Pause`/`Stop` with primary container highlight when playing).
  - The Vibration row is a dedicated setting row with title, description, and toggle switch (`Switch` or `LiteverSwitch`).
  - Clicking the Play button plays a preview of the selected ringtone at the current volume. Clicking it again stops playback.
  - Toggling vibration, picking a new ringtone, changing notification styles, saving, discarding, or navigating away from the screen immediately and safely halts playback without coroutine or media player leaks.
- **Technical Decision**:
  - In `AlarmEditScreen`:
    - Row 1: Ringtone row with `ReMindIcons.MusicNote`.
    - Row 2: Ringtone playback progress bar (`LinearProgressIndicator`).
    - Row 3: Dedicated Volume row with Slider and trailing `onTogglePreview` button (`ReMindSettingIcon`).
    - Row 4: Dedicated Vibration row using `AlarmSettingRow` with `ReMindIcons.Vibration` and trailing `Switch`.
  - In `ReminderAlertConfigSection` & `ReminderEditViewModel`:
    - Injected `AudioPlayer` into `ReminderEditViewModel` alongside `@ApplicationContext Context`.
    - Exposed `toggleRingtonePlayback()`, `startRingtonePlayback()`, and `stopRingtonePlayback()`.
    - Added `isRingtonePlaying: Boolean` to `ReminderEditUiState` and wired playback controls into `ReminderAlertConfigSection`.
    - Automated playback teardown in `onDispose`, `onCleared`, `updateRingtone`, `setVibrationEnabled`, and `setNotificationStyle`.

## 18. Shared PermissionWarningBanner in Core DesignSystem with Semantic Severity & Dismissal

### Scenario: Consolidated Permission Warning Banner Across Alarms and Reminders
- **Expected Behavior**:
  - `PermissionWarningBanner` resides in `:core:designsystem` as a reusable component adhering to Litever Design System guidelines.
  - Distinguishes two UX severity states:
    1. **Critical (Error)**: Core permissions required for operation are missing (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM` for active tasks). Rendered on `errorContainer` with `LvSemantic.Destructive` and `ReMindIcons.ErrorOutline`. Cannot be dismissed via "Remind me later".
    2. **Non-Critical (Warning)**: Optional/optimizing permissions are missing (Battery Optimization, System Overlay, DND access). Rendered on `warningContainer` with `LvSemantic.Warning` and `ReMindIcons.Warning`. Includes a "Remind me later" ("Nhắc tôi sau") dismissal button allowing the user to hide the banner.
  - Independent, feature-specific copy:
    - `:features:alarms` copy emphasizes alarm sound playback and ringing reliability on lock screen.
    - `:features:reminders` copy specifically emphasizes reminder trigger accuracy in the background ("Nhắc nhở"), avoiding alarm-specific terminology.
- **Technical Decision**:
  - Located at `vn.io.litever.remind.core.designsystem.components.PermissionWarningBanner.kt`.
  - Common button action labels (`permission_warning_banner_action` = "Fix Now" / "Sửa ngay" and `permission_warning_banner_remind_later` = "Remind me later" / "Nhắc tôi sau") defined in `:core:designsystem`.
  - Removed outdated feature-specific banners (`AlarmPermissionWarningBanner.kt` in `:features:alarms` and `ReminderPermissionWarningBanner.kt` in `:features:reminders`).
  - `ReminderListViewModel` and `AlarmListViewModel` both maintain `isPermissionWarningDismissed` state flows and expose `dismissPermissionWarning()`.

## 19. Reminder Lifecycle Unification & List Item UX Simplification

### Scenario: Elimination of `isCompleted` & List Item Switch Removal
- **Expected Behavior**:
  - Reminders have a single source of truth for active state: `isEnabled: Boolean`.
  - There is no separate `isCompleted` column in the database or domain model.
  - **One-time Reminders** (`!isRecurring`): When the reminder triggers and is dismissed or times out (auto-silence), it naturally turns off by setting `isEnabled = false`.
  - **Recurring / Interval Reminders** (`isRecurring == true` or `triggerType == INTERVAL`): Remains `isEnabled = true` upon dismiss, advancing `lastTriggeredTime` to the occurrence time and automatically recalculating the next trigger for future occurrences.
  - **List Card UI**: The `LiteverSwitch` is completely removed from the reminder card (`ReminderCard`). The card shows title, notes, time trigger info, repeat days/intervals, and a 3-dot overflow menu button. When `!isEnabled`, the card is rendered with dimmed alpha (0.6f) and `onSurface` color.
  - **Pausing / Disabling Reminders**:
    1. From list: Click 3-dot menu on the card -> Bottom Sheet displays "Tắt nhắc nhở" (or "Bật nhắc nhở" if disabled).
    2. From edit screen: A full-width action button ("Tắt nhắc nhở" / "Bật nhắc nhở") is placed at the bottom of `ReminderEditScreen` for existing reminders (`uiState.id != 0L`).
- **Technical Decision**:
  - Removed `isCompleted` field from `Reminder.kt`, `ReminderEntity.kt`, and `ReminderDao.kt`.
  - Executed Room Database `MIGRATION_7_8` (table recreation in SQLite dropping `is_completed` cleanly).
  - Cleaned up scheduler & receiver logic: `ReminderSchedulerImpl`, `ReminderSyncManager`, `ReminderTriggerEvaluator`, and `ReminderReceiver` only gate on `!reminder.isEnabled`.
  - Removed obsolete actions in `ReminderListViewModel`: `deleteCompletedReminders()`, `toggleCompleted()`.
  - Updated `ReminderActionBottomSheet.kt` with `onToggleEnabledClick` replacing `onToggleCompletedClick`.
  - Updated `ReminderCard.kt` removing `LiteverSwitch` and `onToggle` parameter.

## 20. Unified Snooze Bottom Sheet & Date/Time Picker Dialogs Across Alarms and Reminders

### Scenario: Snooze Bottom Sheet & Date/Time Picker Consistency
- **Expected Behavior**:
  - **Snooze Configuration**: Both `AlarmEditScreen` and `ReminderEditScreen` (`ReminderAlertConfigSection`) display Snooze ("Báo lại") as a compact setting row opening the shared `SnoozeBottomSheet` from `:core:designsystem`. For Reminders, the Repeat Count section is hidden (`repeatCount = null`), displaying only Enable toggle and Interval options (`1, 3, 5, 10, 30` min).
  - **Date & Time Picker Dialogs**: All Date and Time Picker dialogs in `:features:reminders` (`ReminderRepeatConfigSection`, `ReminderTimeConfigSection`, `ReminderTimeWindowConfigSection`) match `AlarmEditScreen` visually and structurally:
    - Material 3 `DatePickerDialog` and `TimePickerDialog` with bold `titleLarge` header titles (`"Đặt ngày nhắc nhở"` / `"Set Reminder Date"`, `"Đặt giờ nhắc nhở"` / `"Set Reminder Time"`, `"Đặt giờ bắt đầu"` / `"Set Start Time"`, `"Đặt giờ kết thúc"` / `"Set End Time"`).
    - `LvButton` (`LvSemantic.Primary`) for confirm ("Lưu" / "Save") and `LvButton` (`LvButtonType.Outlined`, `LvSemantic.Secondary`) for dismiss ("Hủy" / "Cancel").
- **Technical Decision**:
  - Consolidated `SnoozeBottomSheet.kt` into `:core:designsystem`.
  - Replaced legacy `LiteverDatePickerDialog`, `LiteverTimePickerDialog`, `LiteverButton`, and `LiteverOutlinedButton` in `ReminderRepeatConfigSection.kt`, `ReminderTimeConfigSection.kt`, and `ReminderTimeWindowConfigSection.kt` with `DatePickerDialog`, `TimePickerDialog`, and `LvButton`.

