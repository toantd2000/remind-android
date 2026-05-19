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
