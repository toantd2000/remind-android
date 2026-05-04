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
    - **Simultaneous Scheduling**: `AlarmSyncManager` (on boot) must schedule BOTH if a snooze is active, ensuring the long-term schedule is preserved even during a snooze session.
